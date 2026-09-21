package com.marcos.conversordemoedas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FrankfurterRateResponse(
        String base,
        String quote,
        LocalDate date,
        BigDecimal rate
) {
}
