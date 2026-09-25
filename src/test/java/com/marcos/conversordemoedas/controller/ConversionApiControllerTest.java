package com.marcos.conversordemoedas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcos.conversordemoedas.dto.ConversionResponse;
import com.marcos.conversordemoedas.service.CurrencyConversionService;
import com.marcos.conversordemoedas.service.HistoryOwnerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ConversionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyConversionService conversionService;

    @MockitoBean
    private HistoryOwnerService historyOwnerService;

    @Test
    void shouldConvertCurrency() throws Exception {
        when(historyOwnerService.getOwnerId(any())).thenReturn("test-owner");
        when(conversionService.convert(any(), anyString())).thenReturn(new ConversionResponse(
                1L,
                new BigDecimal("100.00"),
                "BRL",
                "USD",
                new BigDecimal("0.180000"),
                new BigDecimal("18.00"),
                LocalDate.of(2026, 9, 21),
                LocalDateTime.of(2026, 9, 21, 10, 30)
        ));

        mockMvc.perform(post("/api/conversoes").with(csrf())
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

    @Test
    void shouldRejectAmountsOutsideStoragePrecision() throws Exception {
        mockMvc.perform(post("/api/conversoes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10000000000000.0000001,
                                  "sourceCurrency": "BRL",
                                  "targetCurrency": "USD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.amount").exists());
    }

    @Test
    void csrfTokenCanAuthorizeApiMutationAndIsNotCached() throws Exception {
        MvcResult tokenResult = mockMvc.perform(get("/api/security/csrf"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Cache-Control", "no-store"))
                .andReturn();
        String tokenBody = tokenResult.getResponse().getContentAsString();
        String csrfToken = com.jayway.jsonpath.JsonPath.read(tokenBody, "$.token");
        String csrfHeader = com.jayway.jsonpath.JsonPath.read(tokenBody, "$.headerName");
        MockHttpSession session = (MockHttpSession) tokenResult.getRequest().getSession(false);
        when(historyOwnerService.getOwnerId(any())).thenReturn("test-owner");

        mockMvc.perform(post("/api/conversoes")
                        .session(session)
                        .header(csrfHeader, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":100,"sourceCurrency":"BRL","targetCurrency":"USD"}
                                """))
                .andExpect(status().isCreated());
    }
}
