package com.nativa.reserva_service.repository;

import com.nativa.reserva_service.model.Espacio;
import com.nativa.reserva_service.model.TipoEspacio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {
    List<Espacio> findByActivoTrue();
    Optional<Espacio> findByTipoAndActivoTrue(TipoEspacio tipo);
}
