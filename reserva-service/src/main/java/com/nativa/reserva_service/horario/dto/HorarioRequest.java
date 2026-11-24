package com.nativa.reserva_service.horario.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HorarioRequest {

    @NotBlank(message = "El dia de la semana es obligatorio")
    private String diaSemana;

    @NotNull(message = "La hora de apertura es obligatoria")
    private LocalTime horaApertura;

    @NotNull(message = "La hora de cierre es obligatoria")
    private LocalTime horaCierre;

    @NotNull(message = "El estado del espacio es obligatorio")
    private Boolean cerrado;


}

