package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.Position;
import com.portfolio.infrastructure.rest.dto.PositionResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PositionMapperTest {
    private final PositionMapper mapper = Mappers.getMapper(PositionMapper.class);

    @Test
    void testToResponse_normalizesFields() {
        Position pos = new Position();
        pos.setId(UUID.randomUUID());
        pos.setTicker("GOOG");
        pos.setTotalQuantity(new BigDecimal("10.1234567"));
        pos.setAveragePrice(new BigDecimal("100.987654"));
        pos.setCurrentPrice(new BigDecimal("101.234567"));
        pos.setTotalCost(new BigDecimal("1010.987654"));
        pos.setCurrency(Currency.USD);
        pos.setLastUpdated(LocalDate.of(2024, 3, 3));
        pos.setIsActive(true);

        PositionResponse resp = mapper.toResponse(pos);

        assertEquals(new BigDecimal("10.123457"), resp.totalQuantity()); // scale 6
        assertEquals(new BigDecimal("100.9877"), resp.averagePrice()); // scale 4
        assertEquals(new BigDecimal("101.2346"), resp.currentPrice()); // scale 4
        assertEquals(new BigDecimal("1010.9877"), resp.totalCost()); // scale 4
    }
} 