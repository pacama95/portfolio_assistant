package com.portfolio.application.usecase.transaction;

import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.port.PositionRepository;
import com.portfolio.domain.port.TransactionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Use case for creating new transactions
 */
@ApplicationScoped
public class CreateTransactionUseCase {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    PositionRepository positionRepository;

    /**
     * Creates a new transaction and updates the corresponding position
     */
    @WithTransaction
    public Uni<Transaction> execute(Transaction transaction) {
        return transactionRepository.save(transaction)
                .flatMap(savedTransaction -> positionRepository.recalculatePosition(savedTransaction.getTicker()))
                .onItem().invoke(position -> Log.info("Transaction saved and related position with ticker %s recalculated".formatted(position.getTicker())))
                .replaceWith(transaction);
    }
} 