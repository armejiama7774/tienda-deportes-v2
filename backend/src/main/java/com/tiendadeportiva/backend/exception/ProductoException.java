package com.tiendadeportiva.backend.exception;

/**
 * Excepción personalizada para errores relacionados con productos.
 * Aplicando el patrón de excepciones específicas del dominio.
 * 
 * Esta clase permite manejar errores de negocio de manera más granular
 * y proporcionar mensajes más descriptivos al cliente.
 * 
 * @author Equipo Desarrollo
 * @version 1.0
 * @since Fase 1 - Monolito Modular
 */
public class ProductoException extends RuntimeException {
    
    private final String codigo;
    
    /**
     * Constructor con mensaje personalizado
     * @param mensaje Descripción del error
     */
    public ProductoException(String mensaje) {
        super(mensaje);
        this.codigo = "PRODUCTO_ERROR";
    }
    
    /**
     * Constructor con mensaje y código de error
     * @param codigo Código identificador del error
     * @param mensaje Descripción del error
     */
    public ProductoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
    
    /**
     * Constructor con mensaje y causa
     * @param mensaje Descripción del error
     * @param causa Excepción que causó este error
     */
    public ProductoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = "PRODUCTO_ERROR";
    }
    
    /**
     * Constructor completo
     * @param codigo Código identificador del error
     * @param mensaje Descripción del error
     * @param causa Excepción que causó este error
     */
    public ProductoException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }
    
    public String getCodigo() {
        return codigo;
    }
}
