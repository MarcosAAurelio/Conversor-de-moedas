package com.marcos.conversordemoedas.controller;

import com.marcos.conversordemoedas.dto.ConversionRequest;
import com.marcos.conversordemoedas.dto.ConversionResponse;
import com.marcos.conversordemoedas.service.CurrencyConversionService;
import com.marcos.conversordemoedas.service.HistoryOwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversoes")
@Tag(name = "Conversoes", description = "Operacoes de conversao de moedas e historico")
public class ConversionApiController {

    private final CurrencyConversionService conversionService;
    private final HistoryOwnerService historyOwnerService;

    public ConversionApiController(CurrencyConversionService conversionService, HistoryOwnerService historyOwnerService) {
        this.conversionService = conversionService;
        this.historyOwnerService = historyOwnerService;
    }

    @PostMapping
    @Operation(summary = "Converte moedas", description = "Busca a cotação de referência atual e salva a conversão no histórico.")
    public ResponseEntity<ConversionResponse> convert(@Valid @RequestBody ConversionRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversionService.convert(request, historyOwnerService.getOwnerId(httpRequest)));
    }

    @GetMapping("/historico")
    @Operation(summary = "Lista o historico", description = "Retorna as conversoes da sessao atual, da mais recente para a mais antiga.")
    public List<ConversionResponse> getHistory(HttpServletRequest request) {
        return conversionService.getHistory(historyOwnerService.getOwnerId(request));
    }

    @DeleteMapping("/historico")
    @Operation(summary = "Limpa o historico", description = "Remove todas as conversoes da sessao atual.")
    public ResponseEntity<Void> clearHistory(HttpServletRequest request) {
        conversionService.clearHistory(historyOwnerService.getOwnerId(request));
        return ResponseEntity.noContent().build();
    }
}
