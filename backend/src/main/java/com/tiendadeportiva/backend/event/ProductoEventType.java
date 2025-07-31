package com.tiendadeportiva.backend.event;

/**
 * Enum que define los tipos de eventos que pueden ocurrir en el sistema de productos.
 * 
 * EDUCATIVO PARA JUNIORS:
 * - Este enum centraliza todos los tipos de eventos posibles
 * - Facilita el mantenimiento y evita strings mágicos
 * - Cada enum tiene una descripción clara de cuándo se dispara
 * - Preparado para futuras extensiones sin romper código existente
 * 
 * OBSERVER PATTERN - PARTE 1:
 * - Define QUÉ tipos de eventos pueden ocurrir
 * - Los observadores pueden suscribirse a tipos específicos
 * - Permite filtrado de eventos por tipo
 */
public enum ProductoEventType {
    
    // =============================================
    // EVENTOS DE CICLO DE VIDA
    // =============================================
    
    /**
     * Se dispara cuando se crea un nuevo producto en el sistema.
     * Útil para: notificaciones de administradores, sincronización con catálogos externos
     */
    PRODUCTO_CREADO("Producto creado exitosamente"),
    
    /**
     * Se dispara cuando se actualiza información de un producto.
     * Útil para: auditoría de cambios, invalidación de caché
     */
    PRODUCTO_ACTUALIZADO("Producto actualizado exitosamente"),
    
    /**
     * Se dispara cuando se elimina (desactiva) un producto.
     * Útil para: limpieza de carritos, notificación a usuarios
     */
    PRODUCTO_ELIMINADO("Producto eliminado del catálogo"),
    
    // =============================================
    // EVENTOS DE STOCK
    // =============================================
    
    /**
     * Se dispara cuando el stock de un producto se actualiza.
     * Útil para: monitoreo de inventario, reportes de stock
     */
    STOCK_ACTUALIZADO("Stock de producto actualizado"),
    
    /**
     * Se dispara cuando el stock de un producto es bajo (< 5 unidades).
     * Útil para: alertas automáticas, reordenamiento automático
     */
    STOCK_BAJO("Stock bajo detectado"),
    
    /**
     * Se dispara cuando un producto se queda sin stock (0 unidades).
     * Útil para: notificaciones urgentes, desactivación automática
     */
    STOCK_AGOTADO("Producto sin stock disponible"),
    
    // =============================================
    // EVENTOS DE PRECIOS
    // =============================================
    
    /**
     * Se dispara cuando el precio de un producto cambia.
     * Útil para: notificaciones de cambios de precio, análisis de tendencias
     */
    PRECIO_CAMBIADO("Precio de producto modificado"),
    
    /**
     * Se dispara cuando se aplica un descuento a un producto.
     * Útil para: tracking de descuentos, análisis de efectividad
     */
    DESCUENTO_APLICADO("Descuento aplicado al producto"),
    
    // =============================================
    // EVENTOS DE ERRORES
    // =============================================
    
    /**
     * Se dispara cuando ocurre un error durante operaciones con productos.
     * Útil para: logging centralizado, alertas de sistema
     */
    ERROR_PRODUCTO("Error en operación de producto");
    
    // =============================================
    // PROPIEDADES Y MÉTODOS
    // =============================================
    
    private final String descripcion;
    
    ProductoEventType(String descripcion) {
        this.descripcion = descripcion;
    }
    
    /**
     * Obtiene la descripción legible del tipo de evento.
     * @return Descripción del evento en español
     */
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Verifica si este tipo de evento está relacionado con stock.
     * @return true si es un evento de stock, false en caso contrario
     */
    public boolean isEventoDeStock() {
        return this == STOCK_ACTUALIZADO || this == STOCK_BAJO || this == STOCK_AGOTADO;
    }
    
    /**
     * Verifica si este tipo de evento está relacionado con precios.
     * @return true si es un evento de precio, false en caso contrario
     */
    public boolean isEventoDePrecio() {
        return this == PRECIO_CAMBIADO || this == DESCUENTO_APLICADO;
    }
    
    /**
     * Verifica si este tipo de evento requiere atención urgente.
     * @return true si requiere atención inmediata, false en caso contrario
     */
    public boolean isEventoUrgente() {
        return this == STOCK_AGOTADO || this == ERROR_PRODUCTO;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", name(), descripcion);
    }
}
