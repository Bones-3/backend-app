package com.nativa.finanzas_service.service;

import com.nativa.finanzas_service.dto.ReporteFinancieroResponse;
import com.nativa.finanzas_service.exception.BadRequestException;
import com.nativa.finanzas_service.model.TipoMovimiento;
import com.nativa.finanzas_service.repository.CuotaVecinoRepository;
import com.nativa.finanzas_service.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReporteService {
    private final MovimientoRepository movimientoRepository;
    private final CuotaVecinoRepository cuotaVecinoRepository;

    @Transactional(readOnly = true)
    public ReporteFinancieroResponse generar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new BadRequestException("El período del reporte es inválido");
        }
        BigDecimal ingresos = movimientoRepository.sumarPorPeriodo(TipoMovimiento.INGRESO, desde, hasta);
        BigDecimal egresos = movimientoRepository.sumarPorPeriodo(TipoMovimiento.EGRESO, desde, hasta);
        return ReporteFinancieroResponse.builder()
                .desde(desde).hasta(hasta).ingresos(ingresos).egresos(egresos)
                .saldo(ingresos.subtract(egresos))
                .cuotasMorosas(cuotaVecinoRepository.contarMorosas(hasta))
                .build();
    }
}
