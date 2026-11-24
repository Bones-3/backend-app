package com.nativa.reserva_service.dto;

import com.nativa.reserva_service.model.EstadoReserva;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppReservaResponse {
    private Long id;
    private Long usuarioId;
    private String nombre;
    private Long fecha;
    private Integer horaInicioMin;
    private Integer horaFinMin;
    private Integer asistentes;
    private String observaciones;
    private boolean requiereProyector;
    private boolean aceptaReglamento;
    private EstadoReserva estado;
    private Long createdAt;
}
