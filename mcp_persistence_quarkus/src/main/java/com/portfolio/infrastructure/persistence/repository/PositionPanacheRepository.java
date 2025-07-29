package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.PositionEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Panache reactive repository for PositionEntity
 */
@ApplicationScoped
public class PositionPanacheRepository implements PanacheRepository<PositionEntity> {

    @Inject
    TransactionPanacheRepository transactionRepository;

    // READ operations - use @WithSession
    @WithSession
    public Uni<PositionEntity> findById(UUID id) {
        return find("id = ?1", id).firstResult();
    }

    @WithSession
    public Uni<Boolean> deleteById(UUID id) {
        return delete("id = ?1", id).map(count -> count > 0);
    }

    @WithSession
    public Uni<PositionEntity> findByTicker(String ticker) {
        return find("ticker = ?1", ticker).firstResult();
    }

    @WithSession
    public Uni<List<PositionEntity>> findAllWithShares() {
        return find("currentQuantity > 0 ORDER BY ticker").list();
    }

    @WithSession
    public Uni<List<PositionEntity>> findAllActive() {
        return find("ORDER BY ticker").list();
    }

    @WithSession
    public Uni<Boolean> existsByTicker(String ticker) {
        return find("ticker = ?1", ticker)
            .count()
            .map(count -> count > 0);
    }

    @WithSession
    public Uni<Long> countWithShares() {
        return find("currentQuantity > 0").count();
    }

    // WRITE operations - use @WithTransaction
    @WithTransaction
    public Uni<PositionEntity> updateMarketPrice(String ticker, BigDecimal newPrice) {
        return findByTicker(ticker)
            .flatMap(position -> {
                if (position == null) {
                    return Uni.createFrom().nullItem();
                }
                
                position.setCurrentPrice(newPrice);
                position.setLastPriceUpdate(OffsetDateTime.now());
                
                // Recalculate market value and unrealized gain/loss
                BigDecimal marketValue = position.getCurrentQuantity().multiply(newPrice);
                position.setCurrentMarketValue(marketValue);

                BigDecimal costBasis = position.getTotalCostBasis();
                position.setUnrealizedGainLoss(marketValue.subtract(costBasis));
                
                return persistAndFlush(position);
            });
    }

    @WithTransaction
    public Uni<PositionEntity> upsertPosition(PositionEntity position) {
        return findByTicker(position.getTicker())
            .flatMap(existing -> {
                if (existing != null) {
                    // Update existing position
                    existing.setCurrentQuantity(position.getCurrentQuantity());
                    existing.setAvgCostPerShare(position.getAvgCostPerShare());
                    existing.setTotalCostBasis(position.getTotalCostBasis());
                    existing.setPrimaryCurrency(position.getPrimaryCurrency());
                    existing.setLastTransactionDate(position.getLastTransactionDate());
                    existing.setTotalCommissions(position.getTotalCommissions());
                    existing.setFirstPurchaseDate(position.getFirstPurchaseDate());
                    
                    // Recalculate derived fields if current price exists
                    BigDecimal marketValue = existing.getCurrentQuantity().multiply(existing.getCurrentPrice());
                    existing.setCurrentMarketValue(marketValue);
                    existing.setUnrealizedGainLoss(marketValue.subtract(existing.getTotalCostBasis()));
                    
                    return persistAndFlush(existing);
                } else {
                    // Create new position
                    return persistAndFlush(position);
                }
            });
    }

    @WithTransaction
    public Uni<PositionEntity> recalculatePosition(String ticker) {
        // Call the stored procedure to recalculate position from transactions
        // Use a native query to execute the stored procedure
        return find("SELECT recalculate_position(?1)", ticker)
            .firstResult()
            .flatMap(result -> findByTicker(ticker));
    }
} 