package com.marcos.conversordemoedas.client;

import com.marcos.conversordemoedas.dto.FrankfurterRateResponse;
import com.marcos.conversordemoedas.dto.HistoricalRateResponse;
import com.marcos.conversordemoedas.exception.ExternalRateApiException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class FrankfurterClient {

    private final RestClient frankfurterRestClient;

    public FrankfurterClient(RestClient frankfurterRestClient) {
        this.frankfurterRestClient = frankfurterRestClient;
    }

    public FrankfurterRateResponse getRate(String sourceCurrency, String targetCurrency) {
        try {
            FrankfurterRateResponse response = frankfurterRestClient.get()
                    .uri("/v2/rate/{sourceCurrency}/{targetCurrency}", sourceCurrency, targetCurrency)
                    .retrieve()
                    .body(FrankfurterRateResponse.class);

            if (response == null || response.rate() == null || response.date() == null) {
                throw new ExternalRateApiException("O serviço de cotações retornou uma resposta inválida.");
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw new ExternalRateApiException("Não foi possível obter a cotação no momento.", exception);
        } catch (RestClientException exception) {
            throw new ExternalRateApiException("Falha de comunicação com o serviço de cotações.", exception);
        }
    }

    public List<HistoricalRateResponse> getHistoricalRates(String sourceCurrency, String targetCurrency, LocalDate from) {
        try {
            FrankfurterRateResponse[] response = frankfurterRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v2/rates")
                            .queryParam("base", sourceCurrency)
                            .queryParam("quotes", targetCurrency)
                            .queryParam("from", from)
                            .build())
                    .retrieve()
                    .body(FrankfurterRateResponse[].class);

            if (response == null) {
                throw new ExternalRateApiException("Não foi possível carregar o histórico de cotações.");
            }
            return Arrays.stream(response)
                    .filter(item -> item != null && item.date() != null && item.rate() != null)
                    .map(item -> new HistoricalRateResponse(item.date(), item.rate()))
                    .toList();
        } catch (RestClientException exception) {
            throw new ExternalRateApiException("Não foi possível carregar o histórico de cotações.", exception);
        }
    }
}
