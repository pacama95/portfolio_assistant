package com.portfolio.infrastructure.rest.dto;

import java.math.BigDecimal;

public record PortfolioSummaryResponse(
    BigDecimal totalMarketValue,
    BigDecimal totalCost,
    BigDecimal totalUnrealizedGainLoss,
    BigDecimal totalUnrealizedGainLossPercentage,
    long totalPositions,
    long activePositions
) {} 