package com.portfolio.infrastructure.persistence.mapper;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Transaction;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.infrastructure.persistence.entity.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityMapperTest {
    private TransactionEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TransactionEntityMapper.class);
    }

    @Test
    void testToEntityFullMapping() {
        Transaction transaction = new Transaction();
        UUID id = UUID.randomUUID();
        transaction.setId(id);
        transaction.setTicker("AAPL");
        transaction.setTransactionType(TransactionType.BUY);
        transaction.setQuantity(new BigDecimal("10.0"));
        transaction.setPrice(new BigDecimal("100.0"));
        transaction.setFees(new BigDecimal("5.0"));
        transaction.setCurrency(Currency.USD);
        transaction.setTransactionDate(LocalDate.of(2024, 6, 1));
        transaction.setNotes("Test note");

        TransactionEntity entity = mapper.toEntity(transaction);
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("AAPL", entity.getTicker());
        assertEquals(TransactionType.BUY, entity.getTransactionType());
        assertEquals(new BigDecimal("10.0"), entity.getQuantity());
        assertEquals(new BigDecimal("100.0"), entity.getCostPerShare());
        assertEquals(new BigDecimal("5.0"), entity.getCommission());
        assertEquals(Currency.USD, entity.getCurrency());
        assertEquals(LocalDate.of(2024, 6, 1), entity.getTransactionDate());
        assertEquals("Test note", entity.getNotes());
    }

    @Test
    void testToEntityWithNulls() {
        Transaction transaction = new Transaction();
        TransactionEntity entity = mapper.toEntity(transaction);
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getTicker());
        assertNull(entity.getTransactionType());
        assertNull(entity.getQuantity());
        assertNull(entity.getCostPerShare());
        assertNull(entity.getCommission());
        assertNull(entity.getCurrency());
        assertNull(entity.getTransactionDate());
        assertNull(entity.getNotes());
    }

    @Test
    void testToDomainFullMapping() {
        TransactionEntity entity = new TransactionEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setTicker("AAPL");
        entity.setTransactionType(TransactionType.SELL);
        entity.setQuantity(new BigDecimal("20.0"));
        entity.setCostPerShare(new BigDecimal("150.0"));
        entity.setCommission(new BigDecimal("2.5"));
        entity.setCurrency(Currency.CAD);
        entity.setTransactionDate(LocalDate.of(2024, 5, 15));
        entity.setNotes("Domain test");

        Transaction transaction = mapper.toDomain(entity);
        assertNotNull(transaction);
        assertEquals(id, transaction.getId());
        assertEquals("AAPL", transaction.getTicker());
        assertEquals(TransactionType.SELL, transaction.getTransactionType());
        assertEquals(new BigDecimal("20.0"), transaction.getQuantity());
        assertEquals(new BigDecimal("150.0"), transaction.getPrice());
        assertEquals(new BigDecimal("2.5"), transaction.getFees());
        assertEquals(Currency.CAD, transaction.getCurrency());
        assertEquals(LocalDate.of(2024, 5, 15), transaction.getTransactionDate());
        assertEquals("Domain test", transaction.getNotes());
    }
} 