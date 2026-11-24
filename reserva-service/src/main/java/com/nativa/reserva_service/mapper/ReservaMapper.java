package com.nativa.reserva_service.mapper;

import com.nativa.reserva_service.dto.ReservaResponse;
import com.nativa.reserva_service.model.Reserva;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {
    public ReservaResponse toResponse(Reserva reserva) {
        return ReservaResponse.builder()
                .id(reserva.getId())
                .vecinoId(reserva.getVecinoId())
                .espacioId(reserva.getEspacio().getId())
                .espacioNombre(reserva.getEspacio().getNombre())
                .fecha(reserva.getFecha())
                .horaInicio(reserva.getHoraInicio())
                .horaFin(reserva.getHoraFin())
                .estado(reserva.getEstado())
                .fechaCreacion(reserva.getFechaCreacion())
                .build();
    }
}
