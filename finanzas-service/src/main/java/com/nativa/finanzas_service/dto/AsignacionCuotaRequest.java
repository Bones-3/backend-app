package com.nativa.finanzas_service.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AsignacionCuotaRequest {
    @NotEmpty(message = "Debe indicar al menos un vecino")
    private List<Long> vecinoIds;
}
