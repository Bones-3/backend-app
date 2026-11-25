package com.nativa.vecino_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VecinoSyncRequest {
    @NotBlank
    private String rut;

    @NotBlank
    @Size(max = 180)
    private String nombre;

    @Size(max = 30)
    private String telefono;
}
