package com.portfolio.infrastructure.rest.dto;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTransactionRequest(
    @NotNull(message = "Ticker is required")
    @Size(min = 1, max = 10, message = "Ticker must be between 1 and 10 characters")
    String ticker,

    @NotNull(message = "Transaction type is required")
    TransactionType transactionType,

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", inclusive = true, message = "Quantity must be positive")
    BigDecimal quantity,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be positive")
    BigDecimal price,

    @DecimalMin(value = "0.0", inclusive = true, message = "Fees cannot be negative")
    BigDecimal fees,

    @NotNull(message = "Currency is required")
    Currency currency,

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    LocalDate transactionDate,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes,

    Boolean isFractional,
    BigDecimal fractionalMultiplier,
    Currency commissionCurrency
) {
} 