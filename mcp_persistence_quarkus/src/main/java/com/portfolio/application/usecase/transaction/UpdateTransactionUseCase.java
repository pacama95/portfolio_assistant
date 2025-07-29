package com.portfolio.application.usecase.transaction;

import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.port.PositionRepository;
import com.portfolio.domain.port.TransactionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Use case for updating transactions
 */
@ApplicationScoped
public class UpdateTransactionUseCase {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    PositionRepository positionRepository;

    /**
     * Updates an existing transaction and recalculates affected positions
     */
    @WithTransaction
    public Uni<Transaction> execute(UUID id, Transaction updatedTransaction) {
        return transactionRepository.findById(id)
            .flatMap(existingTransaction -> {
                if (existingTransaction == null) {
                    return Uni.createFrom().nullItem();
                }

                String oldTicker = existingTransaction.getTicker();
                String newTicker = updatedTransaction.getTicker();

                return transactionRepository.update(updatedTransaction)
                    .flatMap(savedTransaction -> {
                        // Recalculate position for the new ticker
                        Uni<Void> recalculateNew = positionRepository.recalculatePosition(newTicker)
                            .replaceWithVoid();

                        // If ticker changed, also recalculate the old ticker's position
                        if (!oldTicker.equals(newTicker)) {
                            return recalculateNew
                                .flatMap(ignored -> positionRepository.recalculatePosition(oldTicker))
                                .replaceWith(savedTransaction);
                        } else {
                            return recalculateNew.replaceWith(savedTransaction);
                        }
                    });
            });
    }
} 