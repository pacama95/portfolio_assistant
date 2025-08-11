package com.portfolio.infrastructure.rest.dto;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    String ticker,
    TransactionType transactionType,
    BigDecimal quantity,
    BigDecimal price,
    BigDecimal fees,
    Currency currency,
    LocalDate transactionDate,
    String notes,
    Boolean isActive,
    BigDecimal totalValue,
    BigDecimal totalCost,
    Boolean isFractional,
    BigDecimal fractionalMultiplier,
    Currency commissionCurrency
) {

    public TransactionResponse {
        if (isFractional == null) {
            isFractional = false;
        }
        if (fractionalMultiplier == null) {
            fractionalMultiplier = BigDecimal.ONE;
        }
    }
} 