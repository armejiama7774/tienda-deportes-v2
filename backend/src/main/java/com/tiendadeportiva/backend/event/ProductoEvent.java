package com.tiendadeportiva.backend.event;

import com.tiendadeportiva.backend.model.Producto;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Clase base para todos los eventos relacionados con productos.
 * 
 * EDUCATIVO PARA JUNIORS:
 * - Esta clase encapsula toda la información común de los eventos
 * - Inmutable por diseño (todos los campos final + sin setters)
 * - Incluye metadatos útiles como timestamp y ID único
 * - Builder pattern para construcción fluida y legible
 * 
 * OBSERVER PATTERN - PARTE 2:
 * - Define la estructura común de todos los eventos
 * - Contiene el producto afectado y metadatos del evento
 * - Los observadores reciben instancias de esta clase
 * - Diseño extensible para futuras propiedades
 */
public class ProductoEvent {
    
    // =============================================
    // PROPIEDADES INMUTABLES
    // =============================================
    
    /** ID único del evento para tracking y debugging */
    private final String eventId;
    
    /** Tipo específico del evento */
    private final ProductoEventType tipo;
    
    /** Producto afectado por el evento */
    private final Producto producto;
    
    /** Momento exacto cuando ocurrió el evento */
    private final LocalDateTime timestamp;
    
    /** Descripción adicional del evento (opcional) */
    private final String descripcion;
    
    /** Usuario que causó el evento (para auditoría) */
    private final String usuario;
    
    /** Datos adicionales específicos del evento (opcional) */
    private final Object datosAdicionales;
    
    // =============================================
    // CONSTRUCTOR PRIVADO (USAR BUILDER)
    // =============================================
    
    private ProductoEvent(Builder builder) {
        this.eventId = builder.eventId;
        this.tipo = builder.tipo;
        this.producto = builder.producto;
        this.timestamp = builder.timestamp;
        this.descripcion = builder.descripcion;
        this.usuario = builder.usuario;
        this.datosAdicionales = builder.datosAdicionales;
    }
    
    // =============================================
    // MÉTODOS ESTÁTICOS DE CREACIÓN
    // =============================================
    
    /**
     * Crea un builder para construir eventos de manera fluida.
     * @return Nuevo builder para ProductoEvent
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Método de conveniencia para crear eventos rápidamente.
     * @param tipo Tipo de evento
     * @param producto Producto afectado
     * @return Evento con valores por defecto
     */
    public static ProductoEvent of(ProductoEventType tipo, Producto producto) {
        return builder()
                .conTipo(tipo)
                .conProducto(producto)
                .build();
    }
    
    /**
     * Método de conveniencia para crear eventos con descripción.
     * @param tipo Tipo de evento
     * @param producto Producto afectado
     * @param descripcion Descripción adicional
     * @return Evento configurado
     */
    public static ProductoEvent of(ProductoEventType tipo, Producto producto, String descripcion) {
        return builder()
                .conTipo(tipo)
                .conProducto(producto)
                .conDescripcion(descripcion)
                .build();
    }
    
    // =============================================
    // GETTERS (IMMUTABLE)
    // =============================================
    
    public String getEventId() { return eventId; }
    public ProductoEventType getTipo() { return tipo; }
    public Producto getProducto() { return producto; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescripcion() { return descripcion; }
    public String getUsuario() { return usuario; }
    public Object getDatosAdicionales() { return datosAdicionales; }
    
    // =============================================
    // MÉTODOS DE UTILIDAD
    // =============================================
    
    /**
     * Obtiene el ID del producto afectado.
     * @return ID del producto o null si no hay producto
     */
    public Long getProductoId() {
        return producto != null ? producto.getId() : null;
    }
    
    /**
     * Obtiene el nombre del producto afectado.
     * @return Nombre del producto o "Desconocido" si no hay producto
     */
    public String getProductoNombre() {
        return producto != null ? producto.getNombre() : "Desconocido";
    }
    
    /**
     * Verifica si este evento es de tipo urgente.
     * @return true si requiere atención inmediata
     */
    public boolean isUrgente() {
        return tipo.isEventoUrgente();
    }
    
    // =============================================
    // BUILDER PATTERN
    // =============================================
    
    public static class Builder {
        private String eventId = UUID.randomUUID().toString();
        private ProductoEventType tipo;
        private Producto producto;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String descripcion;
        private String usuario = "SYSTEM";
        private Object datosAdicionales;
        
        public Builder conEventId(String eventId) {
            this.eventId = eventId;
            return this;
        }
        
        public Builder conTipo(ProductoEventType tipo) {
            this.tipo = tipo;
            return this;
        }
        
        public Builder conProducto(Producto producto) {
            this.producto = producto;
            return this;
        }
        
        public Builder conTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder conDescripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }
        
        public Builder conUsuario(String usuario) {
            this.usuario = usuario;
            return this;
        }
        
        public Builder conDatosAdicionales(Object datosAdicionales) {
            this.datosAdicionales = datosAdicionales;
            return this;
        }
        
        public ProductoEvent build() {
            // Validaciones básicas
            Objects.requireNonNull(tipo, "El tipo de evento es requerido");
            Objects.requireNonNull(producto, "El producto es requerido");
            Objects.requireNonNull(timestamp, "El timestamp es requerido");
            
            return new ProductoEvent(this);
        }
    }
    
    // =============================================
    // MÉTODOS ESTÁNDAR
    // =============================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductoEvent that = (ProductoEvent) o;
        return Objects.equals(eventId, that.eventId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
    
    @Override
    public String toString() {
        return String.format("ProductoEvent{id='%s', tipo=%s, producto='%s', timestamp=%s}",
                eventId, tipo, getProductoNombre(), timestamp);
    }
}
