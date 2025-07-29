package com.tiendadeportiva.backend.listener;

import com.tiendadeportiva.backend.event.ProductoCreadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener especializado en actualización de cache.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA:
 * - Responsabilidad única: Solo maneja invalidación/actualización de cache
 * - Asíncrono: No impacta performance de creación de producto
 * - Preparado para integración real con Redis/Hazelcast
 */
@Component
public class CacheNotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheNotificationListener.class);
    
    /**
     * Actualiza cache cuando se crea un producto
     */
    @Async
    @EventListener
    public void manejarProductoCreado(ProductoCreadoEvent evento) {
        try {
            logger.info("🔄 Procesando actualización de cache para producto: {}", 
                       evento.getProducto().getNombre());
            
            actualizarCacheCatalogo(evento);
            invalidarCachesCategorias(evento);
            
            logger.info("✅ Cache actualizado exitosamente para producto: {}", 
                       evento.getProducto().getNombre());
            
        } catch (Exception e) {
            logger.error("❌ Error actualizando cache para producto {}: {}", 
                        evento.getProducto().getId(), e.getMessage(), e);
            
            // TODO: En producción:
            // - Reintentar actualización de cache
            // - Alertar si el cache está fallando sistemáticamente
            // - Métricas de cache hit/miss
        }
    }
    
    /**
     * Actualiza cache principal del catálogo
     */
    private void actualizarCacheCatalogo(ProductoCreadoEvent evento) {
        // Simular operación de cache
        try {
            Thread.sleep(50); // Operación rápida de cache
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        // TODO: Integración real con Redis
        // redisTemplate.opsForValue().set("producto:" + evento.getProducto().getId(), evento.getProducto());
        // redisTemplate.opsForList().rightPush("productos:nuevos", evento.getProducto().getId());
        
        logger.info("🔄 Cache del catálogo actualizado - Producto: {} agregado a categoría: {}", 
                   evento.getProducto().getNombre(), 
                   evento.getProducto().getCategoria());
    }
    
    /**
     * Invalida caches relacionados con categorías
     */
    private void invalidarCachesCategorias(ProductoCreadoEvent evento) {
        // TODO: Invalidar caches específicos
        // redisTemplate.delete("categoria:" + evento.getProducto().getCategoria());
        // redisTemplate.delete("productos:destacados");
        
        logger.info("🔄 Caches de categoría invalidados para: {}", 
                   evento.getProducto().getCategoria());
    }
}