package com.nativa.finanzas_service.repository;

import com.nativa.finanzas_service.model.Movimiento;
import com.nativa.finanzas_service.model.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    @Query("""
            select m from Movimiento m
            where (:tipo is null or m.tipo = :tipo)
              and (:desde is null or m.fecha >= :desde)
              and (:hasta is null or m.fecha <= :hasta)
            """)
    Page<Movimiento> buscar(
            @Param("tipo") TipoMovimiento tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            Pageable pageable);

    @Query("""
            select coalesce(sum(m.monto), 0) from Movimiento m
            where m.tipo = :tipo and m.fecha between :desde and :hasta
            """)
    BigDecimal sumarPorPeriodo(
            @Param("tipo") TipoMovimiento tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
