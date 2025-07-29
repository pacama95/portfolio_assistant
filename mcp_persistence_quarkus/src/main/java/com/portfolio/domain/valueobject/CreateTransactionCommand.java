package com.portfolio.domain.valueobject;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable value object for creating transactions
 */
public record CreateTransactionCommand(
    String ticker,
    TransactionType transactionType,
    BigDecimal quantity,
    BigDecimal price,
    BigDecimal fees,
    Currency currency,
    LocalDate transactionDate,
    String notes,
    Boolean isFractional, // true if fractional
    BigDecimal fractionalMultiplier, // precision 10, scale 8
    Currency commissionCurrency // commission currency
) {
    
    public CreateTransactionCommand {
        // Basic validation
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (transactionDate == null) {
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
    }

    /**
     * Static factory method for creating a command
     */
    public static CreateTransactionCommand of(String ticker, TransactionType transactionType,
                                            BigDecimal quantity, BigDecimal price,
                                            Currency currency, LocalDate transactionDate) {
        return new CreateTransactionCommand(ticker, transactionType, quantity, price,
                                          BigDecimal.ZERO, currency, transactionDate, null,
                                          false, BigDecimal.ONE, null);
    }
} 