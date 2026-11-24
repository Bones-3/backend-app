package com.nativa.finanzas_service.dto;

import com.nativa.finanzas_service.model.TipoCuota;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CuotaRequest {
    @NotBlank private String nombre;
    private String descripcion;
    @NotNull private TipoCuota tipo;
    @NotNull @Positive private BigDecimal monto;
    @NotNull private LocalDate fechaVencimiento;
    private Boolean activa;
}
