package com.portfolio.infrastructure.persistence.mapper;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Position;
import com.portfolio.infrastructure.persistence.entity.PositionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PositionEntityMapperTest {
    private PositionEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PositionEntityMapper.class);
    }

    @Test
    void testToEntityFullMapping() {
        Position position = new Position();
        UUID id = UUID.randomUUID();
        position.setId(id);
        position.setTicker("AAPL");
        position.setTotalQuantity(new BigDecimal("10.0"));
        position.setAveragePrice(new BigDecimal("100.0"));
        position.setTotalCost(new BigDecimal("1000.0"));
        position.setCurrentPrice(new BigDecimal("120.0"));
        position.setCurrency(Currency.EUR);
        position.setLastUpdated(java.time.LocalDate.now());

        PositionEntity entity = mapper.toEntity(position);
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("AAPL", entity.getTicker());
        assertEquals(new BigDecimal("10.0"), entity.getCurrentQuantity());
        assertEquals(new BigDecimal("100.0"), entity.getAvgCostPerShare());
        assertEquals(new BigDecimal("1000.0"), entity.getTotalCostBasis());
        assertEquals(new BigDecimal("120.0"), entity.getCurrentPrice());
        assertEquals(Currency.EUR, entity.getPrimaryCurrency());
        assertEquals(position.getLastUpdated(), entity.getLastTransactionDate());
        assertEquals(position.getLastUpdated(), entity.getFirstPurchaseDate());
        // Unrealized gain/loss: (10 * 120) - 1000 = 200
        assertEquals(new BigDecimal("200.00"), entity.getUnrealizedGainLoss());
    }

    @Test
    void testToEntityWithNulls() {
        Position position = new Position();
        // All fields null
        PositionEntity entity = mapper.toEntity(position);
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getTicker());
        assertNull(entity.getCurrentQuantity());
        assertNull(entity.getAvgCostPerShare());
        assertNull(entity.getTotalCostBasis());
        assertNull(entity.getCurrentPrice());
        assertNull(entity.getPrimaryCurrency());
        assertNull(entity.getLastTransactionDate());
        assertNull(entity.getFirstPurchaseDate());
        assertNull(entity.getUnrealizedGainLoss());
    }

    @Test
    void testToEntityUnrealizedGainLossNullIfMissingFields() {
        Position position = new Position();
        position.setTotalQuantity(new BigDecimal("10.0"));
        // Missing currentPrice and totalCost
        PositionEntity entity = mapper.toEntity(position);
        assertNull(entity.getUnrealizedGainLoss());
    }
} 