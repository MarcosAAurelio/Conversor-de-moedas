package com.marcos.conversordemoedas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversion_history")
public class ConversionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal originalAmount;

    @Column(nullable = false, length = 3)
    private String sourceCurrency;

    @Column(nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal convertedAmount;

    @Column(nullable = false)
    private LocalDate quoteDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 36)
    private String ownerId;

    public ConversionHistory() {
    }

    public ConversionHistory(
            BigDecimal originalAmount,
            String sourceCurrency,
            String targetCurrency,
            BigDecimal rate,
            BigDecimal convertedAmount,
            LocalDate quoteDate,
            String ownerId
    ) {
        this.originalAmount = originalAmount;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.convertedAmount = convertedAmount;
        this.quoteDate = quoteDate;
        this.ownerId = ownerId;
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public LocalDate getQuoteDate() {
        return quoteDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
