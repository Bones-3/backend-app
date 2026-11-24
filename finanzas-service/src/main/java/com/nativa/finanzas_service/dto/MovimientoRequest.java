package com.nativa.finanzas_service.dto;

import com.nativa.finanzas_service.model.TipoMovimiento;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MovimientoRequest {
    @NotNull private TipoMovimiento tipo;
    @NotBlank private String categoria;
    @NotBlank private String descripcion;
    @NotNull @Positive private BigDecimal monto;
    @NotNull private LocalDate fecha;
}
