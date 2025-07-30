package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.model.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de productos.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2: Arquitectura Hexagonal
 * - Separación clara entre comandos y queries
 * - Preparación para CQRS en fases futuras
 * - API completa para operaciones de negocio
 * - Siguiendo principios SOLID del proyecto evolutivo
 */
public interface IProductoService {
    
    // =============================================
    // COMANDOS (OPERACIONES DE ESCRITURA)
    // =============================================
    
    /**
     * Crea un nuevo producto
     * @param producto Producto a crear
     * @return Producto creado con ID asignado
     */
    Producto crearProducto(Producto producto);
    
    /**
     * Actualiza un producto existente
     * @param id ID del producto a actualizar
     * @param productoActualizado Datos actualizados
     * @return Optional con el producto actualizado si existe
     */
    Optional<Producto> actualizarProducto(Long id, Producto productoActualizado);
    
    /**
     * Elimina un producto (soft delete)
     * @param id ID del producto a eliminar
     * @return true si se eliminó exitosamente
     */
    boolean eliminarProducto(Long id);
    
    /**
     * Actualiza el stock de un producto
     * @param id ID del producto
     * @param nuevoStock Nuevo valor de stock
     * @return true si se actualizó exitosamente
     */
    boolean actualizarStock(Long id, Integer nuevoStock);
    
    // =============================================
    // QUERIES BÁSICAS (OPERACIONES DE LECTURA)
    // =============================================
    
    /**
     * Obtiene todos los productos activos
     * @return Lista de productos activos ordenados por fecha de creación
     */
    List<Producto> obtenerTodosLosProductos();
    
    /**
     * Obtiene un producto por su ID
     * @param id ID del producto
     * @return Optional con el producto si existe y está activo
     */
    Optional<Producto> obtenerProductoPorId(Long id);
    
    // =============================================
    // QUERIES DE FILTRADO (BÚSQUEDAS ESPECÍFICAS)
    // =============================================
    
    /**
     * Busca productos por categoría
     * @param categoria Categoría a buscar
     * @return Lista de productos de la categoría ordenados por nombre
     */
    List<Producto> buscarPorCategoria(String categoria);
    
    /**
     * Busca productos por marca
     * @param marca Marca a buscar
     * @return Lista de productos de la marca ordenados por nombre
     */
    List<Producto> buscarPorMarca(String marca);
    
    /**
     * Busca productos por nombre (contiene texto)
     * @param nombre Texto a buscar en el nombre
     * @return Lista de productos que coinciden ordenados por relevancia
     */
    List<Producto> buscarPorNombre(String nombre);
    
    /**
     * Busca productos en un rango de precios
     * @param precioMin Precio mínimo (inclusive)
     * @param precioMax Precio máximo (inclusive) 
     * @return Lista de productos en el rango ordenados por precio
     */
    List<Producto> buscarPorRangoPrecios(BigDecimal precioMin, BigDecimal precioMax);
    
    /**
     * Obtiene productos que tienen stock disponible
     * @return Lista de productos con stock > 0 ordenados por stock descendente
     */
    List<Producto> obtenerProductosConStock();
    
    // =============================================
    // QUERIES DE METADATOS (INFORMACIÓN DEL CATÁLOGO)
    // =============================================
    
    /**
     * Obtiene todas las marcas disponibles
     * @return Lista de marcas únicas de productos activos
     */
    List<String> obtenerMarcas();
    
    /**
     * Obtiene todas las categorías disponibles
     * @return Lista de categorías únicas de productos activos
     */
    List<String> obtenerCategorias();
}
