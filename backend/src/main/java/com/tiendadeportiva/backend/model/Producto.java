package com.tiendadeportiva.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA para productos de la tienda deportiva.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2: Arquitectura Hexagonal
 * - Validaciones robustas con Bean Validation
 * - Boolean wrapper para mejor compatibilidad JPA/Spring
 * - Principios SOLID aplicados
 * - Preparación para microservicios (Fase 3)
 */
@Entity
@Table(name = "productos")
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ✅ VALIDACIONES ROBUSTAS siguiendo Google Java Style Guide
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    @DecimalMax(value = "999999.99", message = "El precio no puede exceder $999,999.99")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @NotBlank(message = "La categoría es obligatoria")
    @Size(min = 2, max = 100, message = "La categoría debe tener entre 2 y 100 caracteres")
    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;
    
    @NotBlank(message = "La marca es obligatoria")
    @Size(min = 2, max = 100, message = "La marca debe tener entre 2 y 100 caracteres")
    @Column(name = "marca", nullable = false, length = 100)
    private String marca;
    
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 999999, message = "El stock no puede exceder 999,999 unidades")
    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible = 0;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    // =============================================
    // CONSTRUCTORES
    // =============================================
    
    public Producto() {
        this.activo = Boolean.TRUE;
        this.fechaCreacion = LocalDateTime.now();
        this.stockDisponible = 0;
    }
    
    /**
     * Constructor de conveniencia para crear productos completos
     * @param nombre Nombre del producto
     * @param descripcion Descripción del producto  
     * @param precio Precio del producto
     * @param categoria Categoría del producto
     * @param marca Marca del producto
     */
    public Producto(String nombre, String descripcion, BigDecimal precio, String categoria, String marca) {
        this(); // Llamar constructor por defecto
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.stockDisponible = 0; // Stock inicial por defecto
    }
    
    /**
     * Constructor completo con stock
     */
    public Producto(String nombre, String descripcion, BigDecimal precio, String categoria, String marca, Integer stock) {
        this(nombre, descripcion, precio, categoria, marca);
        this.stockDisponible = stock;
    }
    
    // =============================================
    // GETTERS Y SETTERS - CORREGIDOS
    // =============================================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public Integer getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(Integer stockDisponible) { this.stockDisponible = stockDisponible; }
    
    // ✅ CORRECCIÓN: Boolean wrapper con isActivo()
    public Boolean isActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    // ✅ CORRECCIÓN: Nombres consistentes de fechas
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    
    // =============================================
    // MÉTODOS DE CONVENIENCIA
    // =============================================
    
    /**
     * Verifica si el producto está disponible para venta
     */
    public Boolean estaDisponible() {
        return Boolean.TRUE.equals(activo) && 
               stockDisponible != null && 
               stockDisponible > 0;
    }
    
    /**
     * Marca el producto como eliminado (soft delete)
     */
    public void marcarComoEliminado() {
        this.activo = Boolean.FALSE;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    /**
     * Reactiva un producto eliminado
     */
    public void reactivar() {
        this.activo = Boolean.TRUE;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    /**
     * Actualiza la fecha de modificación al momento actual
     */
    public void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
    }
    
    // =============================================
    // EQUALS, HASHCODE, TOSTRING
    // =============================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(id, producto.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Producto{id=%d, nombre='%s', marca='%s', precio=%s, activo=%s}", 
                           id, nombre, marca, precio, activo);
    }
}
