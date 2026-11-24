package com.nativa.reserva_service.horario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nativa.reserva_service.horario.model.Horario;

public interface HorarioRepository extends JpaRepository<Horario, Long>{
}

