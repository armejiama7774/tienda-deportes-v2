package com.tiendadeportiva.backend.event.observer;

import com.tiendadeportiva.backend.event.ProductoEvent;
import com.tiendadeportiva.backend.event.ProductoEventType;
import com.tiendadeportiva.backend.event.ProductoObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Observador de logging para el sistema de eventos
 * 
 * RESPONSABILIDADES:
 * - Registra todos los eventos del sistema para auditoría
 * - Proporciona logging estructurado para análisis posterior
 * - Mantiene historial completo de actividades del sistema
 * - Facilita debugging y monitoreo de la aplicación
 * 
 * EDUCATIVO - LOGGING OBSERVER:
 * - Ejemplo de Cross-Cutting Concern (preocupación transversal)
 * - Demuestra cómo el Observer Pattern facilita el logging centralizado
 * - Muestra técnicas de logging estructurado
 * - Preparado para integración con sistemas de monitoreo externos
 */
@Component
public class LoggingObserver implements ProductoObserver {
    
    private static final Logger eventLogger = LoggerFactory.getLogger("PRODUCTO_EVENTS");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Override
    public void onProductoEvent(ProductoEvent event) {
        try {
            // Logging básico del evento
            logEventoBasico(event);
            
            // Logging detallado para auditoría
            logEventoAuditoria(event);
            
            // Logging específico por tipo de evento
            logEventoEspecifico(event);
            
        } catch (Exception e) {
            // Nunca fallar el logging - pero sí registrar el error
            Logger logger = LoggerFactory.getLogger(LoggingObserver.class);
            logger.error("Error en LoggingObserver procesando evento {}: {}", 
                        event.getEventId(), e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isInterestedIn(ProductoEventType eventType) {
        // El LoggingObserver está interesado en TODOS los eventos
        return true;
    }
    
    @Override
    public int getPriority() {
        // Baja prioridad - el logging no debe interferir con lógica de negocio
        return 5;
    }
    
    @Override
    public boolean isAsynchronous() {
        // Logging asíncrono para no bloquear el flujo principal
        return true;
    }
    
    /**
     * Registra información básica del evento
     */
    private void logEventoBasico(ProductoEvent event) {
        eventLogger.info("EVENTO [{}] | ID: {} | Producto: '{}' (ID: {}) | Usuario: {} | Descripción: {}",
                        event.getTipo(),
                        event.getEventId(),
                        event.getProducto().getNombre(),
                        event.getProducto().getId(),
                        event.getUsuario() != null ? event.getUsuario() : "Sistema",
                        event.getDescripcion() != null ? event.getDescripcion() : "N/A");
    }
    
    /**
     * Registra información detallada para auditoría
     */
    private void logEventoAuditoria(ProductoEvent event) {
        auditLogger.info("AUDIT | Timestamp: {} | EventID: {} | EventType: {} | ProductoID: {} | Usuario: {} | Categoria: {} | Precio: {} | Stock: {}",
                        event.getTimestamp().format(formatter),
                        event.getEventId(),
                        event.getTipo(),
                        event.getProducto().getId(),
                        event.getUsuario() != null ? event.getUsuario() : "SYSTEM",
                        event.getProducto().getCategoria() != null ? event.getProducto().getCategoria() : "N/A",
                        event.getProducto().getPrecio(),
                        event.getProducto().getStockDisponible());
    }
    
    /**
     * Logging específico según el tipo de evento
     */
    private void logEventoEspecifico(ProductoEvent event) {
        switch (event.getTipo()) {
            case PRODUCTO_CREADO:
                logProductoCreado(event);
                break;
            case PRODUCTO_ACTUALIZADO:
                logProductoActualizado(event);
                break;
            case PRODUCTO_ELIMINADO:
                logProductoEliminado(event);
                break;
            case STOCK_ACTUALIZADO:
                logStockActualizado(event);
                break;
            case STOCK_BAJO:
                logStockBajo(event);
                break;
            case STOCK_AGOTADO:
                logStockAgotado(event);
                break;
            case PRECIO_CAMBIADO:
                logPrecioCambiado(event);
                break;
            case DESCUENTO_APLICADO:
                logDescuentoAplicado(event);
                break;
            case ERROR_PRODUCTO:
                logErrorProducto(event);
                break;
            default:
                eventLogger.debug("Evento sin logging específico: {}", event.getTipo());
        }
    }
    
    private void logProductoCreado(ProductoEvent event) {
        eventLogger.info("✅ PRODUCTO CREADO: '{}' - Categoría: {}, Precio: {}, Stock inicial: {}",
                        event.getProducto().getNombre(),
                        event.getProducto().getCategoria(),
                        event.getProducto().getPrecio(),
                        event.getProducto().getStockDisponible());
    }
    
    private void logProductoActualizado(ProductoEvent event) {
        eventLogger.info("🔄 PRODUCTO ACTUALIZADO: '{}' - {}", 
                        event.getProducto().getNombre(),
                        event.getDescripcion() != null ? event.getDescripcion() : "Actualización general");
    }
    
    private void logProductoEliminado(ProductoEvent event) {
        eventLogger.warn("🗑️ PRODUCTO ELIMINADO: '{}' - Razón: {}", 
                        event.getProducto().getNombre(),
                        event.getDescripcion() != null ? event.getDescripcion() : "No especificada");
    }
    
    private void logStockActualizado(ProductoEvent event) {
        eventLogger.info("📦 STOCK ACTUALIZADO: '{}' - Nuevo stock: {} unidades", 
                        event.getProducto().getNombre(),
                        event.getProducto().getStockDisponible());
    }
    
    private void logStockBajo(ProductoEvent event) {
        eventLogger.warn("⚠️ STOCK BAJO: '{}' - Solo {} unidades disponibles", 
                        event.getProducto().getNombre(),
                        event.getProducto().getStockDisponible());
    }
    
    private void logStockAgotado(ProductoEvent event) {
        eventLogger.error("🚫 STOCK AGOTADO: '{}' - Producto sin disponibilidad", 
                         event.getProducto().getNombre());
    }
    
    private void logPrecioCambiado(ProductoEvent event) {
        String datosAdicionales = "";
        if (event.getDatosAdicionales() instanceof Number) {
            datosAdicionales = String.format(" (anterior: %s)", event.getDatosAdicionales());
        }
        
        eventLogger.info("💰 PRECIO CAMBIADO: '{}' - Nuevo precio: {}{}", 
                        event.getProducto().getNombre(),
                        event.getProducto().getPrecio(),
                        datosAdicionales);
    }
    
    private void logDescuentoAplicado(ProductoEvent event) {
        eventLogger.info("🏷️ DESCUENTO APLICADO: '{}' - Precio con descuento: {} - Detalles: {}", 
                        event.getProducto().getNombre(),
                        event.getProducto().getPrecio(),
                        event.getDescripcion() != null ? event.getDescripcion() : "Descuento estándar");
    }
    
    private void logErrorProducto(ProductoEvent event) {
        eventLogger.error("❌ ERROR DE PRODUCTO: '{}' - Error: {}", 
                         event.getProducto().getNombre(),
                         event.getDescripcion() != null ? event.getDescripcion() : "Error no especificado");
    }
    
    /**
     * Método de utilidad para logging de métricas
     * Podría ser usado por otros componentes para métricas personalizadas
     */
    public void logMetrica(String nombre, Object valor, ProductoEvent evento) {
        eventLogger.info("📊 MÉTRICA [{}]: {} - Producto: '{}' - EventoID: {}", 
                        nombre, valor, evento.getProducto().getNombre(), evento.getEventId());
    }
    
    /**
     * Logging estructurado para integración con sistemas externos
     * (Elasticsearch, Splunk, etc.)
     */
    public void logEstructurado(ProductoEvent event) {
        // En un sistema real, esto podría ser JSON estructurado
        // para facilitar la ingesta por sistemas de logging externos
        eventLogger.info("STRUCTURED_LOG|{}|{}|{}|{}|{}|{}|{}|{}",
                        event.getTimestamp().format(formatter),
                        event.getEventId(),
                        event.getTipo(),
                        event.getProducto().getId(),
                        event.getProducto().getNombre().replace("|", "_"),
                        event.getUsuario() != null ? event.getUsuario() : "SYSTEM",
                        event.getProducto().getPrecio(),
                        event.getProducto().getStockDisponible());
    }
}
