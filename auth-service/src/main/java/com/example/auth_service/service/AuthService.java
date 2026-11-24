package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.dto.RegisterResponse;
import com.example.auth_service.exception.BadRequestException;
import com.example.auth_service.exception.UnauthorizedException;
import com.example.auth_service.model.Credencial;
import com.example.auth_service.model.Rol;
import com.example.auth_service.model.Token;
import com.example.auth_service.repository.CredencialRepository;
import com.example.auth_service.repository.TokenRepository;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.util.RutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final CredencialRepository credencialRepository;
    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse login(String rut, String password) {
        String rutNormalizado = RutUtils.normalizar(rut);
        Optional<Credencial> credencialOpt = credencialRepository.findByRutUser(rutNormalizado);
        if (credencialOpt.isEmpty()) {
            return null;
        }
        Credencial credencial = credencialOpt.get();
        if (!Boolean.TRUE.equals(credencial.getActivo())
                || !passwordEncoder.matches(password, credencial.getContrasenaUser())) {
            return null;
        }
        tokenRepository.revocarActivosPorCredencial(credencial.getIdCredencial());
        return emitirTokens(credencial);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        Token persistido = tokenRepository.findByTokenAndActivoTrue(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido o revocado"));
        if (!jwtUtil.validarToken(refreshToken, JwtUtil.REFRESH)
                || persistido.getFechaExpiracion().isBefore(java.time.LocalDateTime.now())) {
            persistido.setActivo(false);
            tokenRepository.save(persistido);
            throw new UnauthorizedException("Refresh token expirado o inválido");
        }
        persistido.setActivo(false);
        tokenRepository.save(persistido);
        return emitirTokens(persistido.getCredencial());
    }

    @Transactional
    public void logout(String refreshToken) {
        tokenRepository.findByTokenAndActivoTrue(refreshToken).ifPresent(token -> {
            token.setActivo(false);
            tokenRepository.save(token);
        });
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String rut = RutUtils.normalizar(request.getRut());
        if (!RutUtils.esValido(rut)) {
            throw new BadRequestException("RUT inválido");
        }
        if (credencialRepository.findByRutUser(rut).isPresent()) {
            throw new BadRequestException("Ese RUT ya está registrado");
        }

        Credencial nueva = new Credencial();
        // Se conserva la columna histórica de correo para bases ya existentes.
        // La identidad pública de la app es exclusivamente el RUT.
        nueva.setCorreoUser(rut.toLowerCase() + "@rut.local");
        nueva.setRutUser(rut);
        nueva.setNombre(request.getNombre().trim());
        nueva.setTelefono(request.getTelefono().trim());
        nueva.setRegion(request.getRegion().trim());
        nueva.setComuna(request.getComuna().trim());
        nueva.setContrasenaUser(passwordEncoder.encode(request.getPassword()));
        nueva.setActivo(true);
        nueva.setRol(Rol.VECINO);
        Credencial guardada = credencialRepository.save(nueva);
        return RegisterResponse.builder()
                .usuarioId(guardada.getIdCredencial())
                .rut(guardada.getRutUser())
                .nombre(guardada.getNombre())
                .mensaje("Usuario creado exitosamente")
                .build();
    }

    private AuthResponse emitirTokens(Credencial credencial) {
        String identidad = credencial.getRutUser() != null
                ? credencial.getRutUser()
                : credencial.getCorreoUser();
        String accessToken = jwtUtil.generarAccessToken(identidad, credencial.getRol());
        String refreshToken = jwtUtil.generarRefreshToken(identidad, credencial.getRol());
        Token token = new Token();
        token.setToken(refreshToken);
        token.setFechaExpiracion(jwtUtil.extraerExpiracion(refreshToken));
        token.setActivo(true);
        token.setCredencial(credencial);
        tokenRepository.save(token);
        return AuthResponse.builder()
                .usuarioId(credencial.getIdCredencial())
                .token(accessToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationSeconds())
                .rut(credencial.getRutUser())
                .nombre(credencial.getNombre())
                .telefono(credencial.getTelefono())
                .region(credencial.getRegion())
                .comuna(credencial.getComuna())
                .correo(credencial.getCorreoUser())
                .rol(credencial.getRol())
                .build();
    }
}
