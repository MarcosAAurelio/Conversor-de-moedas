package com.marcos.conversordemoedas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public class ConversionRequest {

    @NotNull(message = "Informe um valor para converter.")
    @Positive(message = "O valor deve ser maior que zero.")
    @Digits(integer = 13, fraction = 6, message = "Use no máximo 13 dígitos inteiros e 6 casas decimais.")
    private BigDecimal amount;

    @NotBlank(message = "Selecione a moeda de origem.")
    private String sourceCurrency = "USD";

    @NotBlank(message = "Selecione a moeda de destino.")
    private String targetCurrency = "BRL";

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
