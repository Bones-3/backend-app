package com.nativa.finanzas_service.controller;

import com.nativa.finanzas_service.dto.*;
import com.nativa.finanzas_service.model.EstadoCuotaVecino;
import com.nativa.finanzas_service.model.TipoCuota;
import com.nativa.finanzas_service.service.CuotaService;
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
import java.util.List;

@RestController
@RequestMapping("/finanzas/cuotas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO')")
public class CuotaController {
    private final CuotaService service;

    @GetMapping
    public PaginaResponse<CuotaResponse> buscar(
            @RequestParam(required = false) TipoCuota tipo,
            @RequestParam(required = false) Boolean activa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimientoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimientoHasta,
            @PageableDefault(size = 20, sort = "fechaVencimiento") Pageable pageable) {
        return PaginaResponse.from(service.buscar(tipo, activa, vencimientoDesde, vencimientoHasta, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO')")
    public ResponseEntity<CuotaResponse> crear(@Valid @RequestBody CuotaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PostMapping("/{id}/asignaciones")
    @PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO')")
    public List<CuotaVecinoResponse> asignar(
            @PathVariable Long id, @Valid @RequestBody AsignacionCuotaRequest request) {
        return service.asignar(id, request);
    }

    @GetMapping("/asignaciones")
    public PaginaResponse<CuotaVecinoResponse> asignaciones(
            @RequestParam(required = false) Long vecinoId,
            @RequestParam(required = false) EstadoCuotaVecino estado,
            @RequestParam(defaultValue = "false") boolean soloMorosas,
            @PageableDefault(size = 20, sort = "fechaAsignacion") Pageable pageable) {
        return PaginaResponse.from(service.buscarAsignaciones(vecinoId, estado, soloMorosas, pageable));
    }

    @GetMapping("/morosidad")
    public PaginaResponse<CuotaVecinoResponse> morosidad(
            @RequestParam(required = false) Long vecinoId,
            @PageableDefault(size = 20, sort = "fechaAsignacion") Pageable pageable) {
        return PaginaResponse.from(
                service.buscarAsignaciones(vecinoId, EstadoCuotaVecino.PENDIENTE, true, pageable));
    }
}
