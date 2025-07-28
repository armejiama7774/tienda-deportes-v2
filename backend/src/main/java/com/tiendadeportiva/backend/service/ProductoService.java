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
     * EVOLUCIÓN: Agregando notificaciones - primera señal de que necesitamos mejor arquitectura
     */
    @Override
    public Producto crearProducto(Producto producto) {
        logger.info("Creando nuevo producto: {}", producto.getNombre());
        
        // Validaciones existentes
        validarProducto(producto);
        
        // Asignar fecha de creación
        producto.setFechaCreacion(LocalDateTime.now());
        
        // Guardar producto
        Producto productoGuardado = productoRepository.save(producto);
        logger.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());
        
        // 🚨 NUEVO: Notificaciones - Esto va a crecer y complicarse...
        enviarNotificaciones(productoGuardado);
        
        return productoGuardado;
    }

    /**
     * Envía notificaciones cuando se crea un producto
     * PROBLEMA: Este método va a crecer mucho y será difícil de testear
     * TODO: Esto viola SRP y será pesadilla de mantenimiento
     */
    private void enviarNotificaciones(Producto producto) {
        try {
            // Notificación por email a administradores
            logger.info("📧 Enviando email de notificación para nuevo producto: {}", producto.getNombre());
            enviarEmailAdministradores(producto);
            
            // Actualizar cache del catálogo
            logger.info("🔄 Actualizando cache del catálogo para producto: {}", producto.getNombre());
            actualizarCacheCatalogo(producto);
            
            // Registrar auditoría
            logger.info("📋 Registrando auditoría para producto: {}", producto.getNombre());
            registrarAuditoria(producto);
            
            // TODO: En el futuro necesitaremos:
            // - Webhooks a sistemas externos
            // - SMS a gerentes
            // - Push notifications
            // - Actualizar sistemas de recomendaciones
            // - Sincronizar con marketplaces externos
            // ¿Dónde ponemos todo eso? ¿Cómo lo testeamos? ¿Qué pasa si uno falla?
            
        } catch (Exception e) {
            // 🚨 PROBLEMA: Si falla una notificación, ¿qué hacemos?
            // ¿Fallar toda la creación del producto? ¿Solo logear el error?
            logger.error("Error enviando notificaciones para producto {}: {}", 
                        producto.getId(), e.getMessage(), e);
            // Por ahora solo logeamos, pero esto no es ideal...
        }
    }

    /**
     * Simula envío de email a administradores
     * PROBLEMA: Hardcodeado y difícil de testear
     */
    private void enviarEmailAdministradores(Producto producto) {
        // Simular delay de servicio externo
        try {
            Thread.sleep(100); // Simular latencia
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // TODO: Integración real con servicio de email
        logger.info("📧 Email enviado a administradores sobre producto: {} - Precio: ${}", 
                   producto.getNombre(), producto.getPrecio());
    }

    /**
     * Simula actualización de cache
     * PROBLEMA: Lógica de cache mezclada con lógica de negocio
     */
    private void actualizarCacheCatalogo(Producto producto) {
        // TODO: Integración real con Redis/Hazelcast
        logger.info("🔄 Cache actualizado - Categoría: {} - Stock: {}", 
                   producto.getCategoria(), producto.getStockDisponible());
    }

    /**
     * Registra auditoría del producto
     * PROBLEMA: ¿Qué pasa si el log de auditoría falla?
     */
    private void registrarAuditoria(Producto producto) {
        // TODO: Persistir en tabla de auditoría
        logger.info("📋 Auditoría registrada - Producto ID: {} creado por: SYSTEM en: {}", 
                   producto.getId(), LocalDateTime.now());
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
