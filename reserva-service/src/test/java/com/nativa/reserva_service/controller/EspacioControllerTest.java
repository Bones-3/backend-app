package com.nativa.reserva_service.controller;

import com.nativa.reserva_service.dto.EspacioResponse;
import com.nativa.reserva_service.model.TipoEspacio;
import com.nativa.reserva_service.security.JwtUtil;
import com.nativa.reserva_service.service.EspacioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EspacioController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "DIRECTIVA")
class EspacioControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private EspacioService espacioService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    void getActivos_shouldReturnSpaces() throws Exception {
        when(espacioService.getActivos()).thenReturn(List.of(
                EspacioResponse.builder().id(1L).nombre("Cancha").tipo(TipoEspacio.CANCHA).activo(true).build(),
                EspacioResponse.builder().id(2L).nombre("Salón").tipo(TipoEspacio.SALON).activo(true).build()));
        mockMvc.perform(get("/reserva/espacios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("CANCHA"))
                .andExpect(jsonPath("$[1].tipo").value("SALON"));
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        when(espacioService.create(any())).thenReturn(
                EspacioResponse.builder().id(1L).nombre("Cancha").tipo(TipoEspacio.CANCHA).activo(true).build());
        mockMvc.perform(post("/reserva/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Cancha","tipo":"CANCHA","activo":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
