package com.nativa.finanzas_service.service;

import com.nativa.finanzas_service.dto.ReporteFinancieroResponse;
import com.nativa.finanzas_service.model.TipoMovimiento;
import com.nativa.finanzas_service.repository.CuotaVecinoRepository;
import com.nativa.finanzas_service.repository.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {
    @Mock private MovimientoRepository movimientoRepository;
    @Mock private CuotaVecinoRepository cuotaVecinoRepository;
    @InjectMocks private ReporteService service;

    @Test
    void generar_shouldCalculateBalanceAndMorosity() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 1, 31);
        when(movimientoRepository.sumarPorPeriodo(TipoMovimiento.INGRESO, desde, hasta))
                .thenReturn(new BigDecimal("100000"));
        when(movimientoRepository.sumarPorPeriodo(TipoMovimiento.EGRESO, desde, hasta))
                .thenReturn(new BigDecimal("35000"));
        when(cuotaVecinoRepository.contarMorosas(hasta)).thenReturn(3L);

        ReporteFinancieroResponse reporte = service.generar(desde, hasta);

        assertThat(reporte.getSaldo()).isEqualByComparingTo("65000");
        assertThat(reporte.getCuotasMorosas()).isEqualTo(3);
    }
}
