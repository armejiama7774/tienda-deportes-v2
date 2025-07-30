package com.tiendadeportiva.backend.exception;

/**
 * Excepción específica para operaciones de producto.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2: Arquitectura Hexagonal
 * - Excepciones del dominio independientes de infraestructura
 * - Códigos de error estructurados para diferentes capas
 * - Preparación para manejo centralizado de errores
 * - Siguiendo Google Java Style Guide
 */
public class ProductoException extends RuntimeException {
    
    private final String errorCode;
    
    public ProductoException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ProductoException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Obtiene el código de error específico
     * @return Código de error estructurado
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Método de compatibilidad para getCodigo()
     * ✅ CORRECCIÓN: Mantener compatibilidad con código existente
     */
    public String getCodigo() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return String.format("ProductoException{errorCode='%s', message='%s'}", 
                           errorCode, getMessage());
    }
}
