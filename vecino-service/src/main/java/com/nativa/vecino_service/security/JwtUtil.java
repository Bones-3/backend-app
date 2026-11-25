package com.nativa.vecino_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims claims(String token) {
        String jwt = token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(jwt).getPayload();
    }

    public boolean validarToken(String token) {
        try {
            return "access".equals(claims(token).get("tipo", String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String extraerCorreo(String token) {
        return claims(token).getSubject();
    }

    public Long extraerVecinoId(String token) {
        Number valor = claims(token).get("vecinoId", Number.class);
        return valor == null ? null : valor.longValue();
    }

    public String extraerRol(String token) {
        return claims(token).get("rol", String.class);
    }
}
