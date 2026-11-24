package com.nativa.finanzas_service.controller;

import com.nativa.finanzas_service.dto.ReporteFinancieroResponse;
import com.nativa.finanzas_service.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/finanzas/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO')")
public class ReporteController {
    private final ReporteService service;

    @GetMapping
    public ReporteFinancieroResponse generar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.generar(desde, hasta);
    }
}
