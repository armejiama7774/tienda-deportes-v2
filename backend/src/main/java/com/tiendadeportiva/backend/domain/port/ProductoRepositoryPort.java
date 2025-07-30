package com.tiendadeportiva.backend.domain.port;

import com.tiendadeportiva.backend.model.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Puerto del dominio para operaciones de persistencia de productos.
 * 
 * ARQUITECTURA HEXAGONAL - Fase 2:
 * - Define el contrato que necesita el dominio
 * - Independiente de tecnología de persistencia
 * - Facilita testing con implementaciones mock
 * - Permite cambiar proveedores de BD sin afectar dominio
 */
public interface ProductoRepositoryPort {
    
    // =============================================
    // OPERACIONES BÁSICAS DE PERSISTENCIA
    // =============================================
    
    /**
     * Persiste un producto en el almacén de datos
     * @param producto Producto a persistir
     * @return Producto persistido con ID asignado
     */
    Producto save(Producto producto);
    
    /**
     * Busca un producto por su identificador
     * @param id Identificador del producto
     * @return Optional con el producto si existe
     */
    Optional<Producto> findById(Long id);
    
    /**
     * Verifica si existe un producto con el ID dado
     * @param id Identificador del producto
     * @return true si existe
     */
    boolean existsById(Long id);
    
    /**
     * Elimina un producto del almacén (hard delete)
     * @param id Identificador del producto
     */
    void deleteById(Long id);
    
    // =============================================
    // CONSULTAS DE NEGOCIO ESPECÍFICAS
    // =============================================
    
    /**
     * Obtiene todos los productos activos ordenados por fecha de creación
     * @return Lista de productos activos
     */
    List<Producto> findAllActiveOrderByCreationDate();
    
    /**
     * Verifica si existe un producto activo con nombre y marca específicos
     * @param nombre Nombre del producto
     * @param marca Marca del producto
     * @return true si existe un producto activo con esos datos
     */
    boolean existsActiveByNameAndBrand(String nombre, String marca);
    
    /**
     * Busca productos activos por categoría
     * @param categoria Categoría a buscar
     * @return Lista de productos de la categoría
     */
    List<Producto> findActiveByCategoryOrderByName(String categoria);
    
    /**
     * Busca productos activos por marca
     * @param marca Marca a buscar
     * @return Lista de productos de la marca
     */
    List<Producto> findActiveByBrandOrderByName(String marca);
    
    /**
     * Busca productos activos que contengan el nombre especificado
     * @param nombre Texto a buscar en el nombre
     * @return Lista de productos que coinciden
     */
    List<Producto> findActiveByNameContainingIgnoreCase(String nombre);
    
    /**
     * Busca productos activos en un rango de precios
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de productos en el rango
     */
    List<Producto> findActiveByPriceRangeOrderByPrice(BigDecimal precioMin, BigDecimal precioMax);
    
    /**
     * Obtiene productos activos con stock disponible (> 0)
     * @return Lista de productos con stock
     */
    List<Producto> findActiveWithStock();
    
    /**
     * Obtiene todas las categorías de productos activos
     * @return Lista de categorías únicas
     */
    List<String> findDistinctActiveCategories();
    
    /**
     * Obtiene todas las marcas de productos activos
     * @return Lista de marcas únicas
     */
    List<String> findDistinctActiveBrands();
}