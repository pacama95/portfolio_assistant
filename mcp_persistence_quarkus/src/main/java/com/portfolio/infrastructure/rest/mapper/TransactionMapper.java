package com.portfolio.infrastructure.rest.mapper;

import com.portfolio.domain.model.Transaction;
import com.portfolio.infrastructure.rest.dto.CreateTransactionRequest;
import com.portfolio.infrastructure.rest.dto.TransactionResponse;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "cdi")
public interface TransactionMapper {
    int MONETARY_SCALE = 4;
    int QUANTITY_SCALE = 6;
    RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Mapping(target = "quantity", expression = "java(normalizeQuantity(transaction.getQuantity()))")
    @Mapping(target = "price", expression = "java(normalizeMonetary(transaction.getPrice()))")
    @Mapping(target = "fees", expression = "java(normalizeMonetary(transaction.getFees()))")
    @Mapping(target = "totalValue", expression = "java(normalizeMonetary(transaction.getTotalValue()))")
    @Mapping(target = "totalCost", expression = "java(normalizeMonetary(transaction.getTotalCost()))")
    @Mapping(target = "fractionalMultiplier", expression = "java(normalizeMonetary(transaction.getFractionalMultiplier()))")
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "quantity", expression = "java(normalizeQuantity(createTransactionRequest.getQuantity()))")
    @Mapping(target = "price", expression = "java(normalizeMonetary(createTransactionRequest.getPrice()))")
    @Mapping(target = "fees", expression = "java(normalizeMonetary(createTransactionRequest.getFees()))")
    @Mapping(target = "fractionalMultiplier", expression = "java(normalizeMonetary(createTransactionRequest.getFractionalMultiplier()))")
    Transaction toTransaction(CreateTransactionRequest createTransactionRequest);

    // Normalization helpers
    default BigDecimal normalizeMonetary(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(MONETARY_SCALE, ROUNDING);
    }
    default BigDecimal normalizeQuantity(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(QUANTITY_SCALE, ROUNDING);
    }
} 