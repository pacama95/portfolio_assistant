package com.portfolio.application.usecase.transaction;

import com.portfolio.application.command.UpdateTransactionCommand;
import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.port.TransactionRepository;
import com.portfolio.util.StringUtils;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UpdateTransactionUseCase {

    @Inject
    TransactionRepository transactionRepository;

    @WithTransaction
    public Uni<Transaction> execute(UpdateTransactionCommand updateTransactionCommand) {
        return transactionRepository.findById(updateTransactionCommand.transactionId())
                .onItem()
                .ifNotNull().transformToUni(transaction -> updateAndPersistTransaction(transaction, updateTransactionCommand))
                .onItem()
                .ifNull().failWith(() -> new ServiceException(Errors.UpdateTransaction.NOT_FOUND));
    }

    private Uni<Transaction> updateAndPersistTransaction(Transaction current, UpdateTransactionCommand updateTransactionCommand) {
        return Uni.createFrom().item(()  -> updateTransaction(current, updateTransactionCommand))
                .flatMap(transactionUpdated -> transactionRepository.update(transactionUpdated));
    }

    private Transaction updateTransaction(Transaction current, UpdateTransactionCommand updateTransactionCommand) {
        if (StringUtils.hasMeaningfulContent(updateTransactionCommand.ticker())) current.setTicker(updateTransactionCommand.ticker());
        if (updateTransactionCommand.transactionType() != null) current.setTransactionType(updateTransactionCommand.transactionType());
        if (updateTransactionCommand.transactionDate() != null) current.setTransactionDate(updateTransactionCommand.transactionDate());
        if (StringUtils.hasMeaningfulContent(updateTransactionCommand.notes())) current.setNotes(updateTransactionCommand.notes());
        if (updateTransactionCommand.price() != null) current.setPrice(updateTransactionCommand.price());
        if (updateTransactionCommand.quantity() != null) current.setQuantity(updateTransactionCommand.quantity());
        if (updateTransactionCommand.currency() != null) current.setCurrency(updateTransactionCommand.currency());
        if(updateTransactionCommand.fractionalMultiplier() != null) current.setFractionalMultiplier(updateTransactionCommand.fractionalMultiplier());
        if(updateTransactionCommand.commissionCurrency() != null) current.setCommissionCurrency(updateTransactionCommand.commissionCurrency());
        current.setIsFractional(updateTransactionCommand.isFractional());

        return current;
    }
} 