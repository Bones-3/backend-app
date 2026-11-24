package com.nativa.finanzas_service.controller;

import com.nativa.finanzas_service.dto.PaginaResponse;
import com.nativa.finanzas_service.dto.PagoRequest;
import com.nativa.finanzas_service.dto.PagoResponse;
import com.nativa.finanzas_service.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/finanzas/pagos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO')")
public class PagoController {
    private final PagoService pagoService;

    @GetMapping
    public PaginaResponse<PagoResponse> buscar(
            @RequestParam(required = false) Long vecinoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20, sort = "fechaPago") Pageable pageable) {
        return PaginaResponse.from(pagoService.buscar(vecinoId, desde, hasta, pageable));
    }

    @GetMapping("/{id}")
    public PagoResponse getById(@PathVariable Long id) {
        return pagoService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO')")
    public ResponseEntity<PagoResponse> create(@Valid @RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.createPago(request));
    }
}
