package com.nativa.finanzas_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComprobanteRequest {
    @NotNull private Long pagoId;
    @NotBlank private String nombreArchivo;
    @NotBlank private String url;
    private String mimeType;
}
