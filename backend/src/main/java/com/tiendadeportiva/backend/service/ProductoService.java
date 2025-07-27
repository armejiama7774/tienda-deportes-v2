package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de productos.
 * Implementa la lógica de negocio para el dominio de productos.
 * En fases posteriores evolucionará hacia una arquitectura hexagonal.
 */
@Service
@Transactional
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Obtiene todos los productos activos
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        logger.debug("Obteniendo todos los productos activos");
        return productoRepository.findByActivoTrueOrderByFechaCreacionDesc();
    }

    /**
     * Busca un producto por su ID
     */
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        logger.debug("Buscando producto con ID: {}", id);
        return productoRepository.findById(id);
    }

    /**
     * Crea un nuevo producto
     */
    public Producto crearProducto(Producto producto) {
        logger.info("Creando nuevo producto: {}", producto.getNombre());
        
        // Validaciones de negocio
        validarProducto(producto);
        
        Producto productoGuardado = productoRepository.save(producto);
        logger.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());
        
        return productoGuardado;
    }

    /**
     * Actualiza un producto existente
     */
    public Optional<Producto> actualizarProducto(Long id, Producto productoActualizado) {
        logger.info("Actualizando producto con ID: {}", id);
        
        return productoRepository.findById(id)
                .map(producto -> {
                    // Actualizar campos
                    producto.setNombre(productoActualizado.getNombre());
                    producto.setDescripcion(productoActualizado.getDescripcion());
                    producto.setPrecio(productoActualizado.getPrecio());
                    producto.setCategoria(productoActualizado.getCategoria());
                    producto.setMarca(productoActualizado.getMarca());
                    producto.setStockDisponible(productoActualizado.getStockDisponible());
                    producto.setImagenUrl(productoActualizado.getImagenUrl());
                    
                    // Validar antes de guardar
                    validarProducto(producto);
                    
                    Producto guardado = productoRepository.save(producto);
                    logger.info("Producto actualizado exitosamente: {}", guardado.getId());
                    
                    return guardado;
                });
    }

    /**
     * Elimina un producto (soft delete)
     */
    public boolean eliminarProducto(Long id) {
        logger.info("Eliminando producto con ID: {}", id);
        
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setActivo(false);
                    productoRepository.save(producto);
                    logger.info("Producto eliminado exitosamente: {}", id);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Busca productos por categoría
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(String categoria) {
        logger.debug("Buscando productos por categoría: {}", categoria);
        return productoRepository.findByCategoriaAndActivoTrue(categoria);
    }

    /**
     * Busca productos por marca
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorMarca(String marca) {
        logger.debug("Buscando productos por marca: {}", marca);
        return productoRepository.findByMarcaAndActivoTrue(marca);
    }

    /**
     * Busca productos por nombre
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        logger.debug("Buscando productos por nombre: {}", nombre);
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    /**
     * Busca productos en un rango de precios
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarPorRangoPrecios(BigDecimal precioMin, BigDecimal precioMax) {
        logger.debug("Buscando productos en rango de precios: {} - {}", precioMin, precioMax);
        return productoRepository.findByPrecioBetweenAndActivoTrueOrderByPrecioAsc(precioMin, precioMax);
    }

    /**
     * Obtiene productos con stock disponible
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConStock() {
        logger.debug("Obteniendo productos con stock disponible");
        return productoRepository.findProductosConStock();
    }

    /**
     * Obtiene todas las categorías disponibles
     */
    @Transactional(readOnly = true)
    public List<String> obtenerCategorias() {
        logger.debug("Obteniendo categorías disponibles");
        return productoRepository.findDistinctCategorias();
    }

    /**
     * Obtiene todas las marcas disponibles
     */
    @Transactional(readOnly = true)
    public List<String> obtenerMarcas() {
        logger.debug("Obteniendo marcas disponibles");
        return productoRepository.findDistinctMarcas();
    }

    /**
     * Actualiza el stock de un producto
     */
    public boolean actualizarStock(Long id, Integer nuevoStock) {
        logger.info("Actualizando stock del producto ID: {} a {}", id, nuevoStock);
        
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setStockDisponible(nuevoStock);
                    productoRepository.save(producto);
                    logger.info("Stock actualizado exitosamente para producto ID: {}", id);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Valida las reglas de negocio para un producto
     */
    private void validarProducto(Producto producto) {
        if (producto.getPrecio() != null && producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        }
        
        if (producto.getStockDisponible() != null && producto.getStockDisponible() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        // Aquí se pueden agregar más validaciones de negocio según sea necesario
    }
}
