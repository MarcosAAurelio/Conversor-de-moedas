package com.marcos.conversordemoedas.service;

import com.marcos.conversordemoedas.client.FrankfurterClient;
import com.marcos.conversordemoedas.dto.ConversionRequest;
import com.marcos.conversordemoedas.dto.ConversionResponse;
import com.marcos.conversordemoedas.dto.FrankfurterRateResponse;
import com.marcos.conversordemoedas.dto.HistoricalRateResponse;
import com.marcos.conversordemoedas.exception.InvalidConversionException;
import com.marcos.conversordemoedas.model.ConversionHistory;
import com.marcos.conversordemoedas.repository.ConversionHistoryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrencyConversionService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("BRL", "USD", "EUR", "GBP", "JPY", "CAD", "ARS", "CNY");

    private final FrankfurterClient frankfurterClient;
    private final ConversionHistoryRepository historyRepository;

    public CurrencyConversionService(FrankfurterClient frankfurterClient, ConversionHistoryRepository historyRepository) {
        this.frankfurterClient = frankfurterClient;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public ConversionResponse convert(ConversionRequest request) {
        validate(request);

        String sourceCurrency = normalizeCurrency(request.getSourceCurrency());
        String targetCurrency = normalizeCurrency(request.getTargetCurrency());

        FrankfurterRateResponse rateResponse = frankfurterClient.getRate(sourceCurrency, targetCurrency);
        BigDecimal rate = rateResponse.rate();
        BigDecimal convertedAmount = request.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);

        ConversionHistory savedConversion = historyRepository.save(new ConversionHistory(
                request.getAmount().setScale(2, RoundingMode.HALF_UP),
                sourceCurrency,
                targetCurrency,
                rate,
                convertedAmount,
                rateResponse.date()
        ));

        return toResponse(savedConversion);
    }

    @Transactional(readOnly = true)
    public List<ConversionResponse> getHistory() {
        return historyRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void clearHistory() {
        historyRepository.deleteAll();
    }

    public Set<String> getSupportedCurrencies() {
        return new TreeSet<>(SUPPORTED_CURRENCIES);
    }

    public List<HistoricalRateResponse> getRateHistory(String sourceCurrency, String targetCurrency, int days) {
        validateCurrencies(sourceCurrency, targetCurrency);
        if (days != 7 && days != 30 && days != 90) {
            throw new InvalidConversionException("Período inválido. Use 7, 30 ou 90 dias.");
        }
        List<HistoricalRateResponse> rates = frankfurterClient.getHistoricalRates(
                normalizeCurrency(sourceCurrency), normalizeCurrency(targetCurrency), LocalDate.now().minusDays(days * 2L + 7));
        List<HistoricalRateResponse> sorted = rates.stream()
                .sorted(Comparator.comparing(HistoricalRateResponse::date))
                .toList();
        return sorted.subList(Math.max(0, sorted.size() - days), sorted.size());
    }

    private void validate(ConversionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidConversionException("O valor deve ser maior que zero.");
        }

        validateCurrencies(request.getSourceCurrency(), request.getTargetCurrency());
    }

    private void validateCurrencies(String source, String target) {
        String sourceCurrency = normalizeCurrency(source);
        String targetCurrency = normalizeCurrency(target);

        if (!SUPPORTED_CURRENCIES.contains(sourceCurrency)) {
            throw new InvalidConversionException("Moeda de origem inválida.");
        }

        if (!SUPPORTED_CURRENCIES.contains(targetCurrency)) {
            throw new InvalidConversionException("Moeda de destino inválida.");
        }

        if (sourceCurrency.equals(targetCurrency)) {
            throw new InvalidConversionException("A moeda de origem deve ser diferente da moeda de destino.");
        }
    }

    private String normalizeCurrency(String currency) {
        return currency == null ? "" : currency.trim().toUpperCase(Locale.ROOT);
    }

    private ConversionResponse toResponse(ConversionHistory history) {
        return new ConversionResponse(
                history.getId(),
                history.getOriginalAmount(),
                history.getSourceCurrency(),
                history.getTargetCurrency(),
                history.getRate(),
                history.getConvertedAmount(),
                history.getQuoteDate(),
                history.getCreatedAt()
        );
    }
}
