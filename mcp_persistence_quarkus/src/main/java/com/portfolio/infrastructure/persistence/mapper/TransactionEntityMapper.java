package com.portfolio.infrastructure.persistence.mapper;

import com.portfolio.domain.model.Transaction;
import com.portfolio.infrastructure.persistence.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface TransactionEntityMapper {
    @Mapping(target = "costPerShare", source = "price")
    @Mapping(target = "commission", source = "fees")
    @Mapping(target = "commissionCurrency", source = "commissionCurrency")
    TransactionEntity toEntity(Transaction transaction);

    @Mapping(target = "price", source = "costPerShare")
    @Mapping(target = "fees", source = "commission")
    @Mapping(target = "commissionCurrency", source = "commissionCurrency")
    Transaction toDomain(TransactionEntity entity);
} 