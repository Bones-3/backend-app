package com.nativa.finanzas_service.dto;

import com.nativa.finanzas_service.model.TipoMovimiento;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class MovimientoResponse {
    private Long id;
    private TipoMovimiento tipo;
    private String categoria;
    private String descripcion;
    private BigDecimal monto;
    private LocalDate fecha;
    private Long pagoId;
}
