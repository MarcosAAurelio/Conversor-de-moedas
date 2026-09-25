package com.marcos.conversordemoedas.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.marcos.conversordemoedas.service.CurrencyConversionService;
import com.marcos.conversordemoedas.service.HistoryOwnerService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyConversionService conversionService;

    @MockitoBean
    private HistoryOwnerService historyOwnerService;

    @Test
    void shouldRenderDashboardAndHistory() throws Exception {
        when(conversionService.getSupportedCurrencies()).thenReturn(Set.of("BRL", "USD"));
        when(historyOwnerService.getOwnerId(org.mockito.ArgumentMatchers.any())).thenReturn("test-owner");
        when(conversionService.getHistory("test-owner")).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Content-Security-Policy", org.hamcrest.Matchers.containsString("object-src 'none'")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")))
                .andExpect(view().name("index"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Evolução da cotação")));

        mockMvc.perform(get("/historico"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Histórico de conversões")));

        mockMvc.perform(get("/sobre"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Marcos Aurélio")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://github.com/MarcosAAurelio")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://www.linkedin.com/in/eu-marcosaurelio-dev")));

        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejectsStateChangingRequestsWithoutCsrfToken() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/historico/limpar"))
                .andExpect(status().isForbidden());
    }
}
