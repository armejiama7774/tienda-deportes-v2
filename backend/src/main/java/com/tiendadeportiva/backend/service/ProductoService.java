package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.command.CommandHandler;
import com.tiendadeportiva.backend.command.producto.ActualizarProductoCommand;
import com.tiendadeportiva.backend.command.producto.CrearProductoCommand;
import com.tiendadeportiva.backend.command.producto.EliminarProductoCommand;
import com.tiendadeportiva.backend.domain.port.EventPublisherPort;
import com.tiendadeportiva.backend.domain.port.ProductoRepositoryPort;
import com.tiendadeportiva.backend.event.ProductoEventPublisher;
import com.tiendadeportiva.backend.event.ProductoEventType;
import com.tiendadeportiva.backend.exception.ProductoException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.factory.ProductoFactoryManager;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import com.tiendadeportiva.backend.service.descuento.DescuentoContexto;
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
 * 
 * OBSERVER PATTERN INTEGRADO:
 * - Emite eventos automáticamente durante operaciones CRUD
 * - Notifica cambios de stock y precios en tiempo real
 * - Mantiene separación entre lógica de negocio y eventos
 * - Permite auditoría y monitoreo transparente
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
    
    // 🔔 OBSERVER PATTERN PUBLISHER
    private final ProductoEventPublisher observerPublisher;
    
    // 🏭 FACTORY PATTERN MANAGER
    private final ProductoFactoryManager factoryManager;

    public ProductoService(
            ProductoRepositoryPort repositoryPort,
            EventPublisherPort eventPublisherPort,
            DescuentoService descuentoService,
            CommandHandler commandHandler,
            ProductoRepository queryRepository,
            ProductoEventPublisher observerPublisher,
            ProductoFactoryManager factoryManager) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.descuentoService = descuentoService;
        this.commandHandler = commandHandler;
        this.queryRepository = queryRepository;
        this.observerPublisher = observerPublisher;
        this.factoryManager = factoryManager;
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
    // 🆕 FACTORY PATTERN - CREACIÓN ESPECIALIZADA
    // =============================================

    /**
     * 🆕 NUEVO MÉTODO CON FACTORY PATTERN
     * Crea productos usando factories especializadas con validaciones y configuraciones automáticas.
     * 
     * DEMO DEL FACTORY PATTERN:
     * - Selecciona automáticamente la factory apropiada según el tipo
     * - Aplica validaciones específicas por tipo de producto  
     * - Configura propiedades automáticamente según características
     * - Integra con Observer Pattern para notificar creación
     * - Proporciona mejor experiencia que creación manual
     * 
     * VENTAJAS VS MÉTODO TRADICIONAL:
     * - Validaciones automáticas específicas por tipo
     * - Configuraciones por defecto inteligentes
     * - Extensible para nuevos tipos sin modificar código existente
     * - Mejor separación de responsabilidades
     * - Facilita testing con mocks
     */
    public Producto crearProductoConFactory(ProductoCreationRequest request) {
        logger.info("🏭 Creando producto con Factory Pattern: {} (Tipo: {}/{})", 
                   request.getNombre(), request.getCategoria(), request.getTipo());
        
        try {
            // PASO 1: Usar Factory Pattern para crear y configurar el producto
            Producto productoCreado = factoryManager.crearProducto(request);
            
            // PASO 2: Persistir usando el método tradicional
            Producto productoGuardado = crearProducto(productoCreado);
            
            // PASO 3: 🔔 Observer Pattern - Notificar creación con factory info
            observerPublisher.notifyEvent(
                ProductoEventType.PRODUCTO_CREADO,
                productoGuardado,
                String.format("Producto creado con Factory Pattern - Tipo: %s, Factory: %s", 
                            request.getTipo(), 
                            factoryManager.getClass().getSimpleName())
            );
            
            logger.info("✅ Producto creado exitosamente con Factory Pattern: {} (ID: {})", 
                       productoGuardado.getNombre(), productoGuardado.getId());
            
            return productoGuardado;
            
        } catch (Exception e) {
            logger.error("❌ Error creando producto con Factory Pattern: {}", e.getMessage(), e);
            throw new ProductoException("FACTORY_CREATION_ERROR", 
                                      "Error creando producto con Factory Pattern: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida si se puede crear un producto del tipo especificado.
     */
    public boolean puedeCrearTipoProducto(String categoria, String tipo) {
        return factoryManager.puedeCrear(categoria, tipo);
    }
    
    /**
     * Obtiene información sobre los tipos de productos que se pueden crear.
     */
    public ProductoFactoryManager.FactoryInfo getFactoryInfo() {
        return factoryManager.getFactoryInfo();
    }
    
    /**
     * Valida una petición de creación antes de procesarla.
     */
    public ProductoFactoryManager.ValidationResult validarPeticionCreacion(ProductoCreationRequest request) {
        return factoryManager.validarPeticion(request);
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
        Integer stockAnterior = producto.getStockDisponible();
        
        producto.setStockDisponible(nuevoStock);
        producto.setFechaModificacion(LocalDateTime.now()); // ✅ CORRECCIÓN: Actualizar fecha
        
        // Guardar el producto actualizado
        Producto productoGuardado = queryRepository.save(producto);
        
        // 🔔 OBSERVER PATTERN: Emitir eventos de stock
        String usuario = obtenerUsuarioActual();
        
        // Evento principal de stock actualizado
        observerPublisher.notifyStockActualizado(productoGuardado, usuario);
        
        // Eventos específicos según el nivel de stock
        if (nuevoStock == 0) {
            observerPublisher.notifyEvent(ProductoEventType.STOCK_AGOTADO, productoGuardado, 
                "Stock agotado - era: " + stockAnterior + " unidades");
        } else if (nuevoStock <= 5 && stockAnterior > 5) {
            observerPublisher.notifyEvent(ProductoEventType.STOCK_BAJO, productoGuardado,
                "Stock bajo detectado - anterior: " + stockAnterior + ", actual: " + nuevoStock);
        }
        
        logger.info("📦 Stock actualizado para producto '{}': {} -> {} unidades", 
                   producto.getNombre(), stockAnterior, nuevoStock);

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
    // OPERACIONES DE PRECIOS Y DESCUENTOS (NUEVA FUNCIONALIDAD)
    // =============================================

    /**
     * 🆕 NUEVO MÉTODO CON OBSERVER PATTERN
     * Actualiza el precio de un producto y emite eventos automáticamente.
     * 
     * DEMO DEL OBSERVER PATTERN:
     * - Actualiza el precio en la base de datos
     * - Emite evento de cambio de precio con datos del precio anterior
     * - Los observadores reaccionan automáticamente (logging, alertas, etc.)
     * - Demuestra separación entre lógica de negocio y efectos secundarios
     */
    public boolean actualizarPrecio(Long id, BigDecimal nuevoPrecio) {
        logger.info("💰 Actualizando precio para producto ID: {} -> ${}", id, nuevoPrecio);
        
        // Validaciones básicas
        if (nuevoPrecio == null || nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductoException("PRECIO_INVALIDO", "El precio debe ser positivo");
        }
        
        Optional<Producto> productoOpt = queryRepository.findById(id);
        if (productoOpt.isEmpty() || !Boolean.TRUE.equals(productoOpt.get().isActivo())) {
            throw new ProductoNoEncontradoException(id);
        }
        
        Producto producto = productoOpt.get();
        BigDecimal precioAnterior = producto.getPrecio();
        
        // Solo actualizar si el precio realmente cambió
        if (precioAnterior.compareTo(nuevoPrecio) == 0) {
            logger.info("💡 Precio no cambió para producto '{}', no se requiere actualización", 
                       producto.getNombre());
            return false;
        }
        
        // Actualizar el precio
        producto.setPrecio(nuevoPrecio);
        producto.setFechaModificacion(LocalDateTime.now());
        
        Producto productoGuardado = queryRepository.save(producto);
        
        // 🔔 OBSERVER PATTERN: Emitir evento de cambio de precio
        observerPublisher.notifyPrecioCambiado(
            productoGuardado, 
            obtenerUsuarioActual(), 
            precioAnterior  // Datos adicionales con el precio anterior
        );
        
        logger.info("✅ Precio actualizado para '{}': ${} -> ${}", 
                   producto.getNombre(), precioAnterior, nuevoPrecio);
        
        return true;
    }

    /**
     * Calcula el precio final de un producto aplicando descuentos disponibles.
     * 
     * IMPLEMENTACIÓN DEL STRATEGY PATTERN + OBSERVER PATTERN:
     * 1. Obtiene el producto de la base de datos
     * 2. Construye el contexto de descuento con los parámetros
     * 3. Delega al DescuentoService (coordinador) la selección de estrategia
     * 4. Extrae el precio final del resultado
     * 5. 🔔 Emite eventos cuando se aplican descuentos
     * 
     * EDUCATIVO PARA JUNIORS:
     * - Este método es un ejemplo de "Facade Pattern" simple
     * - Coordina múltiples operaciones pero delega la lógica compleja
     * - Maneja errores específicos del dominio (ProductoNoEncontrado)
     * - Mantiene logging detallado para debugging
     * - Convierte entre diferentes formatos de datos (DescuentoInfo -> BigDecimal)
     * - 🆕 Demuestra integración de Observer Pattern en operaciones de negocio
     */
    @Override
    public BigDecimal calcularPrecioConDescuento(Long productoId, Integer cantidadEnCarrito, boolean esUsuarioVIP) {
        logger.info("💰 Calculando precio con descuento - Producto ID: {}, Cantidad: {}, VIP: {}", 
                   productoId, cantidadEnCarrito, esUsuarioVIP);
        
        try {
            // PASO 1: Obtener el producto
            Optional<Producto> productoOpt = obtenerProductoPorId(productoId);
            if (productoOpt.isEmpty()) {
                logger.warn("❌ Producto con ID {} no encontrado para cálculo de descuento", productoId);
                throw new ProductoNoEncontradoException("No se encontró producto con ID: " + productoId);
            }
            
            Producto producto = productoOpt.get();
            BigDecimal precioBase = producto.getPrecio();
            
            logger.debug("📦 Producto encontrado: {} - Precio base: ${}", 
                        producto.getNombre(), precioBase);
            
            // PASO 2: Construir contexto de descuento
            DescuentoContexto contexto = DescuentoContexto.builder()
                    .conCantidadEnCarrito(cantidadEnCarrito)
                    .conTipoUsuario(esUsuarioVIP ? "VIP" : "REGULAR")
                    .build();
            
            logger.debug("🔧 Contexto de descuento construido: Cantidad={}, VIP={}", 
                        cantidadEnCarrito, esUsuarioVIP);
            
            // PASO 3: Aplicar descuentos usando Strategy Pattern
            DescuentoService.DescuentoInfo descuentoInfo = descuentoService.aplicarDescuentos(producto, contexto);
            
            // PASO 4: Extraer precio final del resultado
            BigDecimal precioFinal = descuentoInfo.getPrecioFinal();
            
            logger.info("✅ Precio calculado - Base: ${}, Descuento: ${}, Final: ${}, Estrategia: {}", 
                       descuentoInfo.getPrecioOriginal(), 
                       descuentoInfo.getTotalDescuento(), 
                       precioFinal,
                       descuentoInfo.getEstrategiaAplicada());
            
            // PASO 5: 🔔 OBSERVER PATTERN - Emitir evento si se aplicó descuento
            if (descuentoInfo.getTotalDescuento().compareTo(BigDecimal.ZERO) > 0) {
                String detallesDescuento = String.format(
                    "Descuento aplicado: $%.2f usando %s (Usuario: %s, Cantidad: %d)",
                    descuentoInfo.getTotalDescuento(),
                    descuentoInfo.getEstrategiaAplicada(),
                    esUsuarioVIP ? "VIP" : "REGULAR",
                    cantidadEnCarrito
                );
                
                observerPublisher.notifyDescuentoAplicado(producto, obtenerUsuarioActual(), detallesDescuento);
            }
            
            return precioFinal;
            
        } catch (ProductoNoEncontradoException e) {
            // Re-lanzar excepción específica del dominio
            throw e;
        } catch (Exception e) {
            logger.error("💥 Error inesperado calculando precio con descuento para producto {}: {}", 
                        productoId, e.getMessage(), e);
            throw new ProductoException("DESCUENTO_ERROR", "Error calculando precio con descuento: " + e.getMessage());
        }
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