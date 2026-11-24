package com.nativa.finanzas_service.repository;

import com.nativa.finanzas_service.model.CuotaVecino;
import com.nativa.finanzas_service.model.EstadoCuotaVecino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface CuotaVecinoRepository extends JpaRepository<CuotaVecino, Long> {
    boolean existsByCuotaIdAndVecinoId(Long cuotaId, Long vecinoId);
    Optional<CuotaVecino> findByIdAndVecinoId(Long id, Long vecinoId);

    @Query("""
            select cv from CuotaVecino cv
            where (:vecinoId is null or cv.vecinoId = :vecinoId)
              and (:estado is null or cv.estado = :estado)
              and (:soloMorosas = false or (cv.estado = com.nativa.finanzas_service.model.EstadoCuotaVecino.PENDIENTE
                   and cv.cuota.fechaVencimiento < :hoy))
            """)
    Page<CuotaVecino> buscar(
            @Param("vecinoId") Long vecinoId,
            @Param("estado") EstadoCuotaVecino estado,
            @Param("soloMorosas") boolean soloMorosas,
            @Param("hoy") LocalDate hoy,
            Pageable pageable);

    @Query("""
            select count(cv) from CuotaVecino cv
            where cv.estado = com.nativa.finanzas_service.model.EstadoCuotaVecino.PENDIENTE
              and cv.cuota.fechaVencimiento < :fecha
            """)
    long contarMorosas(@Param("fecha") LocalDate fecha);
}
