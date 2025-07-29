package com.portfolio.application.usecase.transaction;

import com.portfolio.domain.port.PositionRepository;
import com.portfolio.domain.port.TransactionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Use case for deleting transactions
 */
@ApplicationScoped
public class DeleteTransactionUseCase {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    PositionRepository positionRepository;

    /**
     * Deletes a transaction and recalculates the affected position
     */
    @WithTransaction
    public Uni<Boolean> execute(UUID id) {
        return transactionRepository.findById(id)
            .flatMap(transaction -> {
                if (transaction == null) {
                    return Uni.createFrom().item(false);
                }

                String ticker = transaction.getTicker();
                
                return transactionRepository.deleteById(id)
                    .flatMap(deleted -> {
                        if (deleted) {
                            // Recalculate position after deletion
                            return positionRepository.recalculatePosition(ticker)
                                .replaceWith(true);
                        } else {
                            return Uni.createFrom().item(false);
                        }
                    });
            });
    }
} 