package com.portfolio.infrastructure.rest.dto;

import com.portfolio.domain.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST DTO for position responses
 */
public class PositionResponse {

    private UUID id;
    private String ticker;
    private BigDecimal totalQuantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal totalCost;
    private Currency currency;
    private LocalDate lastUpdated;
    private Boolean isActive;
    private BigDecimal marketValue;
    private BigDecimal unrealizedGainLoss;
    private BigDecimal unrealizedGainLossPercentage;

    // Default constructor
    public PositionResponse() {}

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }

    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

    public BigDecimal getUnrealizedGainLoss() { return unrealizedGainLoss; }
    public void setUnrealizedGainLoss(BigDecimal unrealizedGainLoss) { this.unrealizedGainLoss = unrealizedGainLoss; }

    public BigDecimal getUnrealizedGainLossPercentage() { return unrealizedGainLossPercentage; }
    public void setUnrealizedGainLossPercentage(BigDecimal unrealizedGainLossPercentage) { 
        this.unrealizedGainLossPercentage = unrealizedGainLossPercentage; 
    }
} 