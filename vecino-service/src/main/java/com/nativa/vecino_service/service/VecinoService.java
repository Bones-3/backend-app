package com.nativa.vecino_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nativa.vecino_service.dto.VecinoRequest;
import com.nativa.vecino_service.dto.VecinoResponse;
import com.nativa.vecino_service.dto.VecinoSyncRequest;
import com.nativa.vecino_service.exception.BadRequestException;
import com.nativa.vecino_service.exception.ConflictException;
import com.nativa.vecino_service.exception.ResourceNotFoundException;
import com.nativa.vecino_service.mapper.VecinoMapper;
import com.nativa.vecino_service.repository.VecinoRepository;
import com.nativa.vecino_service.util.RutUtils;
import com.nativa.vecino_service.model.Rol;
import com.nativa.vecino_service.model.Vecino;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
@Slf4j
public class VecinoService {

    private static final int TAMANIO_DEFECTO = 20;
    private static final int TAMANIO_MAXIMO = 200;

    /**
     * Nombres públicos de ordenamiento (los del JSON y los de la entidad) ->
     * propiedad de la entidad.
     */
    private static final Map<String, String> ORDEN_PERMITIDO = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("rut", "rut"),
            Map.entry("correovecino", "correoVecino"),
            Map.entry("correo", "correoVecino"),
            Map.entry("nombre", "nombres"),
            Map.entry("nombres", "nombres"),
            Map.entry("apellido", "apellidos"),
            Map.entry("apellidos", "apellidos"),
            Map.entry("telefonovecino", "telefonoVecino"),
            Map.entry("estadovecino", "estadoVecino"),
            Map.entry("rol", "rol"));

    private final VecinoRepository vecinoRepository;
    private final VecinoMapper vecinoMapper;

    @Transactional(readOnly = true)
    public Page<VecinoResponse> buscar(String texto, Boolean activo, Rol rol, Pageable pageable) {
        Specification<Vecino> spec = (root, query, cb) -> cb.conjunction();
        if (texto != null && !texto.isBlank()) {
            String patron = "%" + texto.trim().toLowerCase() + "%";
            // El RUT se guarda normalizado: "12.345.678-5" debe encontrar "123456785".
            String rutNormalizado = texto.matches(".*\\d.*") ? RutUtils.normalizar(texto) : "";
            spec = spec.and((root, query, cb) -> {
                var coincide = cb.or(
                        cb.like(cb.lower(root.get("rut")), patron),
                        cb.like(cb.lower(root.get("correoVecino")), patron),
                        cb.like(cb.lower(root.get("nombres")), patron),
                        cb.like(cb.lower(root.get("apellidos")), patron));
                if (rutNormalizado.isEmpty()) {
                    return coincide;
                }
                return cb.or(coincide, cb.like(root.get("rut"), "%" + rutNormalizado + "%"));
            });
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estadoVecino"), activo));
        }
        if (rol != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("rol"), rol));
        }
        return vecinoRepository.findAll(spec, sanearPaginacion(pageable)).map(vecinoMapper::toResponse);
    }

    /**
     * - Traduce sort=nombre/apellido a las propiedades reales y rechaza con 400 las
     * desconocidas
     * (antes terminaban en 500).
     * - Agrega "id" como desempate: ordenar solo por apellidos es inestable y
     * repite/omite filas entre páginas.
     * - Limita el tamaño de página.
     */
    private Pageable sanearPaginacion(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, TAMANIO_DEFECTO, Sort.by("apellidos", "id"));
        }
        List<Sort.Order> ordenes = new ArrayList<>();
        for (Sort.Order orden : pageable.getSort()) {
            String propiedad = ORDEN_PERMITIDO.get(orden.getProperty().toLowerCase());
            if (propiedad == null) {
                throw new BadRequestException("Campo de ordenamiento no válido: '" + orden.getProperty()
                        + "'. Permitidos: nombre, apellido, rut, correo, rol, estadoVecino, id");
            }
            ordenes.add(orden.withProperty(propiedad));
        }
        if (ordenes.stream().noneMatch(o -> o.getProperty().equals("id"))) {
            ordenes.add(Sort.Order.asc("id"));
        }
        int tamanio = Math.max(1, Math.min(pageable.getPageSize(), TAMANIO_MAXIMO));
        return PageRequest.of(pageable.getPageNumber(), tamanio, Sort.by(ordenes));
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
        prepararYValidar(request, null);
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
                    return new ResourceNotFoundException("Vecino no encontrado");
                });

        prepararYValidar(request, id);

        vecino.setRut(request.getRut());
        vecino.setCorreoVecino(request.getCorreoVecino());
        vecino.setNombres(request.getNombres());
        vecino.setApellidos(request.getApellidos());
        vecino.setTelefonoVecino(request.getTelefonoVecino());
        if (request.getRol() != null) {
            vecino.setRol(request.getRol());
        }
        if (request.getEstadoVecino() != null) {
            vecino.setEstadoVecino(request.getEstadoVecino());
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
                    return new ResourceNotFoundException("Vecino no encontrado");
                });

        vecino.setEstadoVecino(false);
        vecinoRepository.save(vecino);
        log.info("Vecino con ID: {} deshabilitado (Eliminación lógica completada)", id);
    }

    /**
     * Crea el vecino si aún no existe para ese RUT (idempotente). Lo usa
     * auth-service al registrar
     * o iniciar sesión, para que quien se registra aparezca también en el listado
     * de vecinos.
     */
    @Transactional
    public VecinoResponse sincronizar(VecinoSyncRequest request) {
        String rut = RutUtils.normalizar(request.getRut());
        if (!RutUtils.esValido(rut)) {
            throw new BadRequestException("RUT inválido");
        }
        return vecinoRepository.findByRut(rut)
                .map(vecinoMapper::toResponse)
                .orElseGet(() -> {
                    String[] partes = request.getNombre().trim().split("\\s+");
                    int corte = partes.length == 1 ? 1 : partes.length / 2;
                    Vecino nuevo = new Vecino();
                    nuevo.setRut(rut);
                    nuevo.setCorreoVecino(rut.toLowerCase() + "@rut.local");
                    nuevo.setNombres(String.join(" ", java.util.Arrays.copyOfRange(partes, 0, corte)));
                    nuevo.setApellidos(String.join(" ", java.util.Arrays.copyOfRange(partes, corte, partes.length)));
                    nuevo.setTelefonoVecino(request.getTelefono());
                    nuevo.setEstadoVecino(true);
                    nuevo.setRol(Rol.VECINO);
                    log.info("Sincronizando nuevo vecino desde auth con RUT {}", rut);
                    return vecinoMapper.toResponse(vecinoRepository.save(nuevo));
                });
    }

    /**
     * Normaliza RUT/correo, valida el dígito verificador y comprueba duplicados con
     * mensaje claro (409).
     */
    private void prepararYValidar(VecinoRequest request, Long idActual) {
        String rut = RutUtils.normalizar(request.getRut());
        if (!RutUtils.esValido(rut)) {
            throw new BadRequestException("RUT inválido");
        }
        request.setRut(rut);
        request.setCorreoVecino(request.getCorreoVecino().trim().toLowerCase());
        request.setNombres(request.getNombres().trim());
        request.setApellidos(request.getApellidos().trim());

        boolean rutDuplicado = idActual == null
                ? vecinoRepository.existsByRut(rut)
                : vecinoRepository.existsByRutAndIdNot(rut, idActual);
        if (rutDuplicado) {
            throw new ConflictException("Ya existe un vecino con el RUT " + rut);
        }
        boolean correoDuplicado = idActual == null
                ? vecinoRepository.existsByCorreoVecinoIgnoreCase(request.getCorreoVecino())
                : vecinoRepository.existsByCorreoVecinoIgnoreCaseAndIdNot(request.getCorreoVecino(), idActual);
        if (correoDuplicado) {
            throw new ConflictException("Ya existe un vecino con el correo " + request.getCorreoVecino());
        }
    }
}
