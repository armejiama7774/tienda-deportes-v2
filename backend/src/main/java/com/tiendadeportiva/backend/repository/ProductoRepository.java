package com.tiendadeportiva.backend.repository;

import com.tiendadeportiva.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para la entidad Producto.
 * Implementa el patrón Repository usando Spring Data JPA.
 * En fases posteriores evolucionará hacia una implementación más sofisticada.
 */
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
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.activo = true")
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(@Param("nombre") String nombre);

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
     * Busca productos por categoría y marca
     */
    List<Producto> findByCategoriaAndMarcaAndActivoTrue(String categoria, String marca);

    /**
     * Obtiene todas las categorías únicas de productos activos
     */
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.activo = true ORDER BY p.categoria")
    List<String> findDistinctCategorias();

    /**
     * Obtiene todas las marcas únicas de productos activos
     */
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.activo = true ORDER BY p.marca")
    List<String> findDistinctMarcas();

    /**
     * Busca productos activos ordenados por precio ascendente
     */
    List<Producto> findByActivoTrueOrderByPrecioAsc();

    /**
     * Busca productos activos ordenados por fecha de creación descendente (más recientes primero)
     */
    List<Producto> findByActivoTrueOrderByFechaCreacionDesc();
}
