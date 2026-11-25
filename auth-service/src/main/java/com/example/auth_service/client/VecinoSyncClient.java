package com.example.auth_service.client;

import org.springframework.stereotype.Component;
import com.example.auth_service.client.dto.VecinoSyncRequest;
import com.example.auth_service.client.dto.VecinoSyncResponse;
import com.example.auth_service.exception.BadRequestException;
import com.example.auth_service.exception.ServicioNoDisponibleException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VecinoSyncClient {
    private final VecinoClient vecinoClient;

    public Long obtenerVecinoId(String rut, String nombre, String telefono) {
        if (rut == null || rut.isBlank()) {
            throw new BadRequestException("La cuenta no tiene RUT asociado");
        }
        String nombreFinal = (nombre == null || nombre.isBlank()) ? rut : nombre;
        try {
            VecinoSyncResponse respuesta = vecinoClient
                    .sincronizar(new VecinoSyncRequest(rut, nombreFinal, telefono));
            if (respuesta == null || respuesta.id() == null) {
                throw new ServicioNoDisponibleException("vecino-service no devolvió el id del vecino");
            }
            return respuesta.id();
        } catch (ServicioNoDisponibleException e) {
            throw e;
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                log.warn("vecino-service rechazó la sincronización del RUT {}: {}", rut, e.getMessage());
                throw new BadRequestException("No se pudo registrar al vecino: datos inválidos");
            }
            log.error("Error de vecino-service al sincronizar el RUT {}: {}", rut, e.getMessage());
            throw new ServicioNoDisponibleException("El servicio de vecinos no está disponible. Intente más tarde.", e);
        } catch (RuntimeException e) {
            log.error("No se pudo contactar a vecino-service para el RUT {}: {}", rut, e.getMessage());
            throw new ServicioNoDisponibleException("El servicio de vecinos no está disponible. Intente más tarde.", e);
        }
    }
}
