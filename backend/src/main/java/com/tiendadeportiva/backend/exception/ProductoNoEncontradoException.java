package com.tiendadeportiva.backend.exception;

/**
 * Excepción específica cuando no se encuentra un producto.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2: Arquitectura Hexagonal
 * - Excepción específica del dominio
 * - Hereda comportamiento de ProductoException
 * - Códigos de error estandarizados
 */
public class ProductoNoEncontradoException extends ProductoException {
    
    private static final String ERROR_CODE = "PRODUCTO_NO_ENCONTRADO";
    
    public ProductoNoEncontradoException(Long id) {
        super(ERROR_CODE, String.format("No se encontró producto con ID: %d", id));
    }
    
    public ProductoNoEncontradoException(String nombre) {
        super(ERROR_CODE, String.format("No se encontró producto con nombre: %s", nombre));
    }
    
    public ProductoNoEncontradoException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
    
    /**
     * Método de compatibilidad para getCodigo()
     * ✅ CORRECCIÓN: Mantener compatibilidad con código existente
     */
    public String getCodigo() {
        return getErrorCode();
    }
}
