package com.nativa.reserva_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @Column(name = "vecino_id", nullable = false)
    private Long vecinoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "solicitante_nombre", length = 120)
    private String solicitanteNombre;

    @Column(name = "asistentes")
    private Integer asistentes;

    @Column(name = "observaciones", length = 200)
    private String observaciones;

    @Column(name = "requiere_proyector")
    private Boolean requiereProyector = false;

    @Column(name = "acepta_reglamento")
    private Boolean aceptaReglamento = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public Reserva(Long id, Long vecinoId, Espacio espacio, LocalDate fecha,
                   LocalTime horaInicio, LocalTime horaFin, EstadoReserva estado,
                   LocalDateTime fechaCreacion) {
        this.id = id;
        this.vecinoId = vecinoId;
        this.espacio = espacio;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }
}
