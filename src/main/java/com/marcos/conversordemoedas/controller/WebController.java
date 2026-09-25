package com.marcos.conversordemoedas.controller;

import com.marcos.conversordemoedas.dto.ConversionRequest;
import com.marcos.conversordemoedas.exception.ConversionException;
import com.marcos.conversordemoedas.service.CurrencyConversionService;
import com.marcos.conversordemoedas.service.HistoryOwnerService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

    private final CurrencyConversionService conversionService;
    private final HistoryOwnerService historyOwnerService;
    private final boolean apiDocsEnabled;

    public WebController(
            CurrencyConversionService conversionService,
            HistoryOwnerService historyOwnerService,
            @Value("${api.docs.enabled:true}") boolean apiDocsEnabled
    ) {
        this.conversionService = conversionService;
        this.historyOwnerService = historyOwnerService;
        this.apiDocsEnabled = apiDocsEnabled;
    }

    @ModelAttribute("currencies")
    public Iterable<String> currencies() {
        return conversionService.getSupportedCurrencies();
    }

    @ModelAttribute("apiDocsEnabled")
    public boolean apiDocsEnabled() {
        return apiDocsEnabled;
    }

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("conversionRequest")) {
            model.addAttribute("conversionRequest", new ConversionRequest());
        }
        model.addAttribute("recentHistory", conversionService.getHistory(historyOwnerService.getOwnerId(request)).stream().limit(3).toList());
        return "index";
    }

    @PostMapping("/converter")
    public String convert(
            @Valid @ModelAttribute("conversionRequest") ConversionRequest request,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest httpRequest
    ) {
        String ownerId = historyOwnerService.getOwnerId(httpRequest);
        model.addAttribute("recentHistory", conversionService.getHistory(ownerId).stream().limit(3).toList());
        if (bindingResult.hasErrors()) {
            return "index";
        }

        try {
            model.addAttribute("conversionResult", conversionService.convert(request, ownerId));
            model.addAttribute("recentHistory", conversionService.getHistory(ownerId).stream().limit(3).toList());
        } catch (ConversionException exception) {
            model.addAttribute("conversionError", exception.getMessage());
        }

        return "index";
    }

    @GetMapping("/historico")
    public String history(Model model, HttpServletRequest request) {
        model.addAttribute("history", conversionService.getHistory(historyOwnerService.getOwnerId(request)));
        return "history";
    }

    @GetMapping("/sobre")
    public String about() {
        return "about";
    }

    @PostMapping("/historico/limpar")
    public String clearHistory(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        conversionService.clearHistory(historyOwnerService.getOwnerId(request));
        redirectAttributes.addFlashAttribute("message", "Historico limpo com sucesso.");
        return "redirect:/historico";
    }
}
