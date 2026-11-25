package com.nativa.vecino_service.dto;

import com.nativa.vecino_service.model.Rol;

public record MiPerfilResponse(Long id, String nombre, String apellido, String telefono, Rol rol) {
}
