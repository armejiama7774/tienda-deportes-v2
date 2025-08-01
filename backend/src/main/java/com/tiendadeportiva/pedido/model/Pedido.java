package com.tiendadeportiva.pedido.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Pedido para el dominio de gestión de pedidos.
 * 
 * BOUNDED CONTEXT: Pedido
 * - Responsabilidad: Gestión de pedidos, carrito, checkout
 * - Separado de los dominios Producto y Usuario
 * 
 * PREPARACIÓN PARA MICROSERVICIOS:
 * - Entidad independiente con referencias por ID a otros dominios
 * - Sin dependencias directas a entidades de otros contextos
 * - Preparada para comunicación asíncrona via eventos
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia por ID al usuario (no entidad directa para preparar microservicios)
    @NotNull(message = "El usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @NotNull(message = "El total es obligatorio")
    @Positive(message = "El total debe ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(precision = 10, scale = 2)
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(length = 500)
    private String observaciones;

    // Items del pedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPedido> items = new ArrayList<>();

    // =============================================
    // CONSTRUCTORES
    // =============================================

    public Pedido() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
        this.total = BigDecimal.ZERO;
        this.descuentoAplicado = BigDecimal.ZERO;
    }

    public Pedido(Long usuarioId) {
        this();
        this.usuarioId = usuarioId;
    }

    // =============================================
    // MÉTODOS DE NEGOCIO
    // =============================================

    /**
     * Agrega un item al pedido.
     */
    public void agregarItem(Long productoId, String nombreProducto, 
                           BigDecimal precioUnitario, Integer cantidad) {
        ItemPedido item = new ItemPedido(this, productoId, nombreProducto, precioUnitario, cantidad);
        this.items.add(item);
        recalcularTotal();
    }

    /**
     * Elimina un item del pedido.
     */
    public boolean eliminarItem(Long productoId) {
        boolean eliminado = this.items.removeIf(item -> item.getProductoId().equals(productoId));
        if (eliminado) {
            recalcularTotal();
        }
        return eliminado;
    }

    /**
     * Actualiza la cantidad de un item.
     */
    public boolean actualizarCantidadItem(Long productoId, Integer nuevaCantidad) {
        for (ItemPedido item : this.items) {
            if (item.getProductoId().equals(productoId)) {
                item.setCantidad(nuevaCantidad);
                recalcularTotal();
                return true;
            }
        }
        return false;
    }

    /**
     * Recalcula el total del pedido basado en los items.
     */
    private void recalcularTotal() {
        this.total = items.stream()
                         .map(ItemPedido::getSubtotal)
                         .reduce(BigDecimal.ZERO, BigDecimal::add)
                         .subtract(this.descuentoAplicado);
        
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Aplica un descuento al pedido.
     */
    public void aplicarDescuento(BigDecimal montoDescuento) {
        if (montoDescuento.compareTo(BigDecimal.ZERO) >= 0) {
            this.descuentoAplicado = montoDescuento;
            recalcularTotal();
        }
    }

    /**
     * Confirma el pedido cambiando su estado.
     */
    public void confirmar() {
        if (this.estado == EstadoPedido.PENDIENTE) {
            this.estado = EstadoPedido.CONFIRMADO;
            this.fechaModificacion = LocalDateTime.now();
        }
    }

    /**
     * Procesa el pedido.
     */
    public void procesar() {
        if (this.estado == EstadoPedido.CONFIRMADO) {
            this.estado = EstadoPedido.PROCESANDO;
            this.fechaModificacion = LocalDateTime.now();
        }
    }

    /**
     * Completa el pedido.
     */
    public void completar() {
        if (this.estado == EstadoPedido.PROCESANDO) {
            this.estado = EstadoPedido.COMPLETADO;
            this.fechaFinalizacion = LocalDateTime.now();
            this.fechaModificacion = LocalDateTime.now();
        }
    }

    /**
     * Cancela el pedido.
     */
    public void cancelar() {
        if (this.estado == EstadoPedido.PENDIENTE || 
            this.estado == EstadoPedido.CONFIRMADO) {
            this.estado = EstadoPedido.CANCELADO;
            this.fechaModificacion = LocalDateTime.now();
        }
    }

    /**
     * Verifica si el pedido puede ser modificado.
     */
    public boolean puedeSerModificado() {
        return this.estado == EstadoPedido.PENDIENTE;
    }

    /**
     * Obtiene la cantidad total de items en el pedido.
     */
    public Integer getCantidadTotalItems() {
        return items.stream()
                   .mapToInt(ItemPedido::getCantidad)
                   .sum();
    }

    // =============================================
    // GETTERS Y SETTERS
    // =============================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
        this.fechaModificacion = LocalDateTime.now();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getDescuentoAplicado() {
        return descuentoAplicado;
    }

    public void setDescuentoAplicado(BigDecimal descuentoAplicado) {
        this.descuentoAplicado = descuentoAplicado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
        this.fechaModificacion = LocalDateTime.now();
    }

    public List<ItemPedido> getItems() {
        return new ArrayList<>(items); // Defensive copy
    }

    public void setItems(List<ItemPedido> items) {
        this.items = new ArrayList<>(items);
    }

    // =============================================
    // EQUALS, HASHCODE Y TOSTRING
    // =============================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pedido pedido = (Pedido) obj;
        return id != null && id.equals(pedido.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Pedido{id=%d, usuarioId=%d, estado=%s, total=%s, items=%d}", 
                           id, usuarioId, estado, total, items.size());
    }

    // =============================================
    // ENUM INTERNO
    // =============================================

    public enum EstadoPedido {
        PENDIENTE("Pendiente"),
        CONFIRMADO("Confirmado"),
        PROCESANDO("Procesando"),
        COMPLETADO("Completado"),
        CANCELADO("Cancelado");

        private final String descripcion;

        EstadoPedido(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
