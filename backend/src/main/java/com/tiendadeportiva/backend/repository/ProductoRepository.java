package com.tiendadeportiva.backend.repository;

import com.tiendadeportiva.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos activos por categoría
     */
    List<Producto> findByCategoriaAndActivoTrue(String categoria);

    /**
     * Busca productos activos por marca
     */
    List<Producto> findByMarcaAndActivoTrue(String marca);

    /**
     * Busca productos por nombre que contenga el texto especificado (case-insensitive)
     */
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    /**
     * Busca productos en un rango de precios
     */
    List<Producto> findByPrecioBetweenAndActivoTrueOrderByPrecioAsc(BigDecimal precioMin, BigDecimal precioMax);

    /**
     * Busca productos con stock disponible
     */
    @Query("SELECT p FROM Producto p WHERE p.stockDisponible > 0 AND p.activo = true")
    List<Producto> findProductosConStock();

    /**
     * Obtiene todas las categorías únicas de productos activos
     */
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.activo = true")
    List<String> findDistinctCategorias();

    /**
     * Obtiene todas las marcas únicas de productos activos
     */
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.activo = true")
    List<String> findDistinctMarcas();

    /**
     * Busca productos activos ordenados por fecha de creación descendente (más recientes primero)
     */
    List<Producto> findByActivoTrueOrderByFechaCreacionDesc();

    /**
     * Verifica si existe un producto activo con el mismo nombre y marca
     * Útil para validaciones de negocio de duplicados
     */
    boolean existsByNombreAndMarcaAndActivoTrue(String nombre, String marca);

    /**
     * Verifica si existe un producto activo con el mismo nombre
     * Útil para validaciones de negocio de duplicados
     */
    boolean existsByNombreAndActivoTrue(String nombre);

    /**
     * Cuenta el número de productos activos
     */
    long countByActivoTrue();
}
