package com.nativa.reserva_service.controller;

import com.nativa.reserva_service.dto.ReservaResponse;
import com.nativa.reserva_service.dto.AppReservaResponse;
import com.nativa.reserva_service.model.EstadoReserva;
import com.nativa.reserva_service.security.JwtUtil;
import com.nativa.reserva_service.security.SecurityConfig;
import com.nativa.reserva_service.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import com.nativa.reserva_service.dto.AppReservaRequest;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@WithMockUser(roles = "VECINO")
class ReservaControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReservaService reservaService;
    @MockitoBean private JwtUtil jwtUtil;

    private Authentication vecino(long id) {
        var auth = new UsernamePasswordAuthenticationToken("rut", null,
                List.of(new SimpleGrantedAuthority("ROLE_VECINO")));
        auth.setDetails(id);
        return auth;
    }

    @Test
    @WithMockUser(roles = "DIRECTIVA")
    void create_shouldReturnCreated() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        when(reservaService.create(any())).thenReturn(ReservaResponse.builder()
                .id(1L).vecinoId(10L).espacioId(1L).espacioNombre("Cancha")
                .fecha(fecha).horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(11, 0))
                .estado(EstadoReserva.PENDIENTE).build());

        mockMvc.perform(post("/reserva/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vecinoId": 10,
                                  "espacioId": 1,
                                  "fecha": "%s",
                                  "horaInicio": "10:00",
                                  "horaFin": "11:00"
                                }
                                """.formatted(fecha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.espacioNombre").value("Cancha"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void createFromApp_shouldAcceptAndroidPayload() throws Exception {
        long fecha = java.time.LocalDate.now().plusDays(1)
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
        when(reservaService.createFromApp(any())).thenReturn(AppReservaResponse.builder()
                .id(3L).usuarioId(10L).nombre("Vecino Demo").fecha(fecha)
                .horaInicioMin(600).horaFinMin(660).asistentes(10)
                .observaciones("").requiereProyector(true).aceptaReglamento(true)
                .estado(EstadoReserva.PENDIENTE).createdAt(fecha).build());

        mockMvc.perform(post("/reserva/reservas/app")
                        .with(authentication(vecino(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 10,
                                  "nombre": "Vecino Demo",
                                  "fecha": %d,
                                  "horaInicioMin": 600,
                                  "horaFinMin": 660,
                                  "asistentes": 10,
                                  "observaciones": "",
                                  "requiereProyector": true,
                                  "aceptaReglamento": true
                                }
                                """.formatted(fecha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(10))
                .andExpect(jsonPath("$.horaInicioMin").value(600))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void vecinoNoPuedeListarTodasLasReservas() throws Exception {
        mockMvc.perform(get("/reserva/reservas").with(authentication(vecino(10L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearDesdeAppIgnoraUsuarioIdManipulado() throws Exception {
        long fecha = LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
        mockMvc.perform(post("/reserva/reservas/app").with(authentication(vecino(10L)))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"usuarioId":20,"nombre":"Vecino Demo","fecha":%d,"horaInicioMin":600,
                         "horaFinMin":660,"asistentes":10,"aceptaReglamento":true}
                        """.formatted(fecha)))
                .andExpect(status().isCreated());
        ArgumentCaptor<AppReservaRequest> captor = ArgumentCaptor.forClass(AppReservaRequest.class);
        verify(reservaService).createFromApp(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getUsuarioId()).isEqualTo(10L);
    }

    @Test
    void vecinoNoPuedeConsultarNiCancelarReservaAjena() throws Exception {
        when(reservaService.getById(5L)).thenReturn(ReservaResponse.builder().id(5L).vecinoId(20L).build());
        mockMvc.perform(get("/reserva/reservas/5").with(authentication(vecino(10L))))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/reserva/reservas/5").with(authentication(vecino(10L))))
                .andExpect(status().isForbidden());
        verify(reservaService, never()).cancel(5L);
    }

    @Test
    void vecinoNoPuedeEditarReservaAjena() throws Exception {
        when(reservaService.getById(5L)).thenReturn(ReservaResponse.builder().id(5L).vecinoId(20L).build());
        String contenido = """
                {"vecinoId":20,"espacioId":1,"fecha":"%s","horaInicio":"10:00","horaFin":"11:00"}
                """.formatted(LocalDate.now().plusDays(1));
        mockMvc.perform(put("/reserva/reservas/5").with(authentication(vecino(10L)))
                        .contentType(MediaType.APPLICATION_JSON).content(contenido))
                .andExpect(status().isForbidden());
        verify(reservaService, never()).update(eq(5L), any());
    }

    @Test
    void vecinoNoPuedeCambiarPropietarioAlEditar() throws Exception {
        when(reservaService.getById(5L)).thenReturn(ReservaResponse.builder().id(5L).vecinoId(10L).build());
        String contenido = """
                {"vecinoId":20,"espacioId":1,"fecha":"%s","horaInicio":"10:00","horaFin":"11:00"}
                """.formatted(LocalDate.now().plusDays(1));
        mockMvc.perform(put("/reserva/reservas/5").with(authentication(vecino(10L)))
                        .contentType(MediaType.APPLICATION_JSON).content(contenido))
                .andExpect(status().isOk());
        org.mockito.ArgumentCaptor<com.nativa.reserva_service.dto.ReservaRequest> captor =
                org.mockito.ArgumentCaptor.forClass(com.nativa.reserva_service.dto.ReservaRequest.class);
        verify(reservaService).update(eq(5L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getVecinoId()).isEqualTo(10L);
    }
}
