package com.example.auth_service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {
    private Long usuarioId;
    private String rut;
    private String nombre;
    private String mensaje;
}
