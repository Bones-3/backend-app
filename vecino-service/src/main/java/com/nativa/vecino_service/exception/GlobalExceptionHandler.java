package com.nativa.vecino_service.exception;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 400 -
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException exception,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    // 400 - Errores de validacion
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handlerValidation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String mensajeErrores = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String mensajeFinal = "Errores de validación -> " + mensajeErrores;
        log.warn("400 Validation: {} | path: {}", mensajeErrores, request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, mensajeFinal, request);
    }

    // 404 - Recurso no encontrado
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Ruta no encontrada.", request);
    }

    // 409 - Violacion de integridad de base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        String causa = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
        log.warn("Integridad de datos: {} | path: {}", causa, request.getRequestURI());
        if (causa.contains("duplicate") || causa.contains("unique")) {
            return build(HttpStatus.CONFLICT, "Ya existe un vecino con ese RUT o correo.", request);
        }
        if (causa.contains("data too long") || causa.contains("value too long")) {
            return build(HttpStatus.BAD_REQUEST, "Alguno de los valores supera el largo permitido.", request);
        }
        return build(HttpStatus.CONFLICT,
                "No se puede completar la operación: el registro está referenciado por otros datos o viola una restricción.",
                request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación.", request);
    }

    // 500 - Error Interno inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception exception, HttpServletRequest request) {
        log.error("500 Error inesperado en {} {}", request.getMethod(), request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno inesperado. Contacte al administrador.", request);
    }

    // Ayudante de respuesta unificado (DRY) utilizando OffsetDateTime
    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now()) // Mejor para auditoría y zonas horarias (UTC)
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(body, status);
    }

}
