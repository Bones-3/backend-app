package com.nativa.reserva_service.mapper;

import com.nativa.reserva_service.dto.EspacioRequest;
import com.nativa.reserva_service.dto.EspacioResponse;
import com.nativa.reserva_service.model.Espacio;
import org.springframework.stereotype.Component;

@Component
public class EspacioMapper {
    public Espacio toEntity(EspacioRequest request) {
        Espacio espacio = new Espacio();
        espacio.setNombre(request.getNombre());
        espacio.setTipo(request.getTipo());
        espacio.setActivo(request.getActivo() == null ? true : request.getActivo());
        return espacio;
    }

    public EspacioResponse toResponse(Espacio espacio) {
        return EspacioResponse.builder()
                .id(espacio.getId())
                .nombre(espacio.getNombre())
                .tipo(espacio.getTipo())
                .activo(espacio.getActivo())
                .build();
    }
}
