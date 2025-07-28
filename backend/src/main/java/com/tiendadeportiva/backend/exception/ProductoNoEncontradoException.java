package com.tiendadeportiva.backend.exception;

/**
 * Excepción específica para cuando no se encuentra un producto.
 * Extiende ProductoException para mantener consistencia en el manejo de errores.
 * 
 * @author Equipo Desarrollo
 * @version 1.0
 * @since Fase 1 - Monolito Modular
 */
public class ProductoNoEncontradoException extends ProductoException {
    
    /**
     * Constructor para producto no encontrado por ID
     * @param id Identificador del producto no encontrado
     */
    public ProductoNoEncontradoException(Long id) {
        super("PRODUCTO_NO_ENCONTRADO", 
              String.format("No se encontró el producto con ID: %d", id));
    }
    
    /**
     * Constructor para producto no encontrado con mensaje personalizado
     * @param mensaje Descripción específica del error
     */
    public ProductoNoEncontradoException(String mensaje) {
        super("PRODUCTO_NO_ENCONTRADO", mensaje);
    }
}
