package com.nativa.reserva_service.repository;

import com.nativa.reserva_service.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {
    List<Reserva> findByVecinoIdOrderByFechaDescHoraInicioAsc(Long vecinoId);

    @Query("""
            select (count(r) > 0) from Reserva r
            where r.espacio.id = :espacioId
              and r.fecha = :fecha
              and r.estado <> com.nativa.reserva_service.model.EstadoReserva.CANCELADA
              and r.horaInicio < :horaFin
              and r.horaFin > :horaInicio
              and (:excluirId is null or r.id <> :excluirId)
            """)
    boolean existeSolapamiento(
            @Param("espacioId") Long espacioId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excluirId") Long excluirId
    );
}
