package com.portfolio.infrastructure.rest.dto;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * REST DTO for creating transactions
 */
public class CreateTransactionRequest {

    @NotNull(message = "Ticker is required")
    @Size(min = 1, max = 10, message = "Ticker must be between 1 and 10 characters")
    private String ticker;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", inclusive = true, message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be positive")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fees cannot be negative")
    private BigDecimal fees;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private Boolean isFractional;
    private BigDecimal fractionalMultiplier;
    private Currency commissionCurrency;

    // Default constructor
    public CreateTransactionRequest() {
        this.isFractional = false;
        this.fractionalMultiplier = BigDecimal.ONE;
        this.commissionCurrency = null;
    }

    // Constructor with required fields
    public CreateTransactionRequest(String ticker, TransactionType transactionType, 
                                  BigDecimal quantity, BigDecimal price,
                                  Currency currency, LocalDate transactionDate) {
        this();
        this.ticker = ticker;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.fees = BigDecimal.ZERO;
    }

    // Getters and setters
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

    public Boolean getIsFractional() { return isFractional; }
    public void setIsFractional(Boolean isFractional) { this.isFractional = isFractional; }

    public BigDecimal getFractionalMultiplier() { return fractionalMultiplier; }
    public void setFractionalMultiplier(BigDecimal fractionalMultiplier) { this.fractionalMultiplier = fractionalMultiplier; }
    
    public Currency getCommissionCurrency() { return commissionCurrency; }
    public void setCommissionCurrency(Currency commissionCurrency) { this.commissionCurrency = commissionCurrency; }
} 