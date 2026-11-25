package com.nativa.vecino_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.nativa.vecino_service.model.Rol;

@Getter
@Setter
@Builder
public class VecinoResponse {
    private Long id;
    private String rut;
    private String correoVecino;
    private String nombre;
    private String apellido;
    private String telefonoVecino;
    private boolean estadoVecino;
    private Rol rol;
}
