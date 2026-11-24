package com.nativa.finanzas_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nativa.finanzas_service.model.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface PagoRepository extends JpaRepository<Pago, Long>{
    @Query("""
            select p from Pago p
            where (:vecinoId is null or p.vecino_id = :vecinoId)
              and (:desde is null or p.fechaPago >= :desde)
              and (:hasta is null or p.fechaPago <= :hasta)
            """)
    Page<Pago> buscar(
            @Param("vecinoId") Long vecinoId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);
}

