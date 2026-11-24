package com.nativa.reserva_service.controller;

import com.nativa.reserva_service.dto.EspacioRequest;
import com.nativa.reserva_service.dto.EspacioResponse;
import com.nativa.reserva_service.service.EspacioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/reserva/espacios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO','VECINO')")
public class EspacioController {
    private final EspacioService espacioService;

    @GetMapping
    public List<EspacioResponse> getActivos() {
        return espacioService.getActivos();
    }

    @GetMapping("/all")
    public List<EspacioResponse> getAll() {
        return espacioService.getAll();
    }

    @GetMapping("/{id}")
    public EspacioResponse getById(@PathVariable Long id) {
        return espacioService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA','SECRETARIO')")
    public ResponseEntity<EspacioResponse> create(@Valid @RequestBody EspacioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(espacioService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTIVA','SECRETARIO')")
    public EspacioResponse update(@PathVariable Long id, @Valid @RequestBody EspacioRequest request) {
        return espacioService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTIVA','SECRETARIO')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        espacioService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
