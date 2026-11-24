package com.nativa.finanzas_service.service;

import com.nativa.finanzas_service.dto.MovimientoRequest;
import com.nativa.finanzas_service.dto.MovimientoResponse;
import com.nativa.finanzas_service.model.Movimiento;
import com.nativa.finanzas_service.model.TipoMovimiento;
import com.nativa.finanzas_service.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MovimientoService {
    private final MovimientoRepository repository;

    @Transactional
    public MovimientoResponse crear(MovimientoRequest request) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(request.getTipo());
        movimiento.setCategoria(request.getCategoria());
        movimiento.setDescripcion(request.getDescripcion());
        movimiento.setMonto(request.getMonto());
        movimiento.setFecha(request.getFecha());
        return toResponse(repository.save(movimiento));
    }

    @Transactional(readOnly = true)
    public Page<MovimientoResponse> buscar(
            TipoMovimiento tipo, LocalDate desde, LocalDate hasta, Pageable pageable) {
        return repository.buscar(tipo, desde, hasta, pageable).map(this::toResponse);
    }

    private MovimientoResponse toResponse(Movimiento movimiento) {
        return MovimientoResponse.builder()
                .id(movimiento.getId()).tipo(movimiento.getTipo())
                .categoria(movimiento.getCategoria()).descripcion(movimiento.getDescripcion())
                .monto(movimiento.getMonto()).fecha(movimiento.getFecha())
                .pagoId(movimiento.getPago() == null ? null : movimiento.getPago().getId())
                .build();
    }
}
