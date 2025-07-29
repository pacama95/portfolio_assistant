package com.portfolio.infrastructure.rest.dto;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST DTO for transaction responses
 */
public class TransactionResponse {

    private UUID id;
    private String ticker;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal fees;
    private Currency currency;
    private LocalDate transactionDate;
    private String notes;
    private Boolean isActive;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private Boolean isFractional;
    private BigDecimal fractionalMultiplier;
    private Currency commissionCurrency;

    // Default constructor
    public TransactionResponse() {
        this.isFractional = false;
        this.fractionalMultiplier = BigDecimal.ONE;
        this.commissionCurrency = null;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public Boolean getIsFractional() { return isFractional; }
    public void setIsFractional(Boolean isFractional) { this.isFractional = isFractional; }
    public BigDecimal getFractionalMultiplier() { return fractionalMultiplier; }
    public void setFractionalMultiplier(BigDecimal fractionalMultiplier) { this.fractionalMultiplier = fractionalMultiplier; }
    public Currency getCommissionCurrency() { return commissionCurrency; }
    public void setCommissionCurrency(Currency commissionCurrency) { this.commissionCurrency = commissionCurrency; }
} 