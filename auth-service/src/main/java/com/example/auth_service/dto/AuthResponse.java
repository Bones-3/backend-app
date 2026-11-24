package com.example.auth_service.dto;

import com.example.auth_service.model.Rol;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private Long usuarioId;
    private String token;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String rut;
    private String nombre;
    private String telefono;
    private String region;
    private String comuna;
    /** Campo legado; los clientes Android deben identificar al usuario por RUT. */
    private String correo;
    private Rol rol;
}
