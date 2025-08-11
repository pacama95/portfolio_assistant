package com.portfolio.application.usecase.transaction;

import com.portfolio.domain.exception.Errors;
import com.portfolio.domain.exception.ServiceException;
import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.domain.port.TransactionRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateTransactionUseCaseTest {
    private TransactionRepository transactionRepository;
    private CreateTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        useCase = new CreateTransactionUseCase();
        useCase.transactionRepository = transactionRepository;
    }

    @Test
    void testExecuteSuccess() {
        // Given
        Transaction inputTransaction = createValidTransaction();
        Transaction savedTransaction = createValidTransaction();
        savedTransaction.setId(UUID.randomUUID());

        when(transactionRepository.save(any(Transaction.class)))
            .thenReturn(Uni.createFrom().item(savedTransaction));

        // When
        Uni<Transaction> result = useCase.execute(inputTransaction);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(actualTransaction);
        assertEquals(savedTransaction.getId(), actualTransaction.getId());
        assertEquals(savedTransaction.getTicker(), actualTransaction.getTicker());
        assertEquals(savedTransaction.getTransactionType(), actualTransaction.getTransactionType());
        assertEquals(savedTransaction.getQuantity(), actualTransaction.getQuantity());
        assertEquals(savedTransaction.getPrice(), actualTransaction.getPrice());
        assertEquals(savedTransaction.getCurrency(), actualTransaction.getCurrency());
        assertEquals(savedTransaction.getTransactionDate(), actualTransaction.getTransactionDate());

        verify(transactionRepository).save(inputTransaction);
    }

    @Test
    void testExecuteRepositoryFailure() {
        // Given
        Transaction inputTransaction = createValidTransaction();
        RuntimeException exception = new RuntimeException("Database error");

        when(transactionRepository.save(any(Transaction.class)))
            .thenReturn(Uni.createFrom().failure(exception));

        // When
        Uni<Transaction> result = useCase.execute(inputTransaction);

        // Then
        var failure = result.subscribe()
                        .withSubscriber(UniAssertSubscriber.create())
                        .assertFailedWith(ServiceException.class)
                        .getFailure();

        assertEquals(Errors.CreateTransaction.PERSISTENCE_ERROR, ((ServiceException) failure).getError());

        verify(transactionRepository).save(inputTransaction);
    }

    @Test
    void testExecuteWithComplexTransaction() {
        // Given
        Transaction inputTransaction = createComplexTransaction();
        Transaction savedTransaction = createComplexTransaction();
        savedTransaction.setId(UUID.randomUUID());

        when(transactionRepository.save(any(Transaction.class)))
            .thenReturn(Uni.createFrom().item(savedTransaction));

        // When
        Uni<Transaction> result = useCase.execute(inputTransaction);

        // Then
        Transaction actualTransaction = result.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .assertCompleted()
            .getItem();

        assertNotNull(actualTransaction);
        assertEquals(savedTransaction.getId(), actualTransaction.getId());
        assertEquals(savedTransaction.getTicker(), actualTransaction.getTicker());
        assertEquals(savedTransaction.getFees(), actualTransaction.getFees());
        assertEquals(savedTransaction.getNotes(), actualTransaction.getNotes());
        assertEquals(savedTransaction.getIsFractional(), actualTransaction.getIsFractional());
        assertEquals(savedTransaction.getFractionalMultiplier(), actualTransaction.getFractionalMultiplier());
        assertEquals(savedTransaction.getCommissionCurrency(), actualTransaction.getCommissionCurrency());

        verify(transactionRepository).save(inputTransaction);
    }

    private Transaction createValidTransaction() {
        return new Transaction(
            "AAPL",
            TransactionType.BUY,
            new BigDecimal("10"),
            new BigDecimal("150.50"),
            Currency.USD,
            LocalDate.of(2024, 1, 15)
        );
    }

    private Transaction createComplexTransaction() {
        Transaction transaction = new Transaction(
            "MSFT",
            TransactionType.SELL,
            new BigDecimal("5.5"),
            new BigDecimal("420.75"),
            Currency.USD,
            LocalDate.of(2024, 2, 20)
        );
        transaction.setFees(new BigDecimal("9.99"));
        transaction.setNotes("Complex sell transaction");
        transaction.setIsFractional(true);
        transaction.setFractionalMultiplier(new BigDecimal("0.5"));
        transaction.setCommissionCurrency(Currency.EUR);
        return transaction;
    }
}
