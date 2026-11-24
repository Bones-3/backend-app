package com.nativa.vecino_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nativa.vecino_service.dto.VecinoRequest;
import com.nativa.vecino_service.dto.VecinoResponse;
import com.nativa.vecino_service.exception.ResourceNotFoundException;
import com.nativa.vecino_service.mapper.VecinoMapper;
import com.nativa.vecino_service.repository.VecinoRepository;
import com.nativa.vecino_service.model.Rol;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
@Slf4j
public class VecinoService {

    private final VecinoRepository vecinoRepository;
    private final VecinoMapper vecinoMapper;

    @Transactional(readOnly = true)
    public Page<VecinoResponse> buscar(String texto, Boolean activo, Rol rol, Pageable pageable) {
        Specification<com.nativa.vecino_service.model.Vecino> spec =
                (root, query, cb) -> cb.conjunction();
        if (texto != null && !texto.isBlank()) {
            String patron = "%" + texto.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("rut")), patron),
                    cb.like(cb.lower(root.get("correoVecino")), patron),
                    cb.like(cb.lower(root.get("nombres")), patron),
                    cb.like(cb.lower(root.get("apellidos")), patron)));
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estadoVecino"), activo));
        }
        if (rol != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("rol"), rol));
        }
        return vecinoRepository.findAll(spec, pageable).map(vecinoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<VecinoResponse> getVecinosActivo() {
        log.info("Solicitando lista de vecinos activos");
        return vecinoRepository.findByEstadoVecinoTrue()
                .stream()
                .map(vecinoMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<VecinoResponse> getAllVecinos() {
        log.info("Solicitando la lista de todos los vecinos registrados");
        return vecinoRepository.findAll()
                .stream()
                .map(vecinoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VecinoResponse getVecinoById(Long id) {
        log.info("Buscando vecino con ID: {}", id);
        return vecinoRepository.findById(id)
                .map(vecinoMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("No se encontró el vecino con ID: {}", id);
                    return new ResourceNotFoundException("Vecino no encontrado");
                });
    }

    @Transactional
    public VecinoResponse createVecino(VecinoRequest request) {
        log.info("Iniciando la creación de un nuevo vecino con correo: {}", request.getCorreoVecino());
        var vecino = vecinoMapper.toEntity(request);
        var vecinoGuardado = vecinoRepository.save(vecino);
        log.info("Vecino creado exitosamente con ID: {}", vecinoGuardado.getId());
        return vecinoMapper.toResponse(vecinoGuardado);
    }

    @Transactional
    public VecinoResponse updateVecino(Long id, VecinoRequest request) {
        log.info("Iniciando actualización del vecino con ID: {}", id);
        
        var vecino = vecinoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error al actualizar: Vecino con ID {} no existe", id);
                    return new ResourceNotFoundException("Vecino no encontrado"); // Corregido el mensaje "Categoría no encontrada"
                });
        
        vecino.setRut(request.getRut());
        vecino.setCorreoVecino(request.getCorreoVecino());
        vecino.setNombres(request.getNombres());  
        vecino.setApellidos(request.getApellidos());
        vecino.setTelefonoVecino(request.getTelefonoVecino());
        if (request.getRol() != null) {
            vecino.setRol(request.getRol());
        }

        var vecinoActualizado = vecinoRepository.save(vecino);
        log.info("Vecino con ID: {} actualizado correctamente", id);
        return vecinoMapper.toResponse(vecinoActualizado);
    }

    @Transactional
    public void deleteVecino(Long id) {
        log.info("Iniciando eliminación lógica del vecino con ID: {}", id);
        
        var vecino = vecinoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error al eliminar: Vecino con ID {} no existe", id);
                    return new ResourceNotFoundException("Vecino no encontrado"); // Corregido el mensaje "Categoría no encontrada"
                });

        vecino.setEstadoVecino(false);
        vecinoRepository.save(vecino);
        log.info("Vecino con ID: {} deshabilitado (Eliminación lógica completada)", id);
    }

}

