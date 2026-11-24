package com.nativa.finanzas_service.service;

import com.nativa.finanzas_service.client.VecinoClient;
import com.nativa.finanzas_service.dto.*;
import com.nativa.finanzas_service.exception.BadRequestException;
import com.nativa.finanzas_service.exception.ResourceNotFoundException;
import com.nativa.finanzas_service.model.*;
import com.nativa.finanzas_service.repository.CuotaRepository;
import com.nativa.finanzas_service.repository.CuotaVecinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuotaService {
    private final CuotaRepository cuotaRepository;
    private final CuotaVecinoRepository cuotaVecinoRepository;
    private final VecinoClient vecinoClient;

    @Transactional
    public CuotaResponse crear(CuotaRequest request) {
        Cuota cuota = new Cuota();
        cuota.setNombre(request.getNombre());
        cuota.setDescripcion(request.getDescripcion());
        cuota.setTipo(request.getTipo());
        cuota.setMonto(request.getMonto());
        cuota.setFechaVencimiento(request.getFechaVencimiento());
        cuota.setActiva(request.getActiva() == null ? true : request.getActiva());
        cuota.setFechaCreacion(LocalDateTime.now());
        return toResponse(cuotaRepository.save(cuota));
    }

    @Transactional(readOnly = true)
    public Page<CuotaResponse> buscar(
            TipoCuota tipo, Boolean activa, LocalDate vencimientoDesde,
            LocalDate vencimientoHasta, Pageable pageable) {
        Specification<Cuota> spec = (root, query, cb) -> cb.conjunction();
        if (tipo != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("tipo"), tipo));
        if (activa != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("activa"), activa));
        if (vencimientoDesde != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("fechaVencimiento"), vencimientoDesde));
        }
        if (vencimientoHasta != null) {
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("fechaVencimiento"), vencimientoHasta));
        }
        return cuotaRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public List<CuotaVecinoResponse> asignar(Long cuotaId, AsignacionCuotaRequest request) {
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada"));
        if (!Boolean.TRUE.equals(cuota.getActiva())) {
            throw new BadRequestException("La cuota está inactiva");
        }
        List<CuotaVecinoResponse> resultado = new ArrayList<>();
        for (Long vecinoId : request.getVecinoIds()) {
            if (vecinoClient.obtenerPorId(vecinoId) == null) {
                throw new ResourceNotFoundException("Vecino no encontrado: " + vecinoId);
            }
            if (cuotaVecinoRepository.existsByCuotaIdAndVecinoId(cuotaId, vecinoId)) {
                throw new BadRequestException("La cuota ya está asignada al vecino " + vecinoId);
            }
            CuotaVecino asignacion = new CuotaVecino();
            asignacion.setCuota(cuota);
            asignacion.setVecinoId(vecinoId);
            asignacion.setEstado(EstadoCuotaVecino.PENDIENTE);
            asignacion.setMontoPagado(BigDecimal.ZERO);
            asignacion.setFechaAsignacion(LocalDateTime.now());
            resultado.add(toResponse(cuotaVecinoRepository.save(asignacion)));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Page<CuotaVecinoResponse> buscarAsignaciones(
            Long vecinoId, EstadoCuotaVecino estado, boolean soloMorosas, Pageable pageable) {
        return cuotaVecinoRepository
                .buscar(vecinoId, estado, soloMorosas, LocalDate.now(), pageable)
                .map(this::toResponse);
    }

    private CuotaResponse toResponse(Cuota cuota) {
        return CuotaResponse.builder()
                .id(cuota.getId()).nombre(cuota.getNombre()).descripcion(cuota.getDescripcion())
                .tipo(cuota.getTipo()).monto(cuota.getMonto())
                .fechaVencimiento(cuota.getFechaVencimiento()).activa(cuota.getActiva())
                .fechaCreacion(cuota.getFechaCreacion()).build();
    }

    private CuotaVecinoResponse toResponse(CuotaVecino asignacion) {
        BigDecimal saldo = asignacion.getCuota().getMonto().subtract(asignacion.getMontoPagado());
        boolean morosa = asignacion.getEstado() == EstadoCuotaVecino.PENDIENTE
                && asignacion.getCuota().getFechaVencimiento().isBefore(LocalDate.now());
        return CuotaVecinoResponse.builder()
                .id(asignacion.getId()).cuotaId(asignacion.getCuota().getId())
                .vecinoId(asignacion.getVecinoId()).cuotaNombre(asignacion.getCuota().getNombre())
                .tipo(asignacion.getCuota().getTipo()).monto(asignacion.getCuota().getMonto())
                .montoPagado(asignacion.getMontoPagado()).saldoPendiente(saldo.max(BigDecimal.ZERO))
                .fechaVencimiento(asignacion.getCuota().getFechaVencimiento())
                .estado(asignacion.getEstado()).morosa(morosa)
                .fechaAsignacion(asignacion.getFechaAsignacion()).build();
    }
}
