package com.portfolio.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * REST DTO for updating market data
 */
public class UpdateMarketDataRequest {

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be positive")
    private BigDecimal price;

    // Default constructor
    public UpdateMarketDataRequest() {}

    public UpdateMarketDataRequest(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
} 