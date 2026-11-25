package com.example.auth_service.client;

import org.springframework.context.annotation.Bean;
import com.example.auth_service.model.Rol;
import com.example.auth_service.security.JwtUtil;
import feign.RequestInterceptor;

public class VecinoClientConfig {
    @Bean
    public RequestInterceptor serviceTokenInterceptor(JwtUtil jwtUtil) {
        return template -> template.header("Authorization",
                "Bearer " + jwtUtil.generarAccessToken("auth-service", Rol.SECRETARIO));
    }
}
