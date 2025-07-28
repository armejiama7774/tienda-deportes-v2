package com.tiendadeportiva.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Implementa el patrón de manejo centralizado de errores.
 * 
 * Beneficios:
 * - Consistencia en las respuestas de error
 * - Separación de responsabilidades
 * - Código más limpio en los controladores
 * - Logging centralizado de errores
 * 
 * @author Equipo Desarrollo
 * @version 1.0
 * @since Fase 1 - Monolito Modular con SOLID
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones específicas de productos
     */
    @ExceptionHandler(ProductoException.class)
    public ResponseEntity<ErrorResponse> handleProductoException(ProductoException e) {
        logger.warn("Error de producto: [{}] {}", e.getCodigo(), e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            e.getCodigo(),
            e.getMessage(),
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de producto no encontrado
     */
    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleProductoNoEncontradoException(ProductoNoEncontradoException e) {
        logger.warn("Producto no encontrado: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            e.getCodigo(),
            e.getMessage(),
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja errores de validación de Bean Validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        logger.warn("Error de validación: {}", e.getMessage());
        
        Map<String, String> errores = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errores.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Errores de validación en los datos enviados",
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            errores
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones generales no capturadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logger.error("Error inesperado en la aplicación", e);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "Error interno del servidor. Por favor contacte al administrador.",
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    /**
     * Clase interna para estructurar las respuestas de error
     */
    public static class ErrorResponse {
        private String codigo;
        private String mensaje;
        private LocalDateTime timestamp;
        private int status;
        private Map<String, String> detalles;

        public ErrorResponse(String codigo, String mensaje, LocalDateTime timestamp, int status) {
            this.codigo = codigo;
            this.mensaje = mensaje;
            this.timestamp = timestamp;
            this.status = status;
        }

        public ErrorResponse(String codigo, String mensaje, LocalDateTime timestamp, int status, Map<String, String> detalles) {
            this(codigo, mensaje, timestamp, status);
            this.detalles = detalles;
        }

        // Getters y setters
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }

        public Map<String, String> getDetalles() { return detalles; }
        public void setDetalles(Map<String, String> detalles) { this.detalles = detalles; }
    }
}
