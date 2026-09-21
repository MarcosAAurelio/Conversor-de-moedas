package com.marcos.conversordemoedas.controller;

import com.marcos.conversordemoedas.dto.HistoricalRateResponse;
import com.marcos.conversordemoedas.service.CurrencyConversionService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cotacoes")
public class RateHistoryController {

    private final CurrencyConversionService conversionService;

    public RateHistoryController(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/historico")
    @Operation(summary = "Histórico de cotações", description = "Cotações reais dos últimos 7, 30 ou 90 dias.")
    public List<HistoricalRateResponse> getHistory(
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) Integer dias,
            @RequestParam(required = false) String sourceCurrency,
            @RequestParam(required = false) String targetCurrency,
            @RequestParam(required = false) Integer days) {
        return conversionService.getRateHistory(
                origem != null ? origem : sourceCurrency != null ? sourceCurrency : "BRL",
                destino != null ? destino : targetCurrency != null ? targetCurrency : "USD",
                dias != null ? dias : days != null ? days : 7);
    }
}
