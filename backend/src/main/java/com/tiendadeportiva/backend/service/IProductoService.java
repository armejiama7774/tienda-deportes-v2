package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.model.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para los servicios de productos.
 * Aplicando el Principio de Inversión de Dependencias (DIP) de SOLID.
 * 
 * Esta abstracción permite:
 * - Desacoplar el controlador de la implementación específica
 * - Facilitar el testing con mocks
 * - Permitir múltiples implementaciones sin afectar clientes
 * - Mejorar la mantenibilidad y extensibilidad
 * 
 * @author Equipo Desarrollo
 * @version 1.0
 * @since Fase 1 - Monolito Modular
 */
public interface IProductoService {
    
    /**
     * Obtiene todos los productos activos del sistema
     * @return Lista de productos disponibles
     */
    List<Producto> obtenerTodosLosProductos();
    
    /**
     * Busca un producto por su identificador único
     * @param id Identificador del producto
     * @return Optional con el producto si existe, empty si no se encuentra
     */
    Optional<Producto> obtenerProductoPorId(Long id);
    
    /**
     * Crea un nuevo producto en el sistema
     * @param producto Datos del producto a crear
     * @return Producto creado con ID asignado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    Producto crearProducto(Producto producto);
    
    /**
     * Actualiza un producto existente
     * @param id Identificador del producto a actualizar
     * @param productoActualizado Nuevos datos del producto
     * @return Optional con el producto actualizado si existe
     * @throws IllegalArgumentException si los datos son inválidos
     */
    Optional<Producto> actualizarProducto(Long id, Producto productoActualizado);
    
    /**
     * Elimina un producto (soft delete)
     * @param id Identificador del producto a eliminar
     * @return true si se eliminó correctamente, false si no existe
     */
    boolean eliminarProducto(Long id);
    
    /**
     * Busca productos por categoría
     * @param categoria Categoría a filtrar
     * @return Lista de productos de la categoría especificada
     */
    List<Producto> buscarPorCategoria(String categoria);
    
    /**
     * Busca productos por marca
     * @param marca Marca a filtrar
     * @return Lista de productos de la marca especificada
     */
    List<Producto> buscarPorMarca(String marca);
    
    /**
     * Busca productos por nombre (búsqueda parcial)
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de productos que coinciden con el criterio
     */
    List<Producto> buscarPorNombre(String nombre);
    
    /**
     * Busca productos en un rango de precios
     * @param precioMinimo Precio mínimo del rango
     * @param precioMaximo Precio máximo del rango
     * @return Lista de productos en el rango especificado
     */
    List<Producto> buscarPorRangoPrecios(BigDecimal precioMinimo, BigDecimal precioMaximo);
    
    /**
     * Obtiene productos que tienen stock disponible
     * @return Lista de productos con stock > 0
     */
    List<Producto> obtenerProductosConStock();
    
    /**
     * Obtiene todas las categorías disponibles en el sistema
     * @return Lista de categorías únicas
     */
    List<String> obtenerCategorias();
    
    /**
     * Obtiene todas las marcas disponibles en el sistema
     * @return Lista de marcas únicas
     */
    List<String> obtenerMarcas();
    
    /**
     * Actualiza el stock de un producto específico
     * @param id Identificador del producto
     * @param nuevoStock Cantidad de stock a establecer
     * @return true si se actualizó correctamente, false si no existe el producto
     * @throws IllegalArgumentException si el stock es negativo
     */
    boolean actualizarStock(Long id, Integer nuevoStock);
}
