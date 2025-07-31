package com.tiendadeportiva.backend.event.observer;

import com.tiendadeportiva.backend.event.ProductoEvent;
import com.tiendadeportiva.backend.event.ProductoEventType;
import com.tiendadeportiva.backend.event.ProductoObserver;
import com.tiendadeportiva.backend.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador especializado en monitoreo de stock de productos.
 * 
 * EDUCATIVO PARA JUNIORS:
 * - Este observador implementa lógica específica para alertas de stock
 * - Demuestra cómo filtrar eventos por tipo usando el método isInterestedIn
 * - Muestra separación de responsabilidades: solo se ocupa del stock
 * - Ejemplo de logging estructurado para alertas
 * 
 * OBSERVER PATTERN - IMPLEMENTACIÓN ESPECÍFICA:
 * - Se suscribe solo a eventos relacionados con stock
 * - Procesa eventos de manera independiente de otros observadores
 * - Puede generar alertas, emails, o acciones automáticas
 * - Fácil extensión para nuevas funcionalidades de stock
 * 
 * CASOS DE USO:
 * - Alertas cuando el stock está bajo
 * - Notificaciones cuando se agota un producto
 * - Generación de reportes de inventario
 * - Activación de reordenamiento automático
 */
@Component
public class StockObserver implements ProductoObserver {
    
    private static final Logger logger = LoggerFactory.getLogger(StockObserver.class);
    
    // Configuración de umbrales de stock
    private static final int STOCK_BAJO_THRESHOLD = 5;
    private static final int STOCK_CRITICO_THRESHOLD = 1;
    
    @Override
    public void onProductoEvent(ProductoEvent event) {
        try {
            // Filtrar solo eventos de stock
            if (!isInterestedIn(event.getTipo())) {
                return;
            }
            
            logger.debug("📦 StockObserver procesando evento: {}", event.getTipo());
            
            Producto producto = event.getProducto();
            Integer stockActual = producto.getStockDisponible();
            
            switch (event.getTipo()) {
                case STOCK_ACTUALIZADO:
                    procesarActualizacionStock(producto, stockActual, event);
                    break;
                    
                case STOCK_BAJO:
                    procesarStockBajo(producto, stockActual, event);
                    break;
                    
                case STOCK_AGOTADO:
                    procesarStockAgotado(producto, event);
                    break;
                    
                default:
                    logger.debug("Evento de stock no manejado específicamente: {}", event.getTipo());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error en StockObserver procesando evento {}: {}", 
                        event.getEventId(), e.getMessage(), e);
            // No re-lanzamos la excepción para no afectar otros observadores
        }
    }
    
    @Override
    public boolean isInterestedIn(ProductoEventType eventType) {
        // Solo nos interesan eventos relacionados con stock
        return eventType.isEventoDeStock();
    }
    
    @Override
    public String getObserverName() {
        return "StockObserver";
    }
    
    @Override
    public int getPriority() {
        return 10; // Alta prioridad para alertas de stock
    }
    
    // =============================================
    // MÉTODOS PRIVADOS DE PROCESAMIENTO
    // =============================================
    
    /**
     * Procesa eventos de actualización de stock.
     */
    private void procesarActualizacionStock(Producto producto, Integer stockActual, ProductoEvent event) {
        logger.info("📊 Stock actualizado - Producto: {} (ID: {}), Nuevo stock: {}", 
                   producto.getNombre(), producto.getId(), stockActual);
        
        // Evaluar si necesitamos generar alertas adicionales
        if (stockActual != null) {
            if (stockActual <= STOCK_CRITICO_THRESHOLD) {
                generarAlertaStockCritico(producto, stockActual);
            } else if (stockActual <= STOCK_BAJO_THRESHOLD) {
                generarAlertaStockBajo(producto, stockActual);
            }
        }
        
        // Aquí podríamos:
        // - Enviar notificaciones por email
        // - Actualizar dashboards en tiempo real
        // - Activar procesos de reordenamiento
        // - Generar reportes automáticos
    }
    
    /**
     * Procesa eventos de stock bajo.
     */
    private void procesarStockBajo(Producto producto, Integer stockActual, ProductoEvent event) {
        logger.warn("⚠️ STOCK BAJO detectado - Producto: {} (ID: {}), Stock: {}", 
                   producto.getNombre(), producto.getId(), stockActual);
        
        generarAlertaStockBajo(producto, stockActual);
        
        // Acciones automáticas para stock bajo
        sugerirReordenamiento(producto, stockActual);
    }
    
    /**
     * Procesa eventos de stock agotado.
     */
    private void procesarStockAgotado(Producto producto, ProductoEvent event) {
        logger.error("🚨 STOCK AGOTADO - Producto: {} (ID: {})", 
                    producto.getNombre(), producto.getId());
        
        generarAlertaStockAgotado(producto);
        
        // Acciones críticas para stock agotado
        notificarStockCritico(producto);
    }
    
    // =============================================
    // MÉTODOS DE GENERACIÓN DE ALERTAS
    // =============================================
    
    private void generarAlertaStockBajo(Producto producto, Integer stock) {
        logger.warn("🔔 ALERTA: Stock bajo para '{}' - Quedan {} unidades (umbral: {})", 
                   producto.getNombre(), stock, STOCK_BAJO_THRESHOLD);
        
        // TODO: Implementar sistema de alertas
        // - Enviar email a administradores
        // - Crear notificación en dashboard
        // - Enviar mensaje a sistema de inventario
    }
    
    private void generarAlertaStockCritico(Producto producto, Integer stock) {
        logger.error("⚡ CRÍTICO: Stock muy bajo para '{}' - Solo {} unidades", 
                    producto.getNombre(), stock);
        
        // TODO: Implementar alertas críticas
        // - Notificación inmediata por SMS/WhatsApp
        // - Email prioritario a gerencia
        // - Alerta en sistemas de monitoreo
    }
    
    private void generarAlertaStockAgotado(Producto producto) {
        logger.error("🛑 URGENTE: Producto '{}' SIN STOCK disponible", producto.getNombre());
        
        // TODO: Implementar acciones de emergencia
        // - Desactivar producto en tienda online
        // - Notificar a departamento de compras
        // - Actualizar sistema de pedidos
    }
    
    // =============================================
    // MÉTODOS DE ACCIONES AUTOMÁTICAS
    // =============================================
    
    private void sugerirReordenamiento(Producto producto, Integer stockActual) {
        // Calcular cantidad sugerida para reordenamiento
        int cantidadSugerida = calcularCantidadReordenamiento(producto, stockActual);
        
        logger.info("💡 Sugerencia de reordenamiento para '{}': {} unidades", 
                   producto.getNombre(), cantidadSugerida);
        
        // TODO: Integrar con sistema de compras
        // - Crear orden de compra automática
        // - Notificar a proveedores
        // - Actualizar sistema ERP
    }
    
    private void notificarStockCritico(Producto producto) {
        logger.error("📢 Notificando stock crítico para producto: {}", producto.getNombre());
        
        // TODO: Implementar notificaciones críticas
        // - Alerta inmediata a gerencia
        // - Notificación a sistema de ventas
        // - Bloqueo de nuevas ventas del producto
    }
    
    private int calcularCantidadReordenamiento(Producto producto, Integer stockActual) {
        // Lógica simple para calcular cantidad de reordenamiento
        // En un sistema real, esto podría basarse en:
        // - Histórico de ventas
        // - Estacionalidad
        // - Lead time del proveedor
        // - Costo de almacenamiento
        
        String categoria = producto.getCategoria();
        
        // Cantidades base por categoría
        return switch (categoria.toLowerCase()) {
            case "zapatos" -> 20;
            case "camisetas" -> 50;
            case "pantalones" -> 30;
            default -> 25;
        };
    }
}
