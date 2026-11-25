package com.example.auth_service.security;

import com.example.auth_service.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration:900000}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarAccessToken(String identidad, Rol rol) {
        return generarToken(identidad, rol, ACCESS, accessExpiration, null);
    }

    /**
     * Incluye el claim "vecinoId" para que los demas servicios identifiquen al
     * usuario sin confiar en el cliente.
     */
    public String generarAccessToken(String identidad, Rol rol, Long vecinoId) {
        return generarToken(identidad, rol, ACCESS, accessExpiration, vecinoId);
    }

    public String generarRefreshToken(String identidad, Rol rol) {
        return generarToken(identidad, rol, REFRESH, refreshExpiration, null);
    }

    private String generarToken(String identidad, Rol rol, String tipo, long duracion, Long vecinoId) {
        Date ahora = new Date();
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(identidad)
                .claim("rol", rol.name())
                .claim("tipo", tipo);
        if (vecinoId != null) {
            builder.claim("vecinoId", vecinoId);
        }
        return builder
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion))
                .signWith(signingKey())
                .compact();
    }

    public Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(limpiar(token))
                .getPayload();
    }

    public boolean validarToken(String token, String tipoEsperado) {
        try {
            return tipoEsperado.equals(claims(token).get("tipo", String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String extraerCorreo(String token) {
        return claims(token).getSubject();
    }

    public Rol extraerRol(String token) {
        return Rol.valueOf(claims(token).get("rol", String.class));
    }

    public LocalDateTime extraerExpiracion(String token) {
        return LocalDateTime.ofInstant(claims(token).getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public long getAccessExpirationSeconds() {
        return accessExpiration / 1000;
    }

    private String limpiar(String token) {
        return token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}
