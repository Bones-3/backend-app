package com.nativa.reserva_service.controller;

import com.nativa.reserva_service.dto.ReservaRequest;
import com.nativa.reserva_service.dto.ReservaResponse;
import com.nativa.reserva_service.dto.AppReservaRequest;
import com.nativa.reserva_service.dto.AppReservaResponse;
import com.nativa.reserva_service.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.nativa.reserva_service.dto.PaginaResponse;
import com.nativa.reserva_service.model.EstadoReserva;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/reserva/reservas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO','VECINO')")
public class ReservaController {
    private final ReservaService reservaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA','TESORERO','SECRETARIO')")
    public PaginaResponse<ReservaResponse> getAll(
            @RequestParam(required = false) Long vecinoId,
            @RequestParam(required = false) Long espacioId,
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 20, sort = "fecha") Pageable pageable) {
        return PaginaResponse.from(
                reservaService.buscar(vecinoId, espacioId, estado, fechaDesde, fechaHasta, pageable));
    }

    @GetMapping("/{id}")
    public ReservaResponse getById(@PathVariable Long id, Authentication authentication) {
        ReservaResponse reserva = reservaService.getById(id);
        autorizarPropietario(reserva.getVecinoId(), authentication);
        return reserva;
    }

    @GetMapping("/app")
    public List<AppReservaResponse> getForApp(
            @RequestParam(required = false) Long usuarioId, Authentication authentication) {
        return reservaService.findForApp(resolverUsuarioId(usuarioId, authentication));
    }

    @PostMapping("/app")
    public ResponseEntity<AppReservaResponse> createFromApp(
            @Valid @RequestBody AppReservaRequest request, Authentication authentication) {
        request.setUsuarioId(resolverUsuarioId(request.getUsuarioId(), authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.createFromApp(request));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA','SECRETARIO')")
    public ResponseEntity<ReservaResponse> create(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.create(request));
    }

    @PutMapping("/{id}")
    public ReservaResponse update(@PathVariable Long id, @Valid @RequestBody ReservaRequest request,
            Authentication authentication) {
        ReservaResponse existente = reservaService.getById(id);
        autorizarPropietario(existente.getVecinoId(), authentication);
        // El cambio de dueño requiere una operación administrativa explícita.
        request.setVecinoId(existente.getVecinoId());
        return reservaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication authentication) {
        autorizarPropietario(reservaService.getById(id).getVecinoId(), authentication);
        reservaService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * El usuario sale del token (claim vecinoId), no de lo que envia el cliente: un
     * VECINO no puede ver ni crear
     * reservas a nombre de otro. DIRECTIVA y SECRETARIO pueden indicar un usuarioId
     * distinto.
     */
    private Long resolverUsuarioId(Long solicitado, Authentication authentication) {
        boolean administra = administra(authentication);
        if (administra && solicitado != null) {
            return solicitado;
        }
        if (authentication.getDetails() instanceof Long propio) {
            return propio;
        }
        throw new AccessDeniedException("El token no identifica al vecino. Inicie sesión nuevamente.");
    }

    private void autorizarPropietario(Long propietarioId, Authentication authentication) {
        if (!administra(authentication) && !propietarioId.equals(resolverUsuarioId(null, authentication))) {
            throw new AccessDeniedException("No puede acceder a esta reserva");
        }
    }

    private boolean administra(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_DIRECTIVA") || a.getAuthority().equals("ROLE_SECRETARIO"));
    }
}
