package com.marcos.conversordemoedas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcos.conversordemoedas.client.FrankfurterClient;
import com.marcos.conversordemoedas.dto.ConversionRequest;
import com.marcos.conversordemoedas.dto.ConversionResponse;
import com.marcos.conversordemoedas.dto.FrankfurterRateResponse;
import com.marcos.conversordemoedas.dto.HistoricalRateResponse;
import com.marcos.conversordemoedas.exception.ExternalRateApiException;
import com.marcos.conversordemoedas.exception.InvalidConversionException;
import com.marcos.conversordemoedas.model.ConversionHistory;
import com.marcos.conversordemoedas.repository.ConversionHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    @Mock
    private FrankfurterClient frankfurterClient;

    @Mock
    private ConversionHistoryRepository historyRepository;

    private CurrencyConversionService conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new CurrencyConversionService(frankfurterClient, historyRepository);
    }

    @Test
    void shouldConvertAndSaveHistory() {
        ConversionRequest request = new ConversionRequest(new BigDecimal("100.00"), "BRL", "USD");
        when(frankfurterClient.getRate("BRL", "USD")).thenReturn(new FrankfurterRateResponse(
                "BRL",
                "USD",
                LocalDate.of(2026, 9, 21),
                new BigDecimal("0.180000")
        ));
        when(historyRepository.save(any(ConversionHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversionResponse response = conversionService.convert(request);

        assertThat(response.convertedAmount()).isEqualByComparingTo("18.00");
        assertThat(response.rate()).isEqualByComparingTo("0.180000");
        assertThat(response.quoteDate()).isEqualTo(LocalDate.of(2026, 9, 21));

        ArgumentCaptor<ConversionHistory> captor = ArgumentCaptor.forClass(ConversionHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getSourceCurrency()).isEqualTo("BRL");
        assertThat(captor.getValue().getTargetCurrency()).isEqualTo("USD");
    }

    @Test
    void shouldRejectSameCurrencies() {
        ConversionRequest request = new ConversionRequest(new BigDecimal("50.00"), "EUR", "EUR");

        assertThatThrownBy(() -> conversionService.convert(request))
                .isInstanceOf(InvalidConversionException.class)
                .hasMessage("A moeda de origem deve ser diferente da moeda de destino.");

        verify(frankfurterClient, never()).getRate(any(), any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void shouldRejectUnsupportedCurrency() {
        ConversionRequest request = new ConversionRequest(new BigDecimal("50.00"), "BTC", "USD");

        assertThatThrownBy(() -> conversionService.convert(request))
                .isInstanceOf(InvalidConversionException.class)
                .hasMessage("Moeda de origem inválida.");
    }

    @Test
    void shouldPropagateExternalApiErrorsWithoutSaving() {
        ConversionRequest request = new ConversionRequest(new BigDecimal("50.00"), "USD", "BRL");
        when(frankfurterClient.getRate("USD", "BRL")).thenThrow(new ExternalRateApiException("Falha de comunicação com o serviço de cotações."));

        assertThatThrownBy(() -> conversionService.convert(request))
                .isInstanceOf(ExternalRateApiException.class);

        verify(historyRepository, never()).save(any());
    }

    @Test
    void shouldLoadRealHistoricalRatesWithoutSavingConversions() {
        List<HistoricalRateResponse> rates = List.of(new HistoricalRateResponse(
                LocalDate.of(2026, 9, 18), new BigDecimal("0.185")));
        when(frankfurterClient.getHistoricalRates(org.mockito.ArgumentMatchers.eq("BRL"),
                org.mockito.ArgumentMatchers.eq("USD"), any(LocalDate.class))).thenReturn(rates);

        assertThat(conversionService.getRateHistory("brl", "usd", 30)).isEqualTo(rates);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void shouldReturnLastSevenAvailableRatesInDateOrder() {
        List<HistoricalRateResponse> rates = new java.util.ArrayList<>(java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(day -> new HistoricalRateResponse(
                        LocalDate.of(2026, 9, day), new BigDecimal("0.18")))
                .toList());
        java.util.Collections.reverse(rates);
        when(frankfurterClient.getHistoricalRates(org.mockito.ArgumentMatchers.eq("BRL"),
                org.mockito.ArgumentMatchers.eq("USD"), any(LocalDate.class))).thenReturn(rates);

        List<HistoricalRateResponse> result = conversionService.getRateHistory("BRL", "USD", 7);

        assertThat(result).hasSize(7);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 9, 4));
        assertThat(result.get(6).date()).isEqualTo(LocalDate.of(2026, 9, 10));
    }

    @Test
    void shouldRejectUnsupportedChartPeriod() {
        assertThatThrownBy(() -> conversionService.getRateHistory("BRL", "USD", 365))
                .isInstanceOf(InvalidConversionException.class);
        verify(frankfurterClient, never()).getHistoricalRates(any(), any(), any());
    }
}
