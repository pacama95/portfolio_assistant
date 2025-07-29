package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.infrastructure.rest.dto.CreateTransactionRequest;
import com.portfolio.infrastructure.rest.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

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

        assertEquals(new BigDecimal("2.987654"), resp.getQuantity()); // scale 6
        assertEquals(new BigDecimal("456.7891"), resp.getPrice()); // scale 4
        assertEquals(new BigDecimal("1.2346"), resp.getFees()); // scale 4
    }

    @Test
    void testToTransaction_AllFieldsMapped() {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setTicker("AAPL");
        req.setTransactionType(TransactionType.BUY);
        req.setQuantity(new BigDecimal("1.234567"));
        req.setPrice(new BigDecimal("123.4567"));
        req.setFees(new BigDecimal("0.1234"));
        req.setCurrency(Currency.USD);
        req.setTransactionDate(LocalDate.of(2024, 6, 1));
        req.setNotes("Test notes");
        req.setIsFractional(true);
        req.setFractionalMultiplier(new BigDecimal("0.25000000"));
        req.setCommissionCurrency(Currency.EUR);

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
} 