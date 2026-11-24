package com.nativa.vecino_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nativa.vecino_service.model.Vecino;

public interface VecinoRepository extends JpaRepository<Vecino, Long>, JpaSpecificationExecutor<Vecino>{
        List<Vecino> findByEstadoVecinoTrue();
}

