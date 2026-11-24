package com.nativa.finanzas_service.mapper;

import com.nativa.finanzas_service.dto.PagoResponse;
import com.nativa.finanzas_service.model.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {
    public PagoResponse toResponse(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .cuotaVecinoId(pago.getCuotaVecino().getId())
                .vecino_id(pago.getVecino_id())
                .total(pago.getTotal())
                .metodoPago(pago.getMetodoPago())
                .fechaPago(pago.getFechaPago())
                .build();
    }
}
