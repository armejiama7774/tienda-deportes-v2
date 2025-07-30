package com.tiendadeportiva.backend.repository;

import com.tiendadeportiva.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio Spring Data JPA para Producto
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2: Arquitectura Hexagonal
 * - Métodos únicos sin duplicación
 * - Consultas optimizadas para operaciones de negocio
 * - Siguiendo Google Java Style Guide
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // =============================================
    // MÉTODOS BÁSICOS PARA QUERIES
    // =============================================
    
    /**
     * Obtiene todos los productos activos ordenados por fecha de creación
     */
    List<Producto> findByActivoTrueOrderByFechaCreacionDesc();
    
    /**
     * Busca productos activos por categoría
     */
    List<Producto> findByCategoriaAndActivoTrue(String categoria);
    
    /**
     * Busca productos activos por marca
     */
    List<Producto> findByMarcaAndActivoTrue(String marca);
    
    /**
     * Busca productos activos que contengan el nombre especificado
     * ✅ MÉTODO ÚNICO - Sin duplicación
     */
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    
    // =============================================
    // MÉTODOS PARA BÚSQUEDAS AVANZADAS
    // =============================================
    
    /**
     * Busca productos activos en rango de precios ordenados por precio
     */
    List<Producto> findByPrecioBetweenAndActivoTrueOrderByPrecio(BigDecimal precioMin, BigDecimal precioMax);
    
    /**
     * Busca productos activos con stock disponible
     */
    List<Producto> findByStockDisponibleGreaterThanAndActivoTrue(Integer stock);
    
    // =============================================
    // MÉTODOS PARA ADAPTADOR HEXAGONAL
    // =============================================
    
    /**
     * Verifica si existe un producto activo con nombre y marca específicos
     */
    boolean existsByNombreAndMarcaAndActivoTrue(String nombre, String marca);
    
    /**
     * Busca productos activos por categoría ordenados por nombre
     */
    List<Producto> findByCategoriaAndActivoTrueOrderByNombre(String categoria);
    
    /**
     * Busca productos activos por marca ordenados por nombre
     */
    List<Producto> findByMarcaAndActivoTrueOrderByNombre(String marca);
    
    // =============================================
    // QUERIES PERSONALIZADAS PARA METADATOS
    // =============================================
    
    /**
     * Obtiene categorías distintas de productos activos
     */
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.activo = true ORDER BY p.categoria")
    List<String> findDistinctCategoriaByActivoTrue();
    
    /**
     * Obtiene marcas distintas de productos activos
     */
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.activo = true ORDER BY p.marca")
    List<String> findDistinctMarcaByActivoTrue();
}
