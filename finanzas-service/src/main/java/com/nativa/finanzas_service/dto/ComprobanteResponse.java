package com.nativa.finanzas_service.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ComprobanteResponse {
    private Long id;
    private Long pagoId;
    private String nombreArchivo;
    private String url;
    private String mimeType;
    private LocalDateTime fechaCarga;
}
