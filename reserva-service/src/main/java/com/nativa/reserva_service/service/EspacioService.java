package com.nativa.reserva_service.service;

import com.nativa.reserva_service.dto.EspacioRequest;
import com.nativa.reserva_service.dto.EspacioResponse;
import com.nativa.reserva_service.exception.ResourceNotFoundException;
import com.nativa.reserva_service.mapper.EspacioMapper;
import com.nativa.reserva_service.repository.EspacioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacioService {
    private final EspacioRepository espacioRepository;
    private final EspacioMapper espacioMapper;

    @Transactional(readOnly = true)
    public List<EspacioResponse> getActivos() {
        return espacioRepository.findByActivoTrue().stream().map(espacioMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EspacioResponse> getAll() {
        return espacioRepository.findAll().stream().map(espacioMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EspacioResponse getById(Long id) {
        return espacioMapper.toResponse(buscar(id));
    }

    @Transactional
    public EspacioResponse create(EspacioRequest request) {
        return espacioMapper.toResponse(espacioRepository.save(espacioMapper.toEntity(request)));
    }

    @Transactional
    public EspacioResponse update(Long id, EspacioRequest request) {
        EspacioActualizador actualizador = new EspacioActualizador(buscar(id));
        actualizador.aplicar(request);
        return espacioMapper.toResponse(espacioRepository.save(actualizador.espacio));
    }

    @Transactional
    public void deactivate(Long id) {
        var espacio = buscar(id);
        espacio.setActivo(false);
        espacioRepository.save(espacio);
    }

    private com.nativa.reserva_service.model.Espacio buscar(Long id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado"));
    }

    private static final class EspacioActualizador {
        private final com.nativa.reserva_service.model.Espacio espacio;
        private EspacioActualizador(com.nativa.reserva_service.model.Espacio espacio) {
            this.espacio = espacio;
        }
        private void aplicar(EspacioRequest request) {
            espacio.setNombre(request.getNombre());
            espacio.setTipo(request.getTipo());
            if (request.getActivo() != null) {
                espacio.setActivo(request.getActivo());
            }
        }
    }
}
