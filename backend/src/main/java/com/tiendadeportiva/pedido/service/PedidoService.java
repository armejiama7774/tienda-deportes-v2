package com.tiendadeportiva.pedido.service;

import com.tiendadeportiva.pedido.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio del dominio Pedido.
 * 
 * BOUNDED CONTEXT: Pedido
 * - Responsabilidad: Gestión de pedidos, carrito, checkout
 * - Independiente de los dominios Producto y Usuario
 * 
 * PREPARACIÓN PARA MICROSERVICIOS:
 * - Servicio autocontenido que se comunica con otros dominios via IDs
 * - No tiene dependencias directas a entidades de otros dominios
 * - Preparado para comunicación asíncrona via eventos
 * - Listo para ser extraído a un microservicio independiente
 * 
 * COMUNICACIÓN ENTRE DOMINIOS:
 * - Con Usuario: Para verificar existencia y obtener datos básicos
 * - Con Producto: Para verificar stock, precios y aplicar descuentos
 * - Via eventos: Para notificar cambios de estado del pedido
 * 
 * FASE ACTUAL: Implementación básica con simulación
 * FUTURO: Integrará con UsuarioService y ProductoService via eventos
 */
@Service
@Transactional
public class PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    // ⚠️ SIMULACIÓN TEMPORAL: Cache en memoria para tests
    // En producción esto estará en base de datos
    private final Map<Long, Pedido> pedidosCache = new HashMap<>();
    private final Map<Long, Long> carritosPorUsuario = new HashMap<>();

    // =============================================
    // OPERACIONES BÁSICAS CRUD
    // =============================================

    /**
     * Crea un nuevo pedido para un usuario.
     */
    public Pedido crearPedido(Long usuarioId) {
        logger.info("🛒 Creando nuevo pedido para usuario: {}", usuarioId);
        
        Pedido pedido = new Pedido(usuarioId);
        pedido.setId(System.currentTimeMillis());
        pedidosCache.put(pedido.getId(), pedido);
        
        logger.info("✅ Pedido creado exitosamente con ID: {}", pedido.getId());
        return pedido;
    }

    /**
     * Obtiene un pedido por su ID.
     */
    public Optional<Pedido> obtenerPedidoPorId(Long id) {
        logger.debug("🔍 Buscando pedido por ID: {}", id);
        
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        
        Pedido pedido = pedidosCache.get(id);
        return Optional.ofNullable(pedido);
    }

    /**
     * Obtiene todos los pedidos de un usuario.
     */
    public List<Pedido> obtenerPedidosDeUsuario(Long usuarioId) {
        logger.debug("🔍 Obteniendo pedidos del usuario: {}", usuarioId);
        
        return pedidosCache.values().stream()
                          .filter(pedido -> pedido.getUsuarioId().equals(usuarioId))
                          .toList();
    }

    // =============================================
    // OPERACIONES DEL CARRITO (PEDIDO PENDIENTE)
    // =============================================

    /**
     * Agrega un producto al carrito (pedido pendiente).
     */
    public Pedido agregarProductoAlCarrito(Long usuarioId, Long productoId, Integer cantidad) {
        logger.info("➕ Agregando producto {} al carrito del usuario {} (cantidad: {})", 
                   productoId, usuarioId, cantidad);
        
        // PASO 1: Obtener o crear carrito (pedido pendiente)
        Pedido carrito = obtenerCarritoDeUsuario(usuarioId);
        
        // PASO 2: Simular datos del producto
        String nombreProducto = "Producto Simulado " + productoId;
        BigDecimal precioUnitario = new BigDecimal("99.99");
        
        // PASO 3: Agregar item al carrito
        carrito.agregarItem(productoId, nombreProducto, precioUnitario, cantidad);
        
        // PASO 4: Persistir cambios
        pedidosCache.put(carrito.getId(), carrito);
        
        logger.info("✅ Producto agregado al carrito. Total items: {}, Total: ${}", 
                   carrito.getCantidadTotalItems(), carrito.getTotal());
        
        return carrito;
    }

    /**
     * Actualiza la cantidad de un producto en el carrito.
     */
    public Pedido actualizarCantidadEnCarrito(Long usuarioId, Long productoId, Integer nuevaCantidad) {
        logger.info("📝 Actualizando cantidad del producto {} en carrito del usuario {} -> {} unidades", 
                   productoId, usuarioId, nuevaCantidad);
        
        Pedido carrito = obtenerCarritoDeUsuario(usuarioId);
        
        if (!carrito.puedeSerModificado()) {
            throw new IllegalStateException("El carrito no puede ser modificado en su estado actual: " + carrito.getEstado());
        }
        
        boolean actualizado = carrito.actualizarCantidadItem(productoId, nuevaCantidad);
        if (!actualizado) {
            throw new IllegalArgumentException("Producto " + productoId + " no encontrado en el carrito");
        }
        
        // Persistir cambios
        pedidosCache.put(carrito.getId(), carrito);
        
        logger.info("✅ Cantidad actualizada. Total items: {}, Total: ${}", 
                   carrito.getCantidadTotalItems(), carrito.getTotal());
        
        return carrito;
    }

    /**
     * Elimina un producto del carrito.
     */
    public Pedido eliminarProductoDelCarrito(Long usuarioId, Long productoId) {
        logger.info("➖ Eliminando producto {} del carrito del usuario {}", productoId, usuarioId);
        
        Pedido carrito = obtenerCarritoDeUsuario(usuarioId);
        
        if (!carrito.puedeSerModificado()) {
            throw new IllegalStateException("El carrito no puede ser modificado en su estado actual: " + carrito.getEstado());
        }
        
        boolean eliminado = carrito.eliminarItem(productoId);
        if (!eliminado) {
            throw new IllegalArgumentException("Producto " + productoId + " no encontrado en el carrito");
        }
        
        // Persistir cambios
        pedidosCache.put(carrito.getId(), carrito);
        
        logger.info("✅ Producto eliminado del carrito. Total items: {}, Total: ${}", 
                   carrito.getCantidadTotalItems(), carrito.getTotal());
        
        return carrito;
    }

    // =============================================
    // OPERACIONES DE CHECKOUT
    // =============================================

    /**
     * Confirma un pedido (checkout).
     */
    public Pedido confirmarPedido(Long pedidoId) {
        logger.info("✅ Confirmando pedido: {}", pedidoId);
        
        Optional<Pedido> pedidoOpt = obtenerPedidoPorId(pedidoId);
        if (pedidoOpt.isEmpty()) {
            throw new IllegalArgumentException("Pedido " + pedidoId + " no encontrado");
        }
        
        Pedido pedido = pedidoOpt.get();
        
        if (pedido.getEstado() != Pedido.EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        
        pedido.confirmar();
        pedidosCache.put(pedido.getId(), pedido);
        
        logger.info("✅ Pedido {} confirmado exitosamente", pedidoId);
        return pedido;
    }

    /**
     * Procesa un pedido confirmado.
     */
    public Pedido procesarPedido(Long pedidoId) {
        logger.info("⚙️ Procesando pedido: {}", pedidoId);
        
        Optional<Pedido> pedidoOpt = obtenerPedidoPorId(pedidoId);
        if (pedidoOpt.isEmpty()) {
            throw new IllegalArgumentException("Pedido " + pedidoId + " no encontrado");
        }
        
        Pedido pedido = pedidoOpt.get();
        
        if (pedido.getEstado() != Pedido.EstadoPedido.CONFIRMADO) {
            throw new IllegalStateException("Solo se pueden procesar pedidos confirmados");
        }
        
        pedido.procesar();
        pedidosCache.put(pedido.getId(), pedido);
        
        logger.info("✅ Pedido {} procesado exitosamente", pedidoId);
        return pedido;
    }

    /**
     * Completa un pedido procesado.
     */
    public Pedido completarPedido(Long pedidoId) {
        logger.info("🎯 Completando pedido: {}", pedidoId);
        
        Optional<Pedido> pedidoOpt = obtenerPedidoPorId(pedidoId);
        if (pedidoOpt.isEmpty()) {
            throw new IllegalArgumentException("Pedido " + pedidoId + " no encontrado");
        }
        
        Pedido pedido = pedidoOpt.get();
        
        if (pedido.getEstado() != Pedido.EstadoPedido.PROCESANDO) {
            throw new IllegalStateException("Solo se pueden completar pedidos en procesamiento");
        }
        
        pedido.completar();
        pedidosCache.put(pedido.getId(), pedido);
        
        logger.info("✅ Pedido {} completado exitosamente", pedidoId);
        return pedido;
    }

    /**
     * Cancela un pedido.
     */
    public Pedido cancelarPedido(Long pedidoId, String motivo) {
        logger.info("❌ Cancelando pedido: {} - Motivo: {}", pedidoId, motivo);
        
        Optional<Pedido> pedidoOpt = obtenerPedidoPorId(pedidoId);
        if (pedidoOpt.isEmpty()) {
            throw new IllegalArgumentException("Pedido " + pedidoId + " no encontrado");
        }
        
        Pedido pedido = pedidoOpt.get();
        
        if (!pedido.puedeSerModificado() && 
            pedido.getEstado() != Pedido.EstadoPedido.CONFIRMADO) {
            throw new IllegalStateException("El pedido no puede ser cancelado en su estado actual: " + pedido.getEstado());
        }
        
        pedido.cancelar();
        pedido.setObservaciones(motivo);
        pedidosCache.put(pedido.getId(), pedido);
        
        logger.info("✅ Pedido {} cancelado exitosamente", pedidoId);
        return pedido;
    }

    // =============================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // =============================================

    /**
     * Obtiene el carrito actual del usuario (pedido pendiente).
     * Si no existe, crea uno nuevo.
     */
    private Pedido obtenerCarritoDeUsuario(Long usuarioId) {
        // Buscar carrito existente en cache
        Long carritoId = carritosPorUsuario.get(usuarioId);
        if (carritoId != null) {
            Pedido carrito = pedidosCache.get(carritoId);
            if (carrito != null && carrito.getEstado() == Pedido.EstadoPedido.PENDIENTE) {
                return carrito;
            }
        }
        
        // Si no existe carrito, crear uno nuevo
        Pedido nuevoCarrito = crearPedido(usuarioId);
        carritosPorUsuario.put(usuarioId, nuevoCarrito.getId());
        return nuevoCarrito;
    }

    // =============================================
    // DTO PARA COMUNICACIÓN ENTRE DOMINIOS
    // =============================================

    /**
     * DTO para compartir información básica del pedido con otros dominios.
     */
    public static class PedidoInfo {
        private final Long id;
        private final Long usuarioId;
        private final Pedido.EstadoPedido estado;
        private final BigDecimal total;
        private final Integer cantidadItems;
        private final LocalDateTime fechaCreacion;

        public PedidoInfo(Pedido pedido) {
            this.id = pedido.getId();
            this.usuarioId = pedido.getUsuarioId();
            this.estado = pedido.getEstado();
            this.total = pedido.getTotal();
            this.cantidadItems = pedido.getCantidadTotalItems();
            this.fechaCreacion = pedido.getFechaCreacion();
        }

        // Getters
        public Long getId() { return id; }
        public Long getUsuarioId() { return usuarioId; }
        public Pedido.EstadoPedido getEstado() { return estado; }
        public BigDecimal getTotal() { return total; }
        public Integer getCantidadItems() { return cantidadItems; }
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }

        @Override
        public String toString() {
            return String.format("PedidoInfo{id=%d, usuarioId=%d, estado=%s, total=%s, items=%d}", 
                               id, usuarioId, estado, total, cantidadItems);
        }
    }
}
