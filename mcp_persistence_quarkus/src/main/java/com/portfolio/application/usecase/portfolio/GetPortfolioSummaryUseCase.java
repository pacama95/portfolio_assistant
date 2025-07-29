package com.portfolio.application.usecase.portfolio;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Position;
import com.portfolio.domain.port.PositionRepository;
import com.portfolio.domain.model.PortfolioSummary;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Use case for calculating portfolio summary
 */
@ApplicationScoped
public class GetPortfolioSummaryUseCase {

    @Inject
    PositionRepository positionRepository;

    /**
     * Gets summary for all positions
     */
    @WithSession
    public Uni<PortfolioSummary> getPortfolioSummary() {
        return positionRepository.findAll()
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPortfolioSummary.PERSISTENCE_ERROR, "Error getting all positions", throwable))
            .map(this::calculateSummary);
    }

    /**
     * Gets summary for active positions only (shares > 0)
     */
    @WithSession
    public Uni<PortfolioSummary> getActiveSummary() {
        return positionRepository.findAllWithShares()
                .onFailure().transform(throwable ->
                        new ServiceException(Errors.GetPortfolioSummary.PERSISTENCE_ERROR, "Error getting all positions with shares", throwable))
            .map(this::calculateSummary);
    }

    /**
     * Calculate portfolio summary from positions
     */
    private PortfolioSummary calculateSummary(List<Position> positions) {
        if (positions.isEmpty()) {
            return PortfolioSummary.empty();
        }

        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        long activePositions = 0;

        for (Position position : positions) {
            if (position.hasShares()) {
                activePositions++;
            }
            totalMarketValue = totalMarketValue.add(position.getMarketValue());
            totalCost = totalCost.add(position.getTotalCost());
        }

        BigDecimal totalUnrealizedGainLoss = totalMarketValue.subtract(totalCost);
        BigDecimal totalUnrealizedGainLossPercentage = BigDecimal.ZERO;

        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            totalUnrealizedGainLossPercentage = totalUnrealizedGainLoss
                .divide(totalCost, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(6, RoundingMode.HALF_UP);
        }

        return new PortfolioSummary(
            totalMarketValue,
            totalCost,
            totalUnrealizedGainLoss,
            totalUnrealizedGainLossPercentage,
            positions.size(),
            activePositions
        );
    }
} 