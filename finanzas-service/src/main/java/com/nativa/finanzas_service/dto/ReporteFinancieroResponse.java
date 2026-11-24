package com.nativa.finanzas_service.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ReporteFinancieroResponse {
    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal ingresos;
    private BigDecimal egresos;
    private BigDecimal saldo;
    private long cuotasMorosas;
}
