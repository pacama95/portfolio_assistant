package com.portfolio.infrastructure.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.application.command.CreateTransactionCommand;
import com.portfolio.application.command.UpdateTransactionCommand;
import com.portfolio.application.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.portfolio.application.usecase.position.GetPositionUseCase;
import com.portfolio.application.usecase.position.RecalculatePositionUseCase;
import com.portfolio.application.usecase.position.UpdateMarketDataUseCase;
import com.portfolio.application.usecase.transaction.CreateTransactionUseCase;
import com.portfolio.application.usecase.transaction.DeleteTransactionUseCase;
import com.portfolio.application.usecase.transaction.GetTransactionUseCase;
import com.portfolio.application.usecase.transaction.UpdateTransactionUseCase;
import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.TransactionType;
import com.portfolio.util.StringUtils;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Singleton
public class PortfolioMcpServer {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CreateTransactionUseCase createTransactionUseCase;

    @Inject
    GetTransactionUseCase getTransactionUseCase;

    @Inject
    UpdateTransactionUseCase updateTransactionUseCase;

    @Inject
    DeleteTransactionUseCase deleteTransactionUseCase;

    @Inject
    GetPositionUseCase getPositionUseCase;

    @Inject
    UpdateMarketDataUseCase updateMarketDataUseCase;

    @Inject
    GetPortfolioSummaryUseCase getPortfolioSummaryUseCase;

    @Inject
    RecalculatePositionUseCase recalculatePositionUseCase;

    @Tool(description = "Create a new transaction in the portfolio.")
    public Uni<String> createTransaction(
            @ToolArg(description = "Stock ticker symbol") String ticker,
            @ToolArg(description = "Transaction type (BUY, SELL, DIVIDEND)") String type,
            @ToolArg(description = "Quantity of shares") Object quantity,
            @ToolArg(description = "Price per share") Object price,
            @ToolArg(description = "Fees paid per transaction", required = false) Object fees,
            @ToolArg(description = "Determine if this is an operation on a stock fraction (for fractional offerings)", required = false, defaultValue = "false") boolean isFractional,
            @ToolArg(description = "Fraction of the real stock option represented by this fractional offered option", required = false) Object fractionalMultiplier,
            @ToolArg(description = "Fees currency", required = false, defaultValue = "USD") Currency commissionCurrency,
            @ToolArg(description = "Transaction currency") Currency currency,
            @ToolArg(description = "Transaction date (YYYY-MM-DD)") String date,
            @ToolArg(description = "Transaction notes", required = false) String notes) {
        
        try {
            // TODO: Define default value converters for BigDecimal and Currency
            TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
            LocalDate transactionDate = LocalDate.parse(date);
            BigDecimal quantityBD = toBigDecimal(quantity);
            BigDecimal priceBD = toBigDecimal(price);
            BigDecimal feesBD = toBigDecimal(fees);
            BigDecimal fractionalMultiplierBD = toBigDecimal(fractionalMultiplier);

            CreateTransactionCommand command = new CreateTransactionCommand(
                    ticker,
                    transactionType,
                    quantityBD,
                    priceBD,
                    feesBD,
                    currency,
                    transactionDate,
                    notes,
                    isFractional,
                    fractionalMultiplierBD,
                    commissionCurrency
            );

            return createTransactionUseCase.execute(command)
                    .map(result -> {
                        try {
                            return objectMapper.writeValueAsString(result);
                        } catch (Exception e) {
                            throw new RuntimeException("Error serializing result", e);
                        }
                    }).onFailure()
                    .invoke(e -> Log.error("Error creating transaction with ticker %s".formatted(ticker), e))
                    .onFailure().transform(throwable -> new ToolCallException("Error creating transaction with ticker %s".formatted(ticker)));
        } catch (IllegalArgumentException e) {
            throw new ToolCallException("Validation error", e);
        }
    }

    @Tool(description = "Get a transaction by its ID.")
    public Uni<String> getTransaction(@ToolArg(description = "The ID of the transaction to retrieve (UUID format)") String transactionId) {
        return Uni.createFrom().item(() -> UUID.fromString(transactionId))
                .flatMap(trxId -> getTransactionUseCase.getById(trxId))
            .map(transaction -> {
                try {
                    return objectMapper.writeValueAsString(transaction);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing result", e);
                }
            })
            .onFailure().invoke(e -> Log.error("Error getting transaction with ID %s".formatted(transactionId), e))
            .onFailure().transform(throwable -> new ToolCallException("Error getting transaction with ID %s".formatted(transactionId)));
    }

    @Tool(description = "Update an existing transaction.")
    public Uni<String> updateTransaction(
            @ToolArg(description = "Transaction ID to update (UUID format)") String transactionId,
            @ToolArg(description = "Stock ticker symbol", required = false) String ticker,
            @ToolArg(description = "Transaction type (BUY, SELL, DIVIDEND)", required = false, defaultValue = "BUY") String type,
            @ToolArg(description = "Quantity of shares", required = false) Object quantity,
            @ToolArg(description = "Price per share", required = false) Object price,
            @ToolArg(description = "Fees paid per transaction", required = false) Object fees,
            @ToolArg(description = "Determine if this is an operation on a stock fraction (for fractional offerings)", required = false, defaultValue = "false") boolean isFractional,
            @ToolArg(description = "Fraction of the real stock option represented by this fractional offered option", required = false) Object fractionalMultiplier,
            @ToolArg(description = "Fees currency", required = false, defaultValue = "USD") Currency commissionCurrency,
            @ToolArg(description = "Transaction currency", required = false, defaultValue = "USD") Currency currency,
            @ToolArg(description = "Transaction date (YYYY-MM-DD)", required = false) String date,
            @ToolArg(description = "Transaction notes", required = false) String notes) {
        
        try {
            // TODO: Define default value converters for BigDecimal and Currency
            TransactionType transactionType = StringUtils.hasMeaningfulContent(date) ? TransactionType.valueOf(type.toUpperCase()) : null;
            LocalDate transactionDate = StringUtils.hasMeaningfulContent(date) ? LocalDate.parse(date) : null;
            BigDecimal quantityBD = toBigDecimal(quantity);
            BigDecimal priceBD = toBigDecimal(price);
            BigDecimal feesBD = toBigDecimal(fees);
            BigDecimal fractionalMultiplierBD = toBigDecimal(fractionalMultiplier);

            // TODO: Refactor this to avoid fetching the transaction twice
            return Uni.createFrom().item(() -> new UpdateTransactionCommand(
                            UUID.fromString(transactionId),
                            ticker,
                            transactionType,
                            quantityBD,
                            priceBD,
                            feesBD,
                            currency,
                            transactionDate,
                            notes,
                            isFractional,
                            fractionalMultiplierBD,
                            commissionCurrency)
                    )
                    .flatMap(updateTransactionCommand ->
                            updateTransactionUseCase.execute(updateTransactionCommand))
                    .map(result -> {
                        try {
                            return objectMapper.writeValueAsString(result);
                        } catch (Exception e) {
                            throw new RuntimeException("Error serializing result", e);
                        }
                    })
                    .onFailure().invoke(e -> Log.error("Error updating transaction with ID %s".formatted(transactionId), e))
                    .onFailure().transform(throwable -> new ToolCallException("Error updating transaction with ID %s".formatted(transactionId)));
        } catch (Exception e) {
            throw new ToolCallException("Validation error", e);
        }
    }

    @Tool(description = "Delete a transaction by ID.")
    public Uni<String> deleteTransaction(@ToolArg(description = "The ID of the transaction to delete (UUID format)") String transactionId) {
        return Uni.createFrom().item(() -> UUID.fromString(transactionId))
                .flatMap(trxId -> deleteTransactionUseCase.execute(trxId))
            .map(result -> {
                try {
                    return objectMapper.writeValueAsString(result);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing result", e);
                }
            })
            .onFailure().invoke(e -> Log.error("Error deleting transaction with ID %s".formatted(transactionId), e))
            .onFailure().transform(throwable -> new ToolCallException("Error deleting transaction with ID %s".formatted(transactionId)));
    }

    @Tool(description = "Get all transactions for a specific ticker.")
    public Uni<String> getTransactionsByTicker(@ToolArg(description = "Stock ticker symbol") String ticker) {
        return getTransactionUseCase.getByTicker(ticker)
            .collect().asList()
            .map(transactions -> {
                try {
                    return objectMapper.writeValueAsString(transactions);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing result", e);
                }
            })
            .onFailure().invoke(e -> Log.error("Error getting transactions for ticker %s".formatted(ticker), e))
            .onFailure().transform(throwable -> new ToolCallException("Error getting transactions for ticker %s".formatted(ticker)));
    }

    @Tool(description = "Get all current positions in the portfolio.")
    public Uni<String> getAllPositions() {
        return getPositionUseCase.getAll()
            .map(positions -> {
                try {
                    return objectMapper.writeValueAsString(positions);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing result", e);
                }
            })
            .onFailure().invoke(e -> Log.error("Error getting all positions", e))
            .onFailure().transform(throwable -> new ToolCallException("Error getting all positions"));
    }

    @Tool(description = "Get position details for a specific ticker.")
    public Uni<String> getPositionByTicker(@ToolArg(description = "Stock ticker symbol") String ticker) {
        return getPositionUseCase.getByTicker(ticker)
            .map(position -> {
                try {
                    return objectMapper.writeValueAsString(position);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing result", e);
                }
            })
            .onFailure().invoke(e -> Log.error("Error getting position for ticker %s".formatted(ticker), e))
            .onFailure().transform(throwable -> new ToolCallException("Error getting position for ticker %s".formatted(ticker)));
    }

    @Tool(description = "Update market data for a position.")
    public Uni<String> updateMarketData(
            @ToolArg(description = "Stock ticker symbol") String ticker,
            @ToolArg(description = "Current market price") Object currentPrice) {
        
        BigDecimal currentPriceBD = toBigDecimal(currentPrice);
        return updateMarketDataUseCase.execute(ticker, currentPriceBD)
            .map(result -> {
                try {
                    return objectMapper.writeValueAsString(result);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing result", e);
                }
            })
            .onFailure().invoke(e -> Log.error("Error updating market data for ticker %s".formatted(ticker), e))
            .onFailure().transform(throwable -> new ToolCallException("Error updating market data for ticker %s".formatted(ticker)));
    }

    @Tool(description = "Get portfolio summary with key metrics.")
    public Uni<String> getPortfolioSummary() {
        return getPortfolioSummaryUseCase.getPortfolioSummary()
            .map(summary -> {
                try {
                    return objectMapper.writeValueAsString(summary);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing result", e);
                }
            })
            .onFailure().invoke(e -> Log.error("Error getting portfolio summary", e))
            .onFailure().transform(throwable -> new ToolCallException("Error getting portfolio summary"));
    }

    @Tool(description = "Search transactions with multiple filters.")
    public Uni<String> searchTransactions(
            @ToolArg(description = "Stock ticker symbol", required = false) String ticker,
            @ToolArg(description = "Start date (YYYY-MM-DD)", required = false) String startDate,
            @ToolArg(description = "End date (YYYY-MM-DD)", required = false) String endDate,
            @ToolArg(description = "Transaction type", required = false) String type) {
        
        try {
            TransactionType transactionType = StringUtils.hasMeaningfulContent(type) ? TransactionType.valueOf(type.toUpperCase()) : null;
            LocalDate fromDate = StringUtils.hasMeaningfulContent(startDate) ? LocalDate.parse(startDate) : null;
            LocalDate toDate = StringUtils.hasMeaningfulContent(endDate) ? LocalDate.parse(endDate) : null;
            
            return getTransactionUseCase.searchTransactions(ticker, transactionType, fromDate, toDate)
                .collect().asList()
                .map(transactions -> {
                    try {
                        return objectMapper.writeValueAsString(transactions);
                    } catch (Exception e) {
                        throw new RuntimeException("Error serializing result", e);
                    }
                })
                .onFailure().invoke(e -> Log.error("Error searching transactions", e))
                .onFailure().transform(throwable -> new ToolCallException("Error searching transactions"));
        } catch (IllegalArgumentException e) {
            throw new ToolCallException("Validation error", e);
        }
    }

    @Tool(description = "Recalculate position for a specific ticker.")
    public Uni<String> recalculatePosition(@ToolArg(description = "Stock ticker symbol") String ticker) {
        return recalculatePositionUseCase.execute(ticker)
            .map(result -> {
                try {
                    return objectMapper.writeValueAsString(result);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing result", e);
                }
            })
            .onFailure().invoke(e -> Log.error("Error recalculating position for ticker %s".formatted(ticker), e))
            .onFailure().transform(throwable -> new ToolCallException("Error recalculating position for ticker %s".formatted(ticker)));
    }

    @Tool(description = "Recalculate all positions from transactions.")
    public Uni<String> recalculateAllPositions() {
        return Uni.createFrom().item("Recalculating all positions...")
            .onFailure().invoke(e -> Log.error("Error recalculating all positions", e))
            .onFailure().transform(throwable -> new ToolCallException("Error recalculating all positions"));
    }

    private BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> null;
            case BigDecimal bigDecimal -> bigDecimal;
            case Integer i -> new BigDecimal(i);
            case Long l -> new BigDecimal(l);
            case Double v -> BigDecimal.valueOf(v);
            case String s -> new BigDecimal(s);
            default -> throw new IllegalArgumentException("Cannot convert value to BigDecimal: " + value);
        };
    }
} 