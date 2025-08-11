package com.portfolio.infrastructure.rest.dto;

import com.portfolio.domain.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PositionResponse(
    UUID id,
    String ticker,
    BigDecimal totalQuantity,
    BigDecimal averagePrice,
    BigDecimal currentPrice,
    BigDecimal totalCost,
    Currency currency,
    LocalDate lastUpdated,
    Boolean isActive,
    BigDecimal marketValue,
    BigDecimal unrealizedGainLoss,
    BigDecimal unrealizedGainLossPercentage
) {} 