package com.tiendadeportiva.backend.event;

import com.tiendadeportiva.backend.model.Producto;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Evento que se dispara cuando se crea un nuevo producto.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA:
 * - Desacopla las notificaciones del ProductoService
 * - Permite múltiples listeners sin modificar código existente
 * - Facilita testing individual de cada tipo de notificación
 * 
 * Patrón: Observer/Publisher-Subscriber
 */
public class ProductoCreadoEvent extends ApplicationEvent {
    
    private final Producto producto;
    private final LocalDateTime fechaEvento;
    private final String usuarioCreador;
    
    public ProductoCreadoEvent(Object source, Producto producto, String usuarioCreador) {
        super(source);
        this.producto = producto;
        this.fechaEvento = LocalDateTime.now();
        this.usuarioCreador = usuarioCreador;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }
    
    public String getUsuarioCreador() {
        return usuarioCreador;
    }
    
    @Override
    public String toString() {
        return String.format("ProductoCreadoEvent{producto='%s', fecha=%s, usuario='%s'}", 
                           producto.getNombre(), fechaEvento, usuarioCreador);
    }
}