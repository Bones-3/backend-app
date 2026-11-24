package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.dto.RegisterResponse;
import com.example.auth_service.exception.UnauthorizedException;
import com.example.auth_service.model.Credencial;
import com.example.auth_service.model.Rol;
import com.example.auth_service.model.Token;
import com.example.auth_service.repository.CredencialRepository;
import com.example.auth_service.repository.TokenRepository;
import com.example.auth_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private CredencialRepository credencialRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthService authService;

    private Credencial credencial;

    @BeforeEach
    void setUp() {
        credencial = new Credencial();
        credencial.setIdCredencial(1L);
        credencial.setCorreoUser("111111111@rut.local");
        credencial.setRutUser("111111111");
        credencial.setNombre("Vecino Demo");
        credencial.setTelefono("911111111");
        credencial.setRegion("Región Metropolitana");
        credencial.setComuna("Santiago");
        credencial.setContrasenaUser("hash");
        credencial.setActivo(true);
        credencial.setRol(Rol.VECINO);
    }

    private void mockTokens() {
        when(jwtUtil.generarAccessToken("111111111", Rol.VECINO)).thenReturn("access");
        when(jwtUtil.generarRefreshToken("111111111", Rol.VECINO)).thenReturn("refresh-nuevo");
        when(jwtUtil.extraerExpiracion("refresh-nuevo")).thenReturn(LocalDateTime.now().plusDays(7));
        when(jwtUtil.getAccessExpirationSeconds()).thenReturn(900L);
    }

    @Test
    void login_shouldReturnAccessAndRefresh() {
        when(credencialRepository.findByRutUser("111111111")).thenReturn(Optional.of(credencial));
        when(passwordEncoder.matches("clave123", "hash")).thenReturn(true);
        mockTokens();

        AuthResponse response = authService.login("11.111.111-1", "clave123");

        assertThat(response.getToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-nuevo");
        assertThat(response.getUsuarioId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Vecino Demo");
        assertThat(response.getRol()).isEqualTo(Rol.VECINO);
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void login_shouldReturnNullForInvalidPassword() {
        when(credencialRepository.findByRutUser("111111111")).thenReturn(Optional.of(credencial));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);
        assertThat(authService.login("11.111.111-1", "mala")).isNull();
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void register_shouldAlwaysCreateVecinoRole() {
        RegisterRequest request = registroValido();
        when(credencialRepository.findByRutUser("222222222")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave123")).thenReturn("hash");
        when(credencialRepository.save(any(Credencial.class))).thenAnswer(invocation -> {
            Credencial guardada = invocation.getArgument(0);
            guardada.setIdCredencial(2L);
            return guardada;
        });

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<Credencial> captor = ArgumentCaptor.forClass(Credencial.class);
        verify(credencialRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo(Rol.VECINO);
        assertThat(captor.getValue().getRutUser()).isEqualTo("222222222");
        assertThat(response.getUsuarioId()).isEqualTo(2L);
        assertThat(response.getNombre()).isEqualTo("Vecino Prueba");
    }

    @Test
    void register_shouldRejectInvalidRut() {
        RegisterRequest request = registroValido();
        request.setRut("12.345.678-9");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(com.example.auth_service.exception.BadRequestException.class)
                .hasMessage("RUT inválido");
        verifyNoInteractions(credencialRepository, passwordEncoder);
    }

    private RegisterRequest registroValido() {
        RegisterRequest request = new RegisterRequest();
        request.setRut("22.222.222-2");
        request.setNombre("Vecino Prueba");
        request.setTelefono("922222222");
        request.setRegion("Región Metropolitana");
        request.setComuna("Ñuñoa");
        request.setPassword("clave123");
        return request;
    }

    @Test
    void refresh_shouldRotateToken() {
        Token anterior = new Token(1L, "refresh-anterior", LocalDateTime.now().plusDays(1), true, credencial);
        when(tokenRepository.findByTokenAndActivoTrue("refresh-anterior")).thenReturn(Optional.of(anterior));
        when(jwtUtil.validarToken("refresh-anterior", JwtUtil.REFRESH)).thenReturn(true);
        mockTokens();

        AuthResponse response = authService.refresh("refresh-anterior");

        assertThat(anterior.getActivo()).isFalse();
        assertThat(response.getRefreshToken()).isEqualTo("refresh-nuevo");
        verify(tokenRepository, times(2)).save(any(Token.class));
    }

    @Test
    void refresh_shouldRejectExpiredToken() {
        Token token = new Token(1L, "expirado", LocalDateTime.now().minusMinutes(1), true, credencial);
        when(tokenRepository.findByTokenAndActivoTrue("expirado")).thenReturn(Optional.of(token));
        when(jwtUtil.validarToken("expirado", JwtUtil.REFRESH)).thenReturn(true);
        assertThatThrownBy(() -> authService.refresh("expirado"))
                .isInstanceOf(UnauthorizedException.class);
        assertThat(token.getActivo()).isFalse();
    }

    @Test
    void logout_shouldRevokeRefreshToken() {
        Token token = new Token(1L, "refresh", LocalDateTime.now().plusDays(1), true, credencial);
        when(tokenRepository.findByTokenAndActivoTrue("refresh")).thenReturn(Optional.of(token));
        authService.logout("refresh");
        assertThat(token.getActivo()).isFalse();
        verify(tokenRepository).save(token);
    }
}
