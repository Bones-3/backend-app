package com.nativa.vecino_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nativa.vecino_service.model.Vecino;

public interface VecinoRepository extends JpaRepository<Vecino, Long>, JpaSpecificationExecutor<Vecino> {
        List<Vecino> findByEstadoVecinoTrue();

        Optional<Vecino> findByRut(String rut);

        boolean existsByRut(String rut);

        boolean existsByRutAndIdNot(String rut, Long id);

        boolean existsByCorreoVecinoIgnoreCase(String correoVecino);

        boolean existsByCorreoVecinoIgnoreCaseAndIdNot(String correoVecino, Long id);
}
