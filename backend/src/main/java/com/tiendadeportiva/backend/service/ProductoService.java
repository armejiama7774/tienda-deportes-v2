package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.command.CommandHandler;
import com.tiendadeportiva.backend.command.producto.ActualizarProductoCommand;
import com.tiendadeportiva.backend.command.producto.CrearProductoCommand;
import com.tiendadeportiva.backend.command.producto.EliminarProductoCommand;
import com.tiendadeportiva.backend.domain.port.EventPublisherPort;
import com.tiendadeportiva.backend.domain.port.ProductoRepositoryPort;
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
 * Servicio de productos usando arquitectura hexagonal.
 * 
 * ARQUITECTURA HEXAGONAL COMPLETADA:
 * - Usa puertos en lugar de implementaciones concretas para comandos
 * - Mantiene queries directas para simplificar (preparación para CQRS)
 * - Separación clara entre commands y queries
 * - Independiente de tecnologías específicas en commands
 */
@Service
@Transactional
public class ProductoService implements IProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    // 🎯 PUERTOS PARA COMANDOS (ARQUITECTURA HEXAGONAL)
    private final ProductoRepositoryPort repositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final DescuentoService descuentoService;
    private final CommandHandler commandHandler;

    // 🎯 REPOSITORIO DIRECTO PARA QUERIES (PREPARACIÓN CQRS)
    private final ProductoRepository queryRepository;

    public ProductoService(
            ProductoRepositoryPort repositoryPort,
            EventPublisherPort eventPublisherPort,
            DescuentoService descuentoService,
            CommandHandler commandHandler,
            ProductoRepository queryRepository) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.descuentoService = descuentoService;
        this.commandHandler = commandHandler;
        this.queryRepository = queryRepository;
    }

    // =============================================
    // COMANDOS (USANDO PUERTOS HEXAGONALES)
    // =============================================

    @Override
    public Producto crearProducto(Producto producto) {
        try {
            CrearProductoCommand command = new CrearProductoCommand(
                producto,
                repositoryPort,       // 🎯 PUERTO EN LUGAR DE IMPLEMENTACIÓN
                descuentoService,
                eventPublisherPort,   // 🎯 PUERTO EN LUGAR DE IMPLEMENTACIÓN
                obtenerUsuarioActual()
            );

            return commandHandler.handle(command);

        } catch (CommandExecutionException e) {
            logger.error("Error creando producto: {}", e.getMessage(), e);
            throw new ProductoException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public Optional<Producto> actualizarProducto(Long id, Producto productoActualizado) {
        try {
            ActualizarProductoCommand command = new ActualizarProductoCommand(
                id,
                productoActualizado,
                repositoryPort,       // 🎯 PUERTO EN LUGAR DE IMPLEMENTACIÓN
                descuentoService,
                eventPublisherPort,   // 🎯 PUERTO EN LUGAR DE IMPLEMENTACIÓN
                obtenerUsuarioActual()
            );

            Producto resultado = commandHandler.handle(command);
            return Optional.of(resultado);

        } catch (CommandExecutionException e) {
            logger.error("Error actualizando producto {}: {}", id, e.getMessage(), e);

            if ("PRODUCTO_NO_ENCONTRADO".equals(e.getErrorCode())) {
                return Optional.empty();
            }

            throw new ProductoException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminarProducto(Long id) {
        try {
            EliminarProductoCommand command = new EliminarProductoCommand(
                id,
                repositoryPort,       // 🎯 PUERTO EN LUGAR DE IMPLEMENTACIÓN
                eventPublisherPort,   // 🎯 PUERTO EN LUGAR DE IMPLEMENTACIÓN
                obtenerUsuarioActual()
            );

            return commandHandler.handle(command);

        } catch (CommandExecutionException e) {
            logger.error("Error eliminando producto {}: {}", id, e.getMessage(), e);

            if ("PRODUCTO_NO_ENCONTRADO".equals(e.getErrorCode())) {
                return false;
            }

            throw new ProductoException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    // =============================================
    // QUERIES (ACCESO DIRECTO - PREPARACIÓN CQRS)
    // =============================================

    @Override
    public List<Producto> obtenerTodosLosProductos() {
        return queryRepository.findByActivoTrueOrderByFechaCreacionDesc();
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(Long id) {
        Optional<Producto> producto = queryRepository.findById(id);
        // ✅ CORRECCIÓN: Safe Boolean comparison
        return producto.filter(p -> Boolean.TRUE.equals(p.isActivo()));
    }

    @Override
    public List<Producto> buscarPorCategoria(String categoria) {
        return queryRepository.findByCategoriaAndActivoTrue(categoria);
    }

    @Override
    public List<Producto> buscarPorMarca(String marca) {
        return queryRepository.findByMarcaAndActivoTrue(marca);
    }

    @Override
    public List<Producto> buscarPorNombre(String nombre) {
        return queryRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    @Override
    public boolean actualizarStock(Long id, Integer nuevoStock) {
        if (nuevoStock < 0) {
            throw new ProductoException("STOCK_INVALIDO", "El stock no puede ser negativo");
        }

        Optional<Producto> productoOpt = queryRepository.findById(id);
        // ✅ CORRECCIÓN: Safe Boolean comparison
        if (productoOpt.isEmpty() || !Boolean.TRUE.equals(productoOpt.get().isActivo())) {
            throw new ProductoNoEncontradoException(id);
        }

        Producto producto = productoOpt.get();
        producto.setStockDisponible(nuevoStock);
        producto.setFechaModificacion(LocalDateTime.now()); // ✅ CORRECCIÓN: Actualizar fecha
        queryRepository.save(producto);

        return true;
    }

    // =============================================
    // QUERIES DE FILTRADO (BÚSQUEDAS ESPECÍFICAS)
    // =============================================

    @Override
    public List<Producto> buscarPorRangoPrecios(BigDecimal precioMin, BigDecimal precioMax) {
        logger.debug("🔍 Buscando productos en rango de precios: ${} - ${}", precioMin, precioMax);
        
        // Validaciones de entrada
        if (precioMin == null || precioMax == null) {
            throw new ProductoException("PRECIO_INVALIDO", "Los precios mínimo y máximo son requeridos");
        }
        
        if (precioMin.compareTo(BigDecimal.ZERO) < 0 || precioMax.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductoException("PRECIO_INVALIDO", "Los precios no pueden ser negativos");
        }
        
        if (precioMin.compareTo(precioMax) > 0) {
            throw new ProductoException("RANGO_INVALIDO", "El precio mínimo no puede ser mayor que el máximo");
        }
        
        List<Producto> productos = queryRepository.findByPrecioBetweenAndActivoTrueOrderByPrecio(precioMin, precioMax);
        
        logger.info("✅ Encontrados {} productos en rango ${} - ${}", 
                   productos.size(), precioMin, precioMax);
        
        return productos;
    }

    @Override
    public List<Producto> obtenerProductosConStock() {
        logger.debug("🔍 Obteniendo productos con stock disponible");
        
        List<Producto> productos = queryRepository.findByStockDisponibleGreaterThanAndActivoTrue(0);
        
        logger.info("✅ Encontrados {} productos con stock disponible", productos.size());
        
        return productos;
    }

    // =============================================
    // QUERIES DE METADATOS (INFORMACIÓN DEL CATÁLOGO)
    // =============================================

    /**
     * Obtiene todas las marcas disponibles de productos activos
     * @return Lista de marcas únicas ordenadas alfabéticamente
     */
    @Override
    public List<String> obtenerMarcas() {
        logger.debug("🔍 Obteniendo todas las marcas disponibles");
        
        List<String> marcas = queryRepository.findDistinctMarcaByActivoTrue();
        
        logger.info("✅ Encontradas {} marcas disponibles", marcas.size());
        
        return marcas;
    }

    /**
     * Obtiene todas las categorías disponibles de productos activos
     * @return Lista de categorías únicas ordenadas alfabéticamente
     */
    @Override
    public List<String> obtenerCategorias() {
        logger.debug("🔍 Obteniendo todas las categorías disponibles");
        
        List<String> categorias = queryRepository.findDistinctCategoriaByActivoTrue();
        
        logger.info("✅ Encontradas {} categorías disponibles", categorias.size());
        
        return categorias;
    }

    // =============================================
    // MÉTODOS PRIVADOS
    // =============================================

    /**
     * Obtiene el usuario actual del contexto de seguridad.
     * TODO: Integrar con Spring Security en fases futuras
     */
    private String obtenerUsuarioActual() {
        // TODO: Implementar cuando tengamos autenticación
        return "SYSTEM";
    }
}