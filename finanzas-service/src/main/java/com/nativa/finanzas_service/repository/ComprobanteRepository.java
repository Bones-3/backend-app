package com.nativa.finanzas_service.repository;

import com.nativa.finanzas_service.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    Optional<Comprobante> findByPagoId(Long pagoId);
    boolean existsByPagoId(Long pagoId);
}
