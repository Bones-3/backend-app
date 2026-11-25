package com.nativa.reserva_service.service;

import com.nativa.reserva_service.dto.ReservaRequest;
import com.nativa.reserva_service.dto.ReservaResponse;
import com.nativa.reserva_service.dto.AppReservaRequest;
import com.nativa.reserva_service.dto.AppReservaResponse;
import com.nativa.reserva_service.exception.BadRequestException;
import com.nativa.reserva_service.exception.ResourceNotFoundException;
import com.nativa.reserva_service.mapper.ReservaMapper;
import com.nativa.reserva_service.model.*;
import com.nativa.reserva_service.repository.EspacioRepository;
import com.nativa.reserva_service.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {
    @Mock private ReservaRepository reservaRepository;
    @Mock private EspacioRepository espacioRepository;
    @Mock private ReservaMapper reservaMapper;
    @InjectMocks private ReservaService reservaService;

    private ReservaRequest request;
    private Espacio cancha;

    @BeforeEach
    void setUp() {
        request = new ReservaRequest();
        request.setVecinoId(10L);
        request.setEspacioId(1L);
        request.setFecha(LocalDate.now().plusDays(1));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFin(LocalTime.of(11, 0));
        cancha = new Espacio(1L, "Cancha", TipoEspacio.CANCHA, true);
    }

    @Test
    void create_shouldPersistWithoutAttendanceLimit() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(cancha));
        when(reservaRepository.existeSolapamiento(1L, request.getFecha(), request.getHoraInicio(), request.getHoraFin(), null))
                .thenReturn(false);
        Reserva saved = new Reserva(1L, 10L, cancha, request.getFecha(), request.getHoraInicio(),
                request.getHoraFin(), EstadoReserva.PENDIENTE, LocalDateTime.now());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(saved);
        when(reservaMapper.toResponse(saved)).thenReturn(ReservaResponse.builder().id(1L).vecinoId(10L).build());

        assertThat(reservaService.create(request).getId()).isEqualTo(1L);
    }

    @Test
    void create_shouldRejectOverlap() {
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(cancha));
        when(reservaRepository.existeSolapamiento(1L, request.getFecha(), request.getHoraInicio(), request.getHoraFin(), null))
                .thenReturn(true);

        assertThatThrownBy(() -> reservaService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El espacio ya está reservado en ese horario");
    }

    @Test
    void create_shouldRejectInvalidRange() {
        request.setHoraFin(LocalTime.of(9, 0));
        assertThatThrownBy(() -> reservaService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("La hora de término debe ser posterior a la hora de inicio");
        verifyNoInteractions(espacioRepository, reservaRepository);
    }

    @Test
    void create_shouldRejectInactiveSpace() {
        cancha.setActivo(false);
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(cancha));
        assertThatThrownBy(() -> reservaService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El espacio no está disponible");
    }

    @Test
    void createFromApp_shouldMapAndroidReservationFields() {
        AppReservaRequest appRequest = new AppReservaRequest();
        LocalDate fecha = LocalDate.now().plusDays(2);
        appRequest.setUsuarioId(7L);
        appRequest.setNombre("Vecino Prueba");
        appRequest.setFecha(fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        appRequest.setHoraInicioMin(600);
        appRequest.setHoraFinMin(660);
        appRequest.setAsistentes(12);
        appRequest.setObservaciones("Reunión vecinal");
        appRequest.setRequiereProyector(true);
        appRequest.setAceptaReglamento(true);
        Espacio salon = new Espacio(2L, "Salón", TipoEspacio.SALON, true);

        when(espacioRepository.findByTipoAndActivoTrue(TipoEspacio.SALON)).thenReturn(Optional.of(salon));
        when(reservaRepository.existeSolapamiento(2L, fecha, LocalTime.of(10, 0),
                LocalTime.of(11, 0), null)).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva guardada = invocation.getArgument(0);
            guardada.setId(9L);
            return guardada;
        });

        AppReservaResponse response = reservaService.createFromApp(appRequest);

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getUsuarioId()).isEqualTo(7L);
        assertThat(response.getNombre()).isEqualTo("Vecino Prueba");
        assertThat(response.getHoraInicioMin()).isEqualTo(600);
        assertThat(response.getAsistentes()).isEqualTo(12);
        assertThat(response.isRequiereProyector()).isTrue();
    }

    @Test
    void createFromApp_shouldAcceptTodayInSantiago() {
        AppReservaRequest appRequest = new AppReservaRequest();
        LocalDate hoyEnSantiago = LocalDate.now(ZoneId.of("America/Santiago"));
        appRequest.setUsuarioId(7L);
        appRequest.setNombre("Vecino Prueba");
        appRequest.setFecha(hoyEnSantiago.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        appRequest.setHoraInicioMin(600);
        appRequest.setHoraFinMin(660);
        appRequest.setAsistentes(12);
        appRequest.setAceptaReglamento(true);
        Espacio salon = new Espacio(2L, "Salón", TipoEspacio.SALON, true);

        when(espacioRepository.findByTipoAndActivoTrue(TipoEspacio.SALON)).thenReturn(Optional.of(salon));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(reservaService.createFromApp(appRequest).getFecha()).isEqualTo(appRequest.getFecha());
    }

    @Test
    void create_shouldRejectYesterdayInSantiago() {
        request.setFecha(LocalDate.now(ZoneId.of("America/Santiago")).minusDays(1));

        assertThatThrownBy(() -> reservaService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("La fecha no puede estar en el pasado");
        verifyNoInteractions(espacioRepository, reservaRepository);
    }

    @Test
    void cancel_shouldMarkReservationAsCancelled() {
        Reserva reserva = new Reserva(1L, 10L, cancha, request.getFecha(), request.getHoraInicio(),
                request.getHoraFin(), EstadoReserva.PENDIENTE, LocalDateTime.now());
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        reservaService.cancel(1L);

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        verify(reservaRepository).save(reserva);
    }

    @Test
    void getById_shouldThrowWhenMissing() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservaService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reserva no encontrada");
    }
}
