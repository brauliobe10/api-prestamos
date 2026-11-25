package com.bellodas.prestamoservice.exception;

import com.bellodas.prestamoservice.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, "Validación incorrecta", mensaje, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "Petición incorrecta", "El cuerpo JSON no es válido", request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> emailDuplicado(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> noEncontrado(ResourceNotFoundException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> credencialesInvalidas(AuthenticationException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.UNAUTHORIZED, "No autorizado", "Credenciales inválidas", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> errorGeneral(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado", request);
    }

    private ResponseEntity<ApiError> respuesta(HttpStatus estado, String error, String mensaje,
                                               HttpServletRequest request) {
        ApiError cuerpo = new ApiError(
                estado.value(),
                error,
                mensaje,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
