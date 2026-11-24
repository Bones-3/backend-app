package com.nativa.finanzas_service.controller;

import com.nativa.finanzas_service.dto.ComprobanteRequest;
import com.nativa.finanzas_service.dto.ComprobanteResponse;
import com.nativa.finanzas_service.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/finanzas/comprobantes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO')")
public class ComprobanteController {
    private final ComprobanteService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO')")
    public ResponseEntity<ComprobanteResponse> crear(@Valid @RequestBody ComprobanteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping("/pago/{pagoId}")
    public ComprobanteResponse porPago(@PathVariable Long pagoId) {
        return service.porPago(pagoId);
    }
}
