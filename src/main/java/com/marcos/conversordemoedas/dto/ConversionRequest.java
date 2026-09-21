package com.marcos.conversordemoedas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ConversionRequest {

    @NotNull(message = "Informe um valor para converter.")
    @Positive(message = "O valor deve ser maior que zero.")
    private BigDecimal amount;

    @NotBlank(message = "Selecione a moeda de origem.")
    private String sourceCurrency = "BRL";

    @NotBlank(message = "Selecione a moeda de destino.")
    private String targetCurrency = "USD";

    public ConversionRequest() {
    }

    public ConversionRequest(BigDecimal amount, String sourceCurrency, String targetCurrency) {
        this.amount = amount;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }
}
