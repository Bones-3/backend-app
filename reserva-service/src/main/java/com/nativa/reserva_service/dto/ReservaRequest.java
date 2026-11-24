package com.nativa.reserva_service.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReservaRequest {
    @NotNull(message = "El vecinoId es obligatorio")
    private Long vecinoId;

    @NotNull(message = "El espacioId es obligatorio")
    private Long espacioId;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede estar en el pasado")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de término es obligatoria")
    private LocalTime horaFin;
}
