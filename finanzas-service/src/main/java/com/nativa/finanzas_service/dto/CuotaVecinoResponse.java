package com.nativa.finanzas_service.dto;

import com.nativa.finanzas_service.model.EstadoCuotaVecino;
import com.nativa.finanzas_service.model.TipoCuota;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CuotaVecinoResponse {
    private Long id;
    private Long cuotaId;
    private Long vecinoId;
    private String cuotaNombre;
    private TipoCuota tipo;
    private BigDecimal monto;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private LocalDate fechaVencimiento;
    private EstadoCuotaVecino estado;
    private boolean morosa;
    private LocalDateTime fechaAsignacion;
}
