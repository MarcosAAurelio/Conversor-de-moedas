package com.marcos.conversordemoedas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricalRateResponse(LocalDate date, BigDecimal rate) {
}
