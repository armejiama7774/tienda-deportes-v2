package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.exception.ProductoException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de productos que cumple con principios SOLID.
 * 
 * Aplicando:
 * - Single Responsibility Principle (SRP): Solo maneja lógica de productos
 * - Open/Closed Principle (OCP): Abierto para extensión vía interfaz
 * - Liskov Substitution Principle (LSP): Puede ser sustituido por cualquier implementación de IProductoService
 * - Interface Segregation Principle (ISP): Interfaz específica para productos
 * - Dependency Inversion Principle (DIP): Depende de abstracciones (ProductoRepository)
 * 
 * @author Equipo Desarrollo
 * @version 1.0
 * @since Fase 1 - Monolito Modular con SOLID
 */
@Service
@Transactional
public class ProductoService implements IProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;

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
        if (productoGuardado != null && productoGuardado.getId() != null) {
            logger.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());
        } else {
            logger.info("Producto creado exitosamente: {}", productoGuardado.getNombre());
        }
        
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
     * @param id Identificador del producto
     * @param nuevoStock Cantidad de stock a establecer
     * @return true si se actualizó correctamente
     * @throws ProductoNoEncontradoException si el producto no existe
     * @throws ProductoException si el stock es inválido
     */
    public boolean actualizarStock(Long id, Integer nuevoStock) {
        logger.info("Actualizando stock del producto ID: {} a {}", id, nuevoStock);
        
        if (nuevoStock < 0) {
            throw new ProductoException("STOCK_INVALIDO", 
                "El stock no puede ser negativo. Valor recibido: " + nuevoStock);
        }
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
                
        producto.setStockDisponible(nuevoStock);
        productoRepository.save(producto);
        logger.info("Stock actualizado exitosamente para producto ID: {}", id);
        return true;
    }

    /**
     * Valida las reglas de negocio para un producto
     * Aplica validaciones específicas del dominio más allá de las anotaciones JPA
     */
    private void validarProducto(Producto producto) {
        if (producto.getPrecio() != null && producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductoException("PRECIO_INVALIDO", 
                "El precio debe ser mayor que 0. Precio recibido: " + producto.getPrecio());
        }
        
        if (producto.getStockDisponible() != null && producto.getStockDisponible() < 0) {
            throw new ProductoException("STOCK_INVALIDO", 
                "El stock no puede ser negativo. Stock recibido: " + producto.getStockDisponible());
        }
        
        // Validación de duplicados por nombre y marca
        if (producto.getId() == null) { // Solo para productos nuevos
            boolean existe = productoRepository.existsByNombreAndMarcaAndActivoTrue(
                producto.getNombre(), producto.getMarca());
            if (existe) {
                throw new ProductoException("PRODUCTO_DUPLICADO", 
                    String.format("Ya existe un producto activo con nombre '%s' y marca '%s'", 
                        producto.getNombre(), producto.getMarca()));
            }
        }
        
        // Validación de categorías permitidas (ejemplo de regla de negocio)
        List<String> categoriasPermitidas = List.of(
            "Camisetas", "Pantalones", "Zapatos", "Accesorios", 
            "Ropa Interior", "Conjuntos", "Chaquetas"
        );
        if (producto.getCategoria() != null && 
            !categoriasPermitidas.contains(producto.getCategoria())) {
            throw new ProductoException("CATEGORIA_NO_PERMITIDA", 
                String.format("La categoría '%s' no está permitida. Categorías válidas: %s", 
                    producto.getCategoria(), categoriasPermitidas));
        }
    }
}
