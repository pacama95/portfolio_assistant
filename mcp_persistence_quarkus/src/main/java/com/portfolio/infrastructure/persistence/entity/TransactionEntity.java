package com.portfolio.infrastructure.persistence.entity;

import com.portfolio.domain.model.Currency;
import com.portfolio.domain.model.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for the transactions table
 */
@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_transactions_ticker", columnList = "ticker"),
        @Index(name = "idx_transactions_date", columnList = "transaction_date"),
        @Index(name = "idx_transactions_ticker_date", columnList = "ticker,transaction_date")
    }
)
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, columnDefinition = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;

    @Column(name = "cost_per_share", nullable = false, precision = 18, scale = 4)
    private BigDecimal costPerShare;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, columnDefinition = "currency_type")
    private Currency currency;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "commission", precision = 18, scale = 4)
    private BigDecimal commission;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_currency", columnDefinition = "currency_type")
    private Currency commissionCurrency;

    @Column(name = "drip_confirmed")
    private Boolean dripConfirmed = false;

    @Column(name = "is_fractional")
    private Boolean isFractional = false;

    @Column(name = "fractional_multiplier", precision = 10, scale = 8)
    private BigDecimal fractionalMultiplier = BigDecimal.ONE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Default constructor
    public TransactionEntity() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
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

    public BigDecimal getCostPerShare() { return costPerShare; }
    public void setCostPerShare(BigDecimal costPerShare) { this.costPerShare = costPerShare; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getCommission() { return commission; }
    public void setCommission(BigDecimal commission) { this.commission = commission; }

    public Currency getCommissionCurrency() { return commissionCurrency; }
    public void setCommissionCurrency(Currency commissionCurrency) { this.commissionCurrency = commissionCurrency; }

    public Boolean getDripConfirmed() { return dripConfirmed; }
    public void setDripConfirmed(Boolean dripConfirmed) { this.dripConfirmed = dripConfirmed; }

    public Boolean getIsFractional() { return isFractional; }
    public void setIsFractional(Boolean isFractional) { this.isFractional = isFractional; }

    public BigDecimal getFractionalMultiplier() { return fractionalMultiplier; }
    public void setFractionalMultiplier(BigDecimal fractionalMultiplier) { this.fractionalMultiplier = fractionalMultiplier; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
} 