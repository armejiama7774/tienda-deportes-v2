package com.tiendadeportiva.backend.factory;

import com.tiendadeportiva.backend.model.Producto;

/**
 * Interfaz base para el Factory Pattern de productos.
 * 
 * PATRÓN FACTORY - NIVEL PROFESIONAL:
 * Define el contrato para crear diferentes tipos de productos
 * con validaciones y configuraciones específicas por categoría.
 * 
 * VENTAJAS PROFESIONALES:
 * - Encapsula lógica de creación compleja
 * - Facilita extensión (nuevos tipos de productos)
 * - Centraliza validaciones específicas por tipo
 * - Mejora testabilidad (mock factories)
 * - Preparación para configuración dinámica
 */
public interface ProductoFactory {
    
    /**
     * Crea un producto con validaciones específicas del tipo.
     * 
     * @param request Datos completos para crear el producto
     * @return Producto creado y validado
     * @throws ProductoCreationException Si hay errores en la creación
     */
    Producto crearProducto(ProductoCreationRequest request);
    
    /**
     * Indica si esta factory puede manejar el tipo de producto solicitado.
     * 
     * @param categoria Categoría del producto
     * @param tipo Tipo específico dentro de la categoría
     * @return true si puede crear este tipo de producto
     */
    boolean puedeCrear(String categoria, String tipo);
    
    /**
     * Obtiene los tipos de productos que puede crear esta factory.
     * 
     * @return Array de tipos soportados
     */
    String[] getTiposSoportados();
    
    /**
     * Obtiene la categoría principal que maneja esta factory.
     * 
     * @return Categoría principal (CALZADO, ROPA, ACCESORIOS, etc.)
     */
    String getCategoriaPrincipal();
    
    /**
     * Valida los datos específicos para el tipo de producto.
     * 
     * @param request Datos a validar
     * @throws ProductoValidationException Si los datos no son válidos
     */
    void validarDatos(ProductoCreationRequest request);
    
    /**
     * Aplica configuraciones por defecto específicas del tipo.
     * 
     * @param producto Producto a configurar
     * @param request Datos originales de la petición
     */
    void aplicarConfiguracionesDefecto(Producto producto, ProductoCreationRequest request);
}
