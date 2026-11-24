package com.nativa.finanzas_service.service;

import com.nativa.finanzas_service.dto.PagoRequest;
import com.nativa.finanzas_service.dto.PagoResponse;
import com.nativa.finanzas_service.exception.BadRequestException;
import com.nativa.finanzas_service.exception.ResourceNotFoundException;
import com.nativa.finanzas_service.mapper.PagoMapper;
import com.nativa.finanzas_service.model.*;
import com.nativa.finanzas_service.repository.CuotaVecinoRepository;
import com.nativa.finanzas_service.repository.MovimientoRepository;
import com.nativa.finanzas_service.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {
    private final PagoRepository pagoRepository;
    private final CuotaVecinoRepository cuotaVecinoRepository;
    private final MovimientoRepository movimientoRepository;
    private final PagoMapper pagoMapper;

    @Transactional(readOnly = true)
    public List<PagoResponse> getAllPagos() {
        return pagoRepository.findAll().stream().map(pagoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<PagoResponse> buscar(Long vecinoId, LocalDate desde, LocalDate hasta, Pageable pageable) {
        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fin = hasta == null ? null : hasta.atTime(LocalTime.MAX);
        return pagoRepository.buscar(vecinoId, inicio, fin, pageable).map(pagoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PagoResponse findById(Long id) {
        return pagoRepository.findById(id).map(pagoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
    }

    @Transactional
    public PagoResponse createPago(PagoRequest request) {
        CuotaVecino asignacion = cuotaVecinoRepository.findById(request.getCuotaVecinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de cuota no encontrada"));
        BigDecimal saldo = asignacion.getCuota().getMonto().subtract(asignacion.getMontoPagado());
        if (saldo.signum() <= 0 || asignacion.getEstado() == EstadoCuotaVecino.PAGADA) {
            throw new BadRequestException("La cuota ya está pagada");
        }
        BigDecimal monto = request.getMonto() == null ? saldo : request.getMonto();
        if (monto.compareTo(saldo) > 0) {
            throw new BadRequestException("El monto supera el saldo pendiente");
        }

        Pago pago = new Pago();
        pago.setCuotaVecino(asignacion);
        pago.setVecino_id(asignacion.getVecinoId());
        pago.setTotal(monto);
        pago.setMetodoPago(request.getMetodoPago());
        pago.setFechaPago(LocalDateTime.now());
        Pago guardado = pagoRepository.save(pago);

        asignacion.setMontoPagado(asignacion.getMontoPagado().add(monto));
        if (asignacion.getMontoPagado().compareTo(asignacion.getCuota().getMonto()) >= 0) {
            asignacion.setEstado(EstadoCuotaVecino.PAGADA);
        }
        cuotaVecinoRepository.save(asignacion);

        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(TipoMovimiento.INGRESO);
        movimiento.setCategoria("CUOTA");
        movimiento.setDescripcion("Pago de cuota: " + asignacion.getCuota().getNombre());
        movimiento.setMonto(monto);
        movimiento.setFecha(pago.getFechaPago().toLocalDate());
        movimiento.setPago(guardado);
        movimientoRepository.save(movimiento);

        return pagoMapper.toResponse(guardado);
    }
}
