package com.marcos.conversordemoedas.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class FrankfurterClientTest {

    @Test
    void shouldReadV2HistoricalRateArray() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.frankfurter.dev");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.frankfurter.dev/v2/rates?base=BRL&quotes=USD&from=2026-09-01"))
                .andRespond(withSuccess("""
                        [{"date":"2026-09-01","base":"BRL","quote":"USD","rate":0.185}]
                        """, MediaType.APPLICATION_JSON));

        FrankfurterClient client = new FrankfurterClient(builder.build());
        var history = client.getHistoricalRates("BRL", "USD", LocalDate.of(2026, 9, 1));

        assertThat(history).hasSize(1);
        assertThat(history.get(0).rate()).isEqualByComparingTo("0.185");
        server.verify();
    }
}
