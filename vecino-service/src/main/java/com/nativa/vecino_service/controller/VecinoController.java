package com.nativa.vecino_service.controller;

import com.nativa.vecino_service.dto.PaginaResponse;
import com.nativa.vecino_service.dto.VecinoRequest;
import com.nativa.vecino_service.dto.VecinoResponse;
import com.nativa.vecino_service.dto.VecinoSyncRequest;
import com.nativa.vecino_service.model.Rol;
import com.nativa.vecino_service.service.VecinoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vecino/vecinos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO','VECINO')")
public class VecinoController {
    private final VecinoService vecinoService;

    @GetMapping
    public PaginaResponse<VecinoResponse> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Rol rol,
            @PageableDefault(size = 20, sort = { "apellidos", "id" }) Pageable pageable) {
        return PaginaResponse.from(vecinoService.buscar(texto, activo, rol, pageable));
    }

    @GetMapping("/{id}")
    public VecinoResponse getById(@PathVariable Long id) {
        return vecinoService.getVecinoById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA','SECRETARIO')")
    public ResponseEntity<VecinoResponse> create(@Valid @RequestBody VecinoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vecinoService.createVecino(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTIVA','SECRETARIO')")
    public VecinoResponse update(@PathVariable Long id, @Valid @RequestBody VecinoRequest request) {
        return vecinoService.updateVecino(id, request);
    }

    @PostMapping("/sincronizar")
    @PreAuthorize("hasAnyRole('DIRECTIVA','SECRETARIO')")
    public VecinoResponse sincronizar(@Valid @RequestBody VecinoSyncRequest request) {
        return vecinoService.sincronizar(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTIVA','SECRETARIO')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vecinoService.deleteVecino(id);
        return ResponseEntity.noContent().build();
    }
}
