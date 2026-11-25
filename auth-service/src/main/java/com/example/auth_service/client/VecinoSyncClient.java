package com.example.auth_service.client;

import org.springframework.stereotype.Component;
import com.example.auth_service.client.dto.VecinoSyncRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VecinoSyncClient {
    private final VecinoClient vecinoClient;

    public void sincronizar(String rut, String nombre, String telefono) {
        if (rut == null || rut.isBlank() || nombre == null || nombre.isBlank()) {
            return;
        }
        try {
            vecinoClient.sincronizar(new VecinoSyncRequest(rut, nombre, telefono));
        } catch (RuntimeException e) {
            log.warn("No se pudo sincronizar el RUT {} con vecino-service: {}", rut, e.getMessage());
        }
    }
}
