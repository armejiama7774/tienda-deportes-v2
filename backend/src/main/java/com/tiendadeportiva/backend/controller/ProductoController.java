package com.tiendadeportiva.backend.controller;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.IProductoService;
import com.tiendadeportiva.backend.exception.ProductoException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para la gestión de productos aplicando principios SOLID.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2: Arquitectura Hexagonal
 * - Dependency Inversion Principle: Depende de IProductoService (abstracción)
 * - Manejo de errores delegado a GlobalExceptionHandler (SRP)
 * - Logging estructurado para observabilidad
 * - Respuestas HTTP semánticamente correctas
 * - Documentación completa siguiendo Google Java Style Guide
 * 
 * @author Equipo Desarrollo
 * @version 2.0
 * @since Fase 2 - Arquitectura Hexagonal
 */
@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);
    private final IProductoService productoService;

    public ProductoController(IProductoService productoService) {
        this.productoService = productoService;
    }

    // =============================================
    // COMANDOS (OPERACIONES DE ESCRITURA)
    // =============================================

    /**
     * Crea un nuevo producto
     * POST /api/productos
     * 
     * ARQUITECTURA HEXAGONAL:
     * - Controller → Service → Commands → Ports → Adapters
     * - Manejo de errores centralizado en GlobalExceptionHandler
     * - Logging estructurado para observabilidad
     * 
     * @param producto Datos del producto a crear (validado con @Valid)
     * @return Producto creado con HTTP 201
     * @throws ProductoException Si hay errores de validación o negocio
     */
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        logger.info("🚀 Petición recibida: POST /api/productos - {}", producto.getNombre());
        
        // ✅ ARQUITECTURA HEXAGONAL: Delegación simple al servicio
        // Los errores son manejados por GlobalExceptionHandler automáticamente
        Producto productoCreado = productoService.crearProducto(producto);
        
        logger.info("✅ Producto creado exitosamente con ID: {}", productoCreado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
    }

    /**
     * Actualiza un producto existente
     * PUT /api/productos/{id}
     * 
     * @param id ID del producto a actualizar
     * @param productoActualizado Nuevos datos del producto
     * @return Producto actualizado o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable Long id, 
            @Valid @RequestBody Producto productoActualizado) {
        
        logger.info("🔄 Petición recibida: PUT /api/productos/{}", id);
        
        Optional<Producto> producto = productoService.actualizarProducto(id, productoActualizado);
        
        return producto
                .map(p -> {
                    logger.info("✅ Producto actualizado exitosamente: {}", p.getId());
                    return ResponseEntity.ok(p);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un producto (soft delete)
     * DELETE /api/productos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        logger.info("🗑️ Petición recibida: DELETE /api/productos/{}", id);
        
        boolean eliminado = productoService.eliminarProducto(id);
        
        if (eliminado) {
            logger.info("✅ Producto eliminado exitosamente: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("⚠️ Producto no encontrado para eliminar: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualiza el stock de un producto
     * PATCH /api/productos/{id}/stock
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> actualizarStock(
            @PathVariable Long id, 
            @RequestParam Integer stock) {
        
        logger.info("📦 Petición recibida: PATCH /api/productos/{}/stock - Nuevo stock: {}", id, stock);
        
        boolean actualizado = productoService.actualizarStock(id, stock);
        
        if (actualizado) {
            logger.info("✅ Stock actualizado exitosamente para producto: {}", id);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("⚠️ Producto no encontrado para actualizar stock: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // =============================================
    // QUERIES (OPERACIONES DE LECTURA)
    // =============================================

    /**
     * Obtiene todos los productos
     * GET /api/productos
     */
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodosLosProductos() {
        logger.info("📋 Petición recibida: GET /api/productos");
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        logger.info("✅ Retornando {} productos", productos.size());
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene un producto por ID
     * GET /api/productos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        logger.info("🔍 Petición recibida: GET /api/productos/{}", id);
        
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        
        return producto
                .map(p -> {
                    logger.info("✅ Producto encontrado: {}", p.getNombre());
                    return ResponseEntity.ok(p);
                })
                .orElseGet(() -> {
                    logger.warn("⚠️ Producto no encontrado: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // =============================================
    // BÚSQUEDAS Y FILTROS
    // =============================================

    /**
     * Busca productos por categoría
     * GET /api/productos/categoria/{categoria}
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@PathVariable String categoria) {
        logger.info("🏷️ Petición recibida: GET /api/productos/categoria/{}", categoria);
        
        List<Producto> productos = productoService.buscarPorCategoria(categoria);
        logger.info("✅ Encontrados {} productos en categoría '{}'", productos.size(), categoria);
        return ResponseEntity.ok(productos);
    }

    /**
     * Busca productos por marca
     * GET /api/productos/marca/{marca}
     */
    @GetMapping("/marca/{marca}")
    public ResponseEntity<List<Producto>> buscarPorMarca(@PathVariable String marca) {
        logger.info("🏭 Petición recibida: GET /api/productos/marca/{}", marca);
        
        List<Producto> productos = productoService.buscarPorMarca(marca);
        logger.info("✅ Encontrados {} productos de marca '{}'", productos.size(), marca);
        return ResponseEntity.ok(productos);
    }

    /**
     * Busca productos por nombre
     * GET /api/productos/buscar?nombre={nombre}
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        logger.info("🔍 Petición recibida: GET /api/productos/buscar?nombre={}", nombre);
        
        List<Producto> productos = productoService.buscarPorNombre(nombre);
        logger.info("✅ Encontrados {} productos con nombre '{}'", productos.size(), nombre);
        return ResponseEntity.ok(productos);
    }

    /**
     * Busca productos en un rango de precios
     * GET /api/productos/precio?min={min}&max={max}
     */
    @GetMapping("/precio")
    public ResponseEntity<List<Producto>> buscarPorRangoPrecios(
            @RequestParam BigDecimal min, 
            @RequestParam BigDecimal max) {
        
        logger.info("💰 Petición recibida: GET /api/productos/precio?min={}&max={}", min, max);
        
        List<Producto> productos = productoService.buscarPorRangoPrecios(min, max);
        logger.info("✅ Encontrados {} productos en rango ${}-${}", productos.size(), min, max);
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene productos con stock disponible
     * GET /api/productos/con-stock
     */
    @GetMapping("/con-stock")
    public ResponseEntity<List<Producto>> obtenerProductosConStock() {
        logger.info("📦 Petición recibida: GET /api/productos/con-stock");
        
        List<Producto> productos = productoService.obtenerProductosConStock();
        logger.info("✅ Encontrados {} productos con stock", productos.size());
        return ResponseEntity.ok(productos);
    }

    // =============================================
    // METADATOS DEL CATÁLOGO
    // =============================================

    /**
     * Obtiene todas las categorías disponibles
     * GET /api/productos/categorias
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<String>> obtenerCategorias() {
        logger.info("📂 Petición recibida: GET /api/productos/categorias");
        
        List<String> categorias = productoService.obtenerCategorias();
        logger.info("✅ Retornando {} categorías", categorias.size());
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtiene todas las marcas disponibles
     * GET /api/productos/marcas
     */
    @GetMapping("/marcas")
    public ResponseEntity<List<String>> obtenerMarcas() {
        logger.info("🏭 Petición recibida: GET /api/productos/marcas");
        
        List<String> marcas = productoService.obtenerMarcas();
        logger.info("✅ Retornando {} marcas", marcas.size());
        return ResponseEntity.ok(marcas);
    }
}
