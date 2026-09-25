package com.marcos.conversordemoedas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.marcos.conversordemoedas.model.ConversionHistory;
import com.marcos.conversordemoedas.repository.ConversionHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConversorDeMoedasApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversionHistoryRepository historyRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void h2ConsoleIsNotMapped() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isNotFound());
    }

    @Test
    void exposesHealthWithoutDetailsAndKeepsMetricsEndpointPrivate() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void historyQueriesAndDeletesAreScopedToOneSessionOwner() {
        historyRepository.saveAndFlush(conversion("owner-one"));
        historyRepository.saveAndFlush(conversion("owner-two"));

        assertThat(historyRepository.findTop100ByOwnerIdOrderByCreatedAtDescIdDesc("owner-one"))
                .hasSize(1);
        assertThat(historyRepository.deleteByOwnerId("owner-one")).isEqualTo(1);
        assertThat(historyRepository.findTop100ByOwnerIdOrderByCreatedAtDescIdDesc("owner-one"))
                .isEmpty();
        assertThat(historyRepository.findTop100ByOwnerIdOrderByCreatedAtDescIdDesc("owner-two"))
                .hasSize(1);
    }

    private ConversionHistory conversion(String ownerId) {
        return new ConversionHistory(
                new BigDecimal("100.00"),
                "BRL",
                "USD",
                new BigDecimal("0.18"),
                new BigDecimal("18.00"),
                LocalDate.of(2026, 9, 24),
                ownerId
        );
    }
}
