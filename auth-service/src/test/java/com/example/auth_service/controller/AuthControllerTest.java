package com.example.auth_service.controller;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.RegisterResponse;
import com.example.auth_service.model.Rol;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtUtil jwtUtil;

    private AuthResponse tokens() {
        return AuthResponse.builder()
                .usuarioId(1L).rut("111111111").nombre("Vecino Demo")
                .token("access").accessToken("access").refreshToken("refresh")
                .tokenType("Bearer").expiresIn(900).rol(Rol.VECINO)
                .build();
    }

    @Test
    void login_shouldReturnTokenPair() throws Exception {
        when(authService.login("11.111.111-1", "clave123")).thenReturn(tokens());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"11.111.111-1\",\"password\":\"clave123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.nombre").value("Vecino Demo"))
                .andExpect(jsonPath("$.rol").value("VECINO"));
    }

    @Test
    void login_shouldReturnUnauthorized() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"11.111.111-1\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_shouldAcceptAndroidProfile() throws Exception {
        when(authService.register(any())).thenReturn(RegisterResponse.builder()
                .usuarioId(2L)
                .rut("222222222")
                .nombre("Vecino Prueba")
                .mensaje("Usuario creado exitosamente")
                .build());

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "22.222.222-2",
                                  "nombre": "Vecino Prueba",
                                  "telefono": "9 2222 2222",
                                  "region": "Región Metropolitana",
                                  "comuna": "Ñuñoa",
                                  "password": "clave123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(2))
                .andExpect(jsonPath("$.rut").value("222222222"))
                .andExpect(jsonPath("$.nombre").value("Vecino Prueba"));
    }

    @Test
    void refresh_shouldReturnRotatedPair() throws Exception {
        when(authService.refresh("refresh-anterior")).thenReturn(tokens());
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-anterior\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void logout_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh\"}"))
                .andExpect(status().isNoContent());
        verify(authService).logout("refresh");
    }
}
