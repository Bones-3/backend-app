package com.nativa.finanzas_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagoRequest {
    @NotNull(message = "La cuotaVecinoId es obligatoria")
    private Long cuotaVecinoId;

    @Positive(message = "El monto debe ser mayor que cero")
    private BigDecimal monto;

    @NotBlank(message = "El método de pago no puede ser nulo")
    private String metodoPago;
}

