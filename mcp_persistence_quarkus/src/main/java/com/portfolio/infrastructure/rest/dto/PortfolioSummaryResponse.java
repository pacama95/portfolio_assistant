package com.portfolio.infrastructure.rest.dto;

import java.math.BigDecimal;

/**
 * REST DTO for portfolio summary responses
 */
public class PortfolioSummaryResponse {

    private BigDecimal totalMarketValue;
    private BigDecimal totalCost;
    private BigDecimal totalUnrealizedGainLoss;
    private BigDecimal totalUnrealizedGainLossPercentage;
    private long totalPositions;
    private long activePositions;

    // Default constructor
    public PortfolioSummaryResponse() {}

    public PortfolioSummaryResponse(BigDecimal totalMarketValue, BigDecimal totalCost, 
                                  BigDecimal totalUnrealizedGainLoss, BigDecimal totalUnrealizedGainLossPercentage,
                                  long totalPositions, long activePositions) {
        this.totalMarketValue = totalMarketValue;
        this.totalCost = totalCost;
        this.totalUnrealizedGainLoss = totalUnrealizedGainLoss;
        this.totalUnrealizedGainLossPercentage = totalUnrealizedGainLossPercentage;
        this.totalPositions = totalPositions;
        this.activePositions = activePositions;
    }

    // Getters and setters
    public BigDecimal getTotalMarketValue() { return totalMarketValue; }
    public void setTotalMarketValue(BigDecimal totalMarketValue) { this.totalMarketValue = totalMarketValue; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getTotalUnrealizedGainLoss() { return totalUnrealizedGainLoss; }
    public void setTotalUnrealizedGainLoss(BigDecimal totalUnrealizedGainLoss) { this.totalUnrealizedGainLoss = totalUnrealizedGainLoss; }

    public BigDecimal getTotalUnrealizedGainLossPercentage() { return totalUnrealizedGainLossPercentage; }
    public void setTotalUnrealizedGainLossPercentage(BigDecimal totalUnrealizedGainLossPercentage) { 
        this.totalUnrealizedGainLossPercentage = totalUnrealizedGainLossPercentage; 
    }

    public long getTotalPositions() { return totalPositions; }
    public void setTotalPositions(long totalPositions) { this.totalPositions = totalPositions; }

    public long getActivePositions() { return activePositions; }
    public void setActivePositions(long activePositions) { this.activePositions = activePositions; }
} 