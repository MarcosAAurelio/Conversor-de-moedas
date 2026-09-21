package com.marcos.conversordemoedas.controller;

import com.marcos.conversordemoedas.dto.ConversionRequest;
import com.marcos.conversordemoedas.exception.ConversionException;
import com.marcos.conversordemoedas.service.CurrencyConversionService;
import jakarta.validation.Valid;
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

    public WebController(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @ModelAttribute("currencies")
    public Iterable<String> currencies() {
        return conversionService.getSupportedCurrencies();
    }

    @GetMapping("/")
    public String home(Model model) {
        if (!model.containsAttribute("conversionRequest")) {
            model.addAttribute("conversionRequest", new ConversionRequest());
        }
        model.addAttribute("recentHistory", conversionService.getHistory().stream().limit(3).toList());
        return "index";
    }

    @PostMapping("/converter")
    public String convert(
            @Valid @ModelAttribute("conversionRequest") ConversionRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("recentHistory", conversionService.getHistory().stream().limit(3).toList());
        if (bindingResult.hasErrors()) {
            return "index";
        }

        try {
            model.addAttribute("conversionResult", conversionService.convert(request));
            model.addAttribute("recentHistory", conversionService.getHistory().stream().limit(3).toList());
        } catch (ConversionException exception) {
            model.addAttribute("conversionError", exception.getMessage());
        }

        return "index";
    }

    @GetMapping("/historico")
    public String history(Model model) {
        model.addAttribute("history", conversionService.getHistory());
        return "history";
    }

    @GetMapping("/sobre")
    public String about() {
        return "about";
    }

    @PostMapping("/historico/limpar")
    public String clearHistory(RedirectAttributes redirectAttributes) {
        conversionService.clearHistory();
        redirectAttributes.addFlashAttribute("message", "Historico limpo com sucesso.");
        return "redirect:/historico";
    }
}
