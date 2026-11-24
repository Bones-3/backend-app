package com.nativa.reserva_service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppReservaRequest {
    @NotNull(message = "El usuarioId es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El nombre del solicitante es obligatorio")
    @Size(min = 3, max = 120)
    private String nombre;

    @NotNull(message = "La fecha es obligatoria")
    private Long fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Min(0) @Max(1439)
    private Integer horaInicioMin;

    @NotNull(message = "La hora de término es obligatoria")
    @Min(1) @Max(1439)
    private Integer horaFinMin;

    @NotNull(message = "La cantidad de asistentes es obligatoria")
    @Min(1) @Max(50)
    private Integer asistentes;

    @Size(max = 200, message = "Las observaciones no pueden superar 200 caracteres")
    private String observaciones;

    private boolean requiereProyector;

    @AssertTrue(message = "Debes aceptar el reglamento de uso")
    private boolean aceptaReglamento;
}
