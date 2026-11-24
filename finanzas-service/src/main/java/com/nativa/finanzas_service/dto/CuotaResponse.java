package com.nativa.finanzas_service.dto;

import com.nativa.finanzas_service.model.TipoCuota;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CuotaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private TipoCuota tipo;
    private BigDecimal monto;
    private LocalDate fechaVencimiento;
    private Boolean activa;
    private LocalDateTime fechaCreacion;
}
