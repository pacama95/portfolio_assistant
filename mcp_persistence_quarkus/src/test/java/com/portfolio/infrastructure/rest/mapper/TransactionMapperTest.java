package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.application.command.UpdateTransactionCommand;
import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.infrastructure.rest.dto.CreateTransactionRequest;
import com.portfolio.infrastructure.rest.dto.TransactionResponse;
import com.portfolio.infrastructure.rest.dto.UpdateTransactionRequest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {
    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void testToResponse_normalizesFields() {
        Transaction tx = new Transaction();
        tx.setId(java.util.UUID.randomUUID());
        tx.setTicker("AAPL");
        tx.setTransactionType(TransactionType.BUY);
        tx.setQuantity(new BigDecimal("2.9876543"));
        tx.setPrice(new BigDecimal("456.789123"));
        tx.setFees(new BigDecimal("1.234567"));
        tx.setCurrency(Currency.USD);
        tx.setTransactionDate(LocalDate.of(2024, 2, 2));
        tx.setNotes("Test2");
        tx.setIsActive(true);

        TransactionResponse resp = mapper.toResponse(tx);

        assertEquals(new BigDecimal("2.987654"), resp.quantity()); // scale 6
        assertEquals(new BigDecimal("456.7891"), resp.price()); // scale 4
        assertEquals(new BigDecimal("1.2346"), resp.fees()); // scale 4
    }

    @Test
    void testToTransaction_AllFieldsMapped() {
        CreateTransactionRequest req = new CreateTransactionRequest(
            "AAPL",
            TransactionType.BUY,
            new BigDecimal("1.234567"),
            new BigDecimal("123.4567"),
            new BigDecimal("0.1234"),
            Currency.USD,
            LocalDate.of(2024, 6, 1),
            "Test notes",
            true,
            new BigDecimal("0.25000000"),
            Currency.EUR
        );

        Transaction tx = mapper.toTransaction(req);

        assertNotNull(tx);
        assertEquals("AAPL", tx.getTicker());
        assertEquals(TransactionType.BUY, tx.getTransactionType());
        assertEquals(new BigDecimal("1.234567"), tx.getQuantity());
        assertEquals(new BigDecimal("123.4567"), tx.getPrice());
        assertEquals(new BigDecimal("0.1234"), tx.getFees());
        assertEquals(Currency.USD, tx.getCurrency());
        assertEquals(LocalDate.of(2024, 6, 1), tx.getTransactionDate());
        assertEquals("Test notes", tx.getNotes());
        assertTrue(tx.getIsFractional());
        assertEquals(new BigDecimal("0.2500"), tx.getFractionalMultiplier());
        assertEquals(Currency.EUR, tx.getCommissionCurrency());
    }

    @Test
    void testToUpdateTransactionCommand_AllFieldsMapped() {
        UUID transactionId = UUID.randomUUID();
        UpdateTransactionRequest req = new UpdateTransactionRequest(
            "MSFT",
            TransactionType.SELL,
            new BigDecimal("5.123456789"),
            new BigDecimal("250.987654321"),
            new BigDecimal("2.555555"),
            Currency.EUR,
            LocalDate.of(2024, 3, 15),
            "Update test notes",
            true,
            new BigDecimal("0.333333333"),
            Currency.GBP
        );

        UpdateTransactionCommand command = mapper.toUpdateTransactionCommand(transactionId, req);

        assertNotNull(command);
        assertEquals(transactionId, command.transactionId());
        assertEquals("MSFT", command.ticker());
        assertEquals(TransactionType.SELL, command.transactionType());
        assertEquals(new BigDecimal("5.123457"), command.quantity()); // normalized to 6 decimal places
        assertEquals(new BigDecimal("250.9877"), command.price()); // normalized to 4 decimal places
        assertEquals(new BigDecimal("2.5556"), command.fees()); // normalized to 4 decimal places
        assertEquals(Currency.EUR, command.currency());
        assertEquals(LocalDate.of(2024, 3, 15), command.transactionDate());
        assertEquals("Update test notes", command.notes());
        assertTrue(command.isFractional());
        assertEquals(new BigDecimal("0.3333"), command.fractionalMultiplier()); // normalized to 4 decimal places
        assertEquals(Currency.GBP, command.commissionCurrency());
    }
} 