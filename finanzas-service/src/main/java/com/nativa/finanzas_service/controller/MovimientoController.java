package com.nativa.finanzas_service.controller;

import com.nativa.finanzas_service.dto.*;
import com.nativa.finanzas_service.model.TipoMovimiento;
import com.nativa.finanzas_service.service.MovimientoService;
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
@RequestMapping("/finanzas/movimientos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO')")
public class MovimientoController {
    private final MovimientoService service;

    @GetMapping
    public PaginaResponse<MovimientoResponse> buscar(
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20, sort = "fecha") Pageable pageable) {
        return PaginaResponse.from(service.buscar(tipo, desde, hasta, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO')")
    public ResponseEntity<MovimientoResponse> crear(@Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }
}
