package com.marcos.conversordemoedas.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcos.conversordemoedas.dto.HistoricalRateResponse;
import com.marcos.conversordemoedas.service.CurrencyConversionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RateHistoryController.class)
class RateHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyConversionService conversionService;

    @Test
    void acceptsPortugueseParametersAndPreservesExistingParameters() throws Exception {
        when(conversionService.getRateHistory("BRL", "USD", 7)).thenReturn(List.of(
                new HistoricalRateResponse(LocalDate.of(2026, 9, 21), new BigDecimal("0.18"))));

        mockMvc.perform(get("/api/cotacoes/historico")
                        .param("origem", "BRL").param("destino", "USD").param("dias", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rate").value(0.18));
        mockMvc.perform(get("/api/cotacoes/historico")
                        .param("sourceCurrency", "BRL").param("targetCurrency", "USD").param("days", "7"))
                .andExpect(status().isOk());
        verify(conversionService, org.mockito.Mockito.times(2)).getRateHistory("BRL", "USD", 7);
    }
}
