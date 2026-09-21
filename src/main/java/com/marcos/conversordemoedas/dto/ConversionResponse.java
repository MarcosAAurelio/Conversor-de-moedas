package com.marcos.conversordemoedas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConversionResponse(
        Long id,
        BigDecimal originalAmount,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal rate,
        BigDecimal convertedAmount,
        LocalDate quoteDate,
        LocalDateTime createdAt
) {
}
