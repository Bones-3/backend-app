package com.nativa.finanzas_service.repository;

import com.nativa.finanzas_service.model.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CuotaRepository extends JpaRepository<Cuota, Long>, JpaSpecificationExecutor<Cuota> {
}
