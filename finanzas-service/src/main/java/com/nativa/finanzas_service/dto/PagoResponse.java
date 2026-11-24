package com.nativa.finanzas_service.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PagoResponse {
    
    private Long id;
    private Long cuotaVecinoId;
    private Long vecino_id;
    private BigDecimal total;
    private String metodoPago;
    private LocalDateTime fechaPago;  
    
}

