package com.nativa.vecino_service.mapper;

import org.springframework.stereotype.Component;

import com.nativa.vecino_service.dto.VecinoRequest;
import com.nativa.vecino_service.dto.VecinoResponse;
import com.nativa.vecino_service.model.Vecino;
import com.nativa.vecino_service.model.Rol;

@Component
public class VecinoMapper {
    public Vecino toEntity(VecinoRequest request) {

        Vecino vecino = new Vecino();

        vecino.setRut(request.getRut());
        vecino.setCorreoVecino(request.getCorreoVecino());
        vecino.setNombres(request.getNombres());
        vecino.setApellidos(request.getApellidos());
        vecino.setTelefonoVecino(request.getTelefonoVecino());
        vecino.setEstadoVecino(true);
        vecino.setRol(request.getRol() == null ? Rol.VECINO : request.getRol());

        return vecino;   
    }
    
    public VecinoResponse toResponse(Vecino vecino) {
        return VecinoResponse.builder()
                .id(vecino.getId())
                .rut(vecino.getRut())
                .correoVecino(vecino.getCorreoVecino())
                .nombre(vecino.getNombres())
                .apellido(vecino.getApellidos())
                .telefonoVecino(vecino.getTelefonoVecino())
                .estadoVecino(vecino.getEstadoVecino())
                .rol(vecino.getRol())
                .build();
    }
}

