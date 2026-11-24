package com.nativa.reserva_service.controller;

import com.nativa.reserva_service.dto.ReservaResponse;
import com.nativa.reserva_service.dto.AppReservaResponse;
import com.nativa.reserva_service.model.EstadoReserva;
import com.nativa.reserva_service.security.JwtUtil;
import com.nativa.reserva_service.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "VECINO")
class ReservaControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReservaService reservaService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
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
}
