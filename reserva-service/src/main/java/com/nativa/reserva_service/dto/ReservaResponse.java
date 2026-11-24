package com.nativa.reserva_service.dto;

import com.nativa.reserva_service.model.EstadoReserva;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class ReservaResponse {
    private Long id;
    private Long vecinoId;
    private Long espacioId;
    private String espacioNombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoReserva estado;
    private LocalDateTime fechaCreacion;
}
