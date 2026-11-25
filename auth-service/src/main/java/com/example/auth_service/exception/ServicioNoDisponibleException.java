package com.example.auth_service.exception;

public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String message) {
        super(message);
    }

    public ServicioNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
