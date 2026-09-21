package com.marcos.conversordemoedas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcos.conversordemoedas.dto.ConversionResponse;
import com.marcos.conversordemoedas.exception.GlobalExceptionHandler;
import com.marcos.conversordemoedas.service.CurrencyConversionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ConversionApiController.class)
@Import(GlobalExceptionHandler.class)
class ConversionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyConversionService conversionService;

    @Test
    void shouldConvertCurrency() throws Exception {
        when(conversionService.convert(any())).thenReturn(new ConversionResponse(
                1L,
                new BigDecimal("100.00"),
                "BRL",
                "USD",
                new BigDecimal("0.180000"),
                new BigDecimal("18.00"),
                LocalDate.of(2026, 9, 21),
                LocalDateTime.of(2026, 9, 21, 10, 30)
        ));

        mockMvc.perform(post("/api/conversoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "sourceCurrency": "BRL",
                                  "targetCurrency": "USD"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceCurrency").value("BRL"))
                .andExpect(jsonPath("$.targetCurrency").value("USD"))
                .andExpect(jsonPath("$.convertedAmount").value(18.00));
    }
}
