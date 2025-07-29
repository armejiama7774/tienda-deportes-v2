package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.command.CommandHandler;
import com.tiendadeportiva.backend.command.producto.CrearProductoCommand;
import com.tiendadeportiva.backend.event.ProductoCreadoEvent;
import com.tiendadeportiva.backend.exception.ProductoException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final ApplicationEventPublisher applicationEventPublisher; // ✅ NUEVO
    private final DescuentoService descuentoService; // ✅ NUEVA DEPENDENCIA
    private final CommandHandler commandHandler; // ✅ NUEVA DEPENDENCIA

    // Constructor actualizado
    public ProductoService(ProductoRepository productoRepository, 
                          ApplicationEventPublisher applicationEventPublisher,
                          DescuentoService descuentoService,
                          CommandHandler commandHandler) {
        this.productoRepository = productoRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.descuentoService = descuentoService;
        this.commandHandler = commandHandler;
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
     * Obtiene un producto por ID.
     * Solo devuelve productos activos (no eliminados con soft delete).
     * 
     * @param id ID del producto a buscar
     * @return Optional con el producto si existe y está activo
     */
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        logger.info("Buscando producto con ID: {}", id);
        
        if (id == null || id <= 0) {
            logger.warn("ID de producto inválido: {}", id);
            return Optional.empty();
        }
        
        Optional<Producto> producto = productoRepository.findById(id);
        
        // CORRECCIÓN: Usar getActivo() en lugar de isActivo()
        if (producto.isPresent() && producto.get().getActivo()) {
            logger.info("Producto encontrado: {}", producto.get().getNombre());
            return producto;
        }
        
        logger.info("Producto con ID {} no encontrado o está inactivo", id);
        return Optional.empty();
    }

    /**
     * Crea un nuevo producto
     * EVOLUCIÓN COMPLETADA: ProductoService realmente limpio y enfocado
     */
    @Override
    public Producto crearProducto(Producto producto) {
        try {
            // 🎯 EVOLUCIÓN: Usar Command Pattern
            CrearProductoCommand command = new CrearProductoCommand(
                producto,
                productoRepository,
                descuentoService,
                applicationEventPublisher,
                "SYSTEM" // TODO: Obtener usuario del contexto de seguridad
            );
            
            return commandHandler.handle(command);
            
        } catch (CommandExecutionException e) {
            logger.error("Error creando producto: {}", e.getMessage(), e);
            throw new ProductoException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    /**
     * Publica evento de producto creado
     */
    private void publicarEventoProductoCreado(Producto producto) {
        try {
            ProductoCreadoEvent evento = new ProductoCreadoEvent(this, producto, "SYSTEM");
            applicationEventPublisher.publishEvent(evento);
            logger.info("✅ Evento ProductoCreadoEvent publicado para producto: {}", producto.getNombre());
        } catch (Exception e) {
            logger.error("❌ Error publicando evento para producto {}: {}", 
                        producto.getId(), e.getMessage(), e);
        }
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
        
        // 🔧 CORRECCIÓN: Validación de duplicados usando el método correcto
        if (producto.getId() == null) { // Solo para productos nuevos
            boolean existe = productoRepository.existsByNombreAndMarcaAndActivoTrue(
                producto.getNombre(), producto.getMarca());
            if (existe) {
                throw new ProductoException("PRODUCTO_DUPLICADO", 
                    String.format("Ya existe un producto activo con nombre '%s' y marca '%s'", 
                        producto.getNombre(), producto.getMarca()));
            }
        }
        
        // Resto de validaciones...
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