package com.tiendadeportiva.pedido.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Entidad ItemPedido representa un producto específico dentro de un pedido.
 * 
 * BOUNDED CONTEXT: Pedido
 * - Responsabilidad: Detalles de productos en pedidos
 * - Referencia productos por ID (no entidad directa)
 * 
 * PREPARACIÓN PARA MICROSERVICIOS:
 * - No tiene referencia directa a la entidad Producto
 * - Almacena datos desnormalizados del producto (nombre, precio) 
 *   para evitar dependencias entre servicios
 * - Preparado para que el servicio de pedidos sea independiente
 */
@Entity
@Table(name = "items_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Referencia por ID al producto (no entidad directa)
    @NotNull(message = "El producto es obligatorio")
    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    // Datos desnormalizados del producto al momento del pedido
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Column(name = "nombre_producto", nullable = false, length = 200)
    private String nombreProducto;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser positivo")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    // =============================================
    // CONSTRUCTORES
    // =============================================

    public ItemPedido() {
    }

    public ItemPedido(Pedido pedido, Long productoId, String nombreProducto, 
                      BigDecimal precioUnitario, Integer cantidad) {
        this.pedido = pedido;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    // =============================================
    // MÉTODOS DE NEGOCIO
    // =============================================

    /**
     * Calcula el subtotal del item (precio unitario * cantidad).
     */
    public void calcularSubtotal() {
        if (precioUnitario != null && cantidad != null) {
            this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Actualiza la cantidad y recalcula el subtotal.
     */
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    /**
     * Actualiza el precio unitario y recalcula el subtotal.
     */
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
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

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    // =============================================
    // EQUALS, HASHCODE Y TOSTRING
    // =============================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ItemPedido item = (ItemPedido) obj;
        return id != null && id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("ItemPedido{id=%d, productoId=%d, nombre='%s', cantidad=%d, subtotal=%s}", 
                           id, productoId, nombreProducto, cantidad, subtotal);
    }
}
