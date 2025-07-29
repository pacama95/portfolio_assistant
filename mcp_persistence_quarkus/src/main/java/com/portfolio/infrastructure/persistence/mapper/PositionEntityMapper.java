package com.portfolio.infrastructure.persistence.mapper;

import com.portfolio.domain.model.Position;
import com.portfolio.infrastructure.persistence.entity.PositionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;

@Mapper(componentModel = "cdi")
public interface PositionEntityMapper {
    @Mapping(target = "currentQuantity", source = "totalQuantity")
    @Mapping(target = "avgCostPerShare", source = "averagePrice")
    @Mapping(target = "totalCostBasis", source = "totalCost")
    @Mapping(target = "primaryCurrency", source = "currency")
    @Mapping(target = "lastTransactionDate", source = "lastUpdated")
    @Mapping(target = "firstPurchaseDate", source = "lastUpdated")
    @Mapping(target = "currentMarketValue", source = "currentPrice")
    @Mapping(target = "unrealizedGainLoss", expression = "java(mapUnrealizedGainLoss(position))")
    PositionEntity toEntity(Position position);

    @Mapping(target = "totalQuantity", source = "currentQuantity")
    @Mapping(target = "averagePrice", source = "avgCostPerShare")
    @Mapping(target = "totalCost", source = "totalCostBasis")
    @Mapping(target = "currency", source = "primaryCurrency")
    @Mapping(target = "lastUpdated", source = "lastTransactionDate")
    Position toDomain(PositionEntity entity);

    default BigDecimal mapUnrealizedGainLoss(Position position) {
        if (position == null || position.getTotalQuantity() == null || position.getCurrentPrice() == null || position.getTotalCost() == null) {
            return null;
        }
        BigDecimal marketValue = position.getTotalQuantity().multiply(position.getCurrentPrice());
        BigDecimal costBasis = position.getTotalCost();
        return marketValue.subtract(costBasis);
    }
} 