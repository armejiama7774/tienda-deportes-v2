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
import java.util.Optional;

/**
 * Controlador REST para la gestión de productos aplicando principios SOLID.
 * 
 * Mejoras implementadas:
 * - Dependency Inversion Principle: Depende de IProductoService (abstracción)
 * - Manejo robusto de excepciones personalizadas
 * - Logging detallado para auditoría y debugging
 * - Respuestas HTTP semánticamente correctas
 * - Documentación completa de endpoints
 * 
 * @author Equipo Desarrollo
 * @version 1.0
 * @since Fase 1 - Monolito Modular con SOLID
 */
@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:3000") // Para permitir conexiones desde React
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    private final IProductoService productoService;

    public ProductoController(IProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Obtiene todos los productos
     * GET /api/productos
     */
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodosLosProductos() {
        logger.info("Petición recibida: GET /api/productos");
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene un producto por ID
     * GET /api/productos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        logger.info("Petición recibida: GET /api/productos/{}", id);
        
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        
        return producto
                .map(p -> ResponseEntity.ok(p))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo producto
     * POST /api/productos
     * 
     * @param producto Datos del producto a crear
     * @return Producto creado con HTTP 201, o error con HTTP 400
     */
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        logger.info("Petición recibida: POST /api/productos - {}", producto.getNombre());
        
        try {
            Producto productoCreado = productoService.crearProducto(producto);
            logger.info("Producto creado exitosamente con ID: {}", productoCreado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
        } catch (ProductoException e) {
            logger.warn("Error de validación al crear producto: [{}] {}", e.getCodigo(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error inesperado al crear producto", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualiza un producto existente
     * PUT /api/productos/{id}
     * 
     * @param id ID del producto a actualizar
     * @param productoActualizado Nuevos datos del producto
     * @return Producto actualizado o error correspondiente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable Long id, 
            @Valid @RequestBody Producto productoActualizado) {
        
        logger.info("Petición recibida: PUT /api/productos/{}", id);
        
        try {
            Optional<Producto> producto = productoService.actualizarProducto(id, productoActualizado);
            
            return producto
                    .map(p -> {
                        logger.info("Producto actualizado exitosamente: {}", p.getId());
                        return ResponseEntity.ok(p);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (ProductoException e) {
            logger.warn("Error de validación al actualizar producto {}: [{}] {}", id, e.getCodigo(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar producto {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Elimina un producto (soft delete)
     * DELETE /api/productos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        logger.info("Petición recibida: DELETE /api/productos/{}", id);
        
        boolean eliminado = productoService.eliminarProducto(id);
        
        return eliminado 
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Busca productos por categoría
     * GET /api/productos/categoria/{categoria}
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@PathVariable String categoria) {
        logger.info("Petición recibida: GET /api/productos/categoria/{}", categoria);
        
        List<Producto> productos = productoService.buscarPorCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    /**
     * Busca productos por marca
     * GET /api/productos/marca/{marca}
     */
    @GetMapping("/marca/{marca}")
    public ResponseEntity<List<Producto>> buscarPorMarca(@PathVariable String marca) {
        logger.info("Petición recibida: GET /api/productos/marca/{}", marca);
        
        List<Producto> productos = productoService.buscarPorMarca(marca);
        return ResponseEntity.ok(productos);
    }

    /**
     * Busca productos por nombre
     * GET /api/productos/buscar?nombre={nombre}
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        logger.info("Petición recibida: GET /api/productos/buscar?nombre={}", nombre);
        
        List<Producto> productos = productoService.buscarPorNombre(nombre);
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
        
        logger.info("Petición recibida: GET /api/productos/precio?min={}&max={}", min, max);
        
        List<Producto> productos = productoService.buscarPorRangoPrecios(min, max);
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene productos con stock disponible
     * GET /api/productos/con-stock
     */
    @GetMapping("/con-stock")
    public ResponseEntity<List<Producto>> obtenerProductosConStock() {
        logger.info("Petición recibida: GET /api/productos/con-stock");
        
        List<Producto> productos = productoService.obtenerProductosConStock();
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene todas las categorías disponibles
     * GET /api/productos/categorias
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<String>> obtenerCategorias() {
        logger.info("Petición recibida: GET /api/productos/categorias");
        
        List<String> categorias = productoService.obtenerCategorias();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtiene todas las marcas disponibles
     * GET /api/productos/marcas
     */
    @GetMapping("/marcas")
    public ResponseEntity<List<String>> obtenerMarcas() {
        logger.info("Petición recibida: GET /api/productos/marcas");
        
        List<String> marcas = productoService.obtenerMarcas();
        return ResponseEntity.ok(marcas);
    }

    /**
     * Actualiza el stock de un producto
     * PATCH /api/productos/{id}/stock
     * 
     * @param id ID del producto
     * @param stock Nueva cantidad de stock
     * @return HTTP 200 si se actualizó, 404 si no existe, 400 si datos inválidos
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> actualizarStock(
            @PathVariable Long id, 
            @RequestParam Integer stock) {
        
        logger.info("Petición recibida: PATCH /api/productos/{}/stock - Nuevo stock: {}", id, stock);
        
        try {
            boolean actualizado = productoService.actualizarStock(id, stock);
            return actualizado 
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (ProductoNoEncontradoException e) {
            logger.warn("Producto no encontrado al actualizar stock: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ProductoException e) {
            logger.warn("Error de validación al actualizar stock: [{}] {}", e.getCodigo(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar stock del producto {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
