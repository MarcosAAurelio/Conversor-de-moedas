package com.marcos.conversordemoedas.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HistoryCleanupJob {

    private final CurrencyConversionService conversionService;

    public HistoryCleanupJob(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Scheduled(cron = "0 17 3 * * *")
    public void cleanupExpiredHistory() {
        conversionService.cleanupExpiredHistory();
    }
}
