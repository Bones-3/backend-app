package com.nativa.vecino_service.controller;

import com.nativa.vecino_service.dto.VecinoResponse;
import com.nativa.vecino_service.model.Rol;
import com.nativa.vecino_service.security.JwtUtil;
import com.nativa.vecino_service.security.SecurityConfig;
import com.nativa.vecino_service.service.VecinoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VecinoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
@WithMockUser(roles = "DIRECTIVA")
class VecinoControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private VecinoService vecinoService;
    @MockitoBean private JwtUtil jwtUtil;

    private VecinoResponse vecino() {
        return VecinoResponse.builder().id(1L).rut("12345678-5")
                .correoVecino("vecino@junta.cl").nombre("Ana").apellido("Pérez")
                .telefonoVecino("+56911111111").estadoVecino(true).rol(Rol.VECINO).build();
    }

    @Test
    void buscar_shouldReturnPaginatedResponse() throws Exception {
        when(vecinoService.buscar(isNull(), eq(true), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(vecino())));
        mockMvc.perform(get("/vecino/vecinos").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].rut").value("12345678-5"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void getById_shouldReturnVecino() throws Exception {
        when(vecinoService.getVecinoById(1L)).thenReturn(vecino());
        mockMvc.perform(get("/vecino/vecinos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("VECINO"));
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        when(vecinoService.createVecino(any())).thenReturn(vecino());
        mockMvc.perform(post("/vecino/vecinos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut":"12345678-5",
                                  "correoVecino":"vecino@junta.cl",
                                  "nombres":"Ana",
                                  "apellidos":"Pérez",
                                  "telefonoVecino":"+56911111111",
                                  "rol":"VECINO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "VECINO")
    void create_shouldRejectVecinoRole() throws Exception {
        mockMvc.perform(post("/vecino/vecinos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut":"12345678-5",
                                  "correoVecino":"vecino@junta.cl",
                                  "nombres":"Ana",
                                  "apellidos":"Pérez",
                                  "telefonoVecino":"+56911111111",
                                  "rol":"VECINO"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
