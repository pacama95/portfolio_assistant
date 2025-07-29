package com.portfolio.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Transaction domain entity representing a financial transaction
 */
public class Transaction {
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
    private Boolean isFractional;
    private BigDecimal fractionalMultiplier;
    private Currency commissionCurrency;

    // Default constructor
    public Transaction() {
        this.isActive = true;
        this.isFractional = false;
        this.fractionalMultiplier = BigDecimal.ONE;
        this.commissionCurrency = null;
    }

    // Constructor with required fields
    public Transaction(String ticker, TransactionType transactionType, BigDecimal quantity,
                       BigDecimal price, Currency currency, LocalDate transactionDate) {
        this();
        this.ticker = ticker;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.fees = BigDecimal.ZERO;
    }

    /**
     * Validates if the transaction is ready for processing
     */
    public boolean isValidForProcessing() {
        return ticker != null && !ticker.trim().isEmpty() &&
               transactionType != null &&
               quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0 &&
               price != null && price.compareTo(BigDecimal.ZERO) > 0 &&
               currency != null &&
               transactionDate != null;
    }

    /**
     * Calculates the total value (quantity * price)
     */
    public BigDecimal getTotalValue() {
        return quantity.multiply(price);
    }

    /**
     * Calculates the total cost including fees
     */
    public BigDecimal getTotalCost() {
        return getTotalValue().add(fees != null ? fees : BigDecimal.ZERO);
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

    public Boolean getIsFractional() { return isFractional; }
    public void setIsFractional(Boolean isFractional) { this.isFractional = isFractional; }
    public BigDecimal getFractionalMultiplier() { return fractionalMultiplier; }
    public void setFractionalMultiplier(BigDecimal fractionalMultiplier) { this.fractionalMultiplier = fractionalMultiplier; }
    public Currency getCommissionCurrency() { return commissionCurrency; }
    public void setCommissionCurrency(Currency commissionCurrency) { this.commissionCurrency = commissionCurrency; }
} 