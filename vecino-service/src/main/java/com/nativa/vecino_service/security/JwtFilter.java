package com.nativa.vecino_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component("jwtFilter")
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final Set<String> ROLES = Set.of("DIRECTIVA", "TESORERO", "SECRETARIO", "VECINO");
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validarToken(token)) {
                String rol = jwtUtil.extraerRol(token);
                if (ROLES.contains(rol)) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            jwtUtil.extraerCorreo(token),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                    auth.setDetails(jwtUtil.extraerVecinoId(token));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
