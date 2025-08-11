package com.portfolio.application.usecase.transaction;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Transaction;
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

    /**
     * Creates a new transaction and updates the corresponding position
     */
    @WithTransaction
    public Uni<Transaction> execute(Transaction transaction) {
        return transactionRepository.save(transaction)
                .onFailure().transform(throwable -> new ServiceException(Errors.CreateTransaction.PERSISTENCE_ERROR, throwable))
                .onItem().invoke(saved -> Log.info("Transaction saved for ticker %s".formatted(saved.getTicker())));
    }
} 