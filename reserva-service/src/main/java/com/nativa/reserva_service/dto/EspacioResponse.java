package com.nativa.reserva_service.dto;

import com.nativa.reserva_service.model.TipoEspacio;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EspacioResponse {
    private Long id;
    private String nombre;
    private TipoEspacio tipo;
    private Boolean activo;
}
