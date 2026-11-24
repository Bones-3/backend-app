package com.nativa.reserva_service.service;

import com.nativa.reserva_service.dto.ReservaRequest;
import com.nativa.reserva_service.dto.ReservaResponse;
import com.nativa.reserva_service.dto.AppReservaRequest;
import com.nativa.reserva_service.dto.AppReservaResponse;
import com.nativa.reserva_service.exception.BadRequestException;
import com.nativa.reserva_service.exception.ResourceNotFoundException;
import com.nativa.reserva_service.mapper.ReservaMapper;
import com.nativa.reserva_service.model.EstadoReserva;
import com.nativa.reserva_service.model.Reserva;
import com.nativa.reserva_service.model.TipoEspacio;
import com.nativa.reserva_service.repository.EspacioRepository;
import com.nativa.reserva_service.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final ReservaMapper reservaMapper;

    @Transactional(readOnly = true)
    public Page<ReservaResponse> buscar(
            Long vecinoId, Long espacioId, EstadoReserva estado,
            LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        Specification<Reserva> spec = (root, query, cb) -> cb.conjunction();
        if (vecinoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("vecinoId"), vecinoId));
        }
        if (espacioId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("espacio").get("id"), espacioId));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (fechaDesde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
        }
        if (fechaHasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
        }
        return reservaRepository.findAll(spec, pageable).map(reservaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> getAll() {
        return reservaRepository.findAll().stream().map(reservaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse getById(Long id) {
        return reservaMapper.toResponse(buscar(id));
    }

    @Transactional
    public ReservaResponse create(ReservaRequest request) {
        validarRango(request);
        var espacio = espacioRepository.findById(request.getEspacioId())
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado"));
        if (!Boolean.TRUE.equals(espacio.getActivo())) {
            throw new BadRequestException("El espacio no está disponible");
        }
        validarSolapamiento(request, null);

        Reserva reserva = new Reserva();
        reserva.setVecinoId(request.getVecinoId());
        reserva.setEspacio(espacio);
        reserva.setFecha(request.getFecha());
        reserva.setHoraInicio(request.getHoraInicio());
        reserva.setHoraFin(request.getHoraFin());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaCreacion(LocalDateTime.now());
        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    @Transactional
    public AppReservaResponse createFromApp(AppReservaRequest request) {
        LocalDate fecha = Instant.ofEpochMilli(request.getFecha())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
        LocalTime horaInicio = LocalTime.of(request.getHoraInicioMin() / 60, request.getHoraInicioMin() % 60);
        LocalTime horaFin = LocalTime.of(request.getHoraFinMin() / 60, request.getHoraFinMin() % 60);

        if (fecha.isBefore(LocalDate.now())) {
            throw new BadRequestException("La fecha no puede estar en el pasado");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new BadRequestException("La hora de término debe ser posterior a la hora de inicio");
        }

        var espacio = espacioRepository.findByTipoAndActivoTrue(TipoEspacio.SALON)
                .orElseThrow(() -> new ResourceNotFoundException("El salón no está disponible"));
        ReservaRequest reservaRequest = new ReservaRequest();
        reservaRequest.setVecinoId(request.getUsuarioId());
        reservaRequest.setEspacioId(espacio.getId());
        reservaRequest.setFecha(fecha);
        reservaRequest.setHoraInicio(horaInicio);
        reservaRequest.setHoraFin(horaFin);
        validarSolapamiento(reservaRequest, null);

        Reserva reserva = new Reserva();
        reserva.setVecinoId(request.getUsuarioId());
        reserva.setEspacio(espacio);
        reserva.setFecha(fecha);
        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFin(horaFin);
        reserva.setSolicitanteNombre(request.getNombre().trim());
        reserva.setAsistentes(request.getAsistentes());
        reserva.setObservaciones(request.getObservaciones() == null ? "" : request.getObservaciones().trim());
        reserva.setRequiereProyector(request.isRequiereProyector());
        reserva.setAceptaReglamento(request.isAceptaReglamento());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaCreacion(LocalDateTime.now());
        return toAppResponse(reservaRepository.save(reserva));
    }

    @Transactional(readOnly = true)
    public List<AppReservaResponse> findForApp(Long usuarioId) {
        return reservaRepository.findByVecinoIdOrderByFechaDescHoraInicioAsc(usuarioId)
                .stream()
                .map(this::toAppResponse)
                .toList();
    }

    @Transactional
    public ReservaResponse update(Long id, ReservaRequest request) {
        validarRango(request);
        Reserva reserva = buscar(id);
        var espacio = espacioRepository.findById(request.getEspacioId())
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado"));
        if (!Boolean.TRUE.equals(espacio.getActivo())) {
            throw new BadRequestException("El espacio no está disponible");
        }
        validarSolapamiento(request, id);
        reserva.setVecinoId(request.getVecinoId());
        reserva.setEspacio(espacio);
        reserva.setFecha(request.getFecha());
        reserva.setHoraInicio(request.getHoraInicio());
        reserva.setHoraFin(request.getHoraFin());
        return reservaMapper.toResponse(reservaRepository.save(reserva));
    }

    @Transactional
    public void cancel(Long id) {
        Reserva reserva = buscar(id);
        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);
    }

    private void validarRango(ReservaRequest request) {
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new BadRequestException("La hora de término debe ser posterior a la hora de inicio");
        }
    }

    private void validarSolapamiento(ReservaRequest request, Long excluirId) {
        if (reservaRepository.existeSolapamiento(
                request.getEspacioId(), request.getFecha(), request.getHoraInicio(), request.getHoraFin(), excluirId)) {
            throw new BadRequestException("El espacio ya está reservado en ese horario");
        }
    }

    private Reserva buscar(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    private AppReservaResponse toAppResponse(Reserva reserva) {
        long fechaMillis = reserva.getFecha().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        int horaInicioMin = reserva.getHoraInicio().getHour() * 60 + reserva.getHoraInicio().getMinute();
        int horaFinMin = reserva.getHoraFin().getHour() * 60 + reserva.getHoraFin().getMinute();
        long createdAt = reserva.getFechaCreacion().toInstant(ZoneOffset.UTC).toEpochMilli();
        return AppReservaResponse.builder()
                .id(reserva.getId())
                .usuarioId(reserva.getVecinoId())
                .nombre(reserva.getSolicitanteNombre())
                .fecha(fechaMillis)
                .horaInicioMin(horaInicioMin)
                .horaFinMin(horaFinMin)
                .asistentes(reserva.getAsistentes())
                .observaciones(reserva.getObservaciones())
                .requiereProyector(Boolean.TRUE.equals(reserva.getRequiereProyector()))
                .aceptaReglamento(Boolean.TRUE.equals(reserva.getAceptaReglamento()))
                .estado(reserva.getEstado())
                .createdAt(createdAt)
                .build();
    }
}
