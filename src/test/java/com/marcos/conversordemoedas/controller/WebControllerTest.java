package com.marcos.conversordemoedas.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.marcos.conversordemoedas.service.CurrencyConversionService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WebController.class)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyConversionService conversionService;

    @Test
    void shouldRenderDashboardAndHistory() throws Exception {
        when(conversionService.getSupportedCurrencies()).thenReturn(Set.of("BRL", "USD"));
        when(conversionService.getHistory()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
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
    }
}
