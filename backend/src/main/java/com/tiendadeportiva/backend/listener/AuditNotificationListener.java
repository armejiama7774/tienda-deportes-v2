package com.tiendadeportiva.backend.listener;

import com.tiendadeportiva.backend.event.ProductoCreadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Listener especializado en auditoría y logging.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA:
 * - Responsabilidad única: Solo maneja auditoría
 * - Asíncrono: No bloquea operaciones de negocio
 * - Preparado para integración con sistemas de auditoría empresariales
 */
@Component
public class AuditNotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditNotificationListener.class);
    
    /**
     * Registra auditoría cuando se crea un producto
     */
    @Async
    @EventListener
    public void manejarProductoCreado(ProductoCreadoEvent evento) {
        try {
            logger.info("📋 Procesando auditoría para producto: {}", 
                       evento.getProducto().getNombre());
            
            registrarAuditoriaCreacion(evento);
            registrarMetricasNegocio(evento);
            
            logger.info("✅ Auditoría registrada exitosamente para producto: {}", 
                       evento.getProducto().getNombre());
            
        } catch (Exception e) {
            logger.error("❌ Error registrando auditoría para producto {}: {}", 
                        evento.getProducto().getId(), e.getMessage(), e);
            
            // TODO: En producción:
            // - Auditoría es crítica, podría requerir retry obligatorio
            // - Alertar inmediatamente si falla auditoría
            // - Backup en sistema de auditoría secundario
        }
    }
    
    /**
     * Registra evento de auditoría en sistema de trazabilidad
     */
    private void registrarAuditoriaCreacion(ProductoCreadoEvent evento) {
        // Simular persistencia en tabla de auditoría
        try {
            Thread.sleep(30); // Operación rápida de DB
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        // TODO: Persistir en tabla AUDIT_LOG
        /*
        AuditLog auditLog = AuditLog.builder()
            .entidad("PRODUCTO")
            .entidadId(evento.getProducto().getId())
            .accion("CREAR")
            .usuario(evento.getUsuarioCreador())
            .fecha(evento.getFechaEvento())
            .datosAnteriores(null)
            .datosNuevos(objectMapper.writeValueAsString(evento.getProducto()))
            .build();
        auditRepository.save(auditLog);
        */
        
        logger.info("📋 Auditoría registrada - Producto ID: {} creado por: {} en: {}", 
                   evento.getProducto().getId(), 
                   evento.getUsuarioCreador(), 
                   evento.getFechaEvento());
    }
    
    /**
     * Registra métricas de negocio
     */
    private void registrarMetricasNegocio(ProductoCreadoEvent evento) {
        // TODO: Integración con sistemas de métricas (Micrometer, Prometheus)
        // meterRegistry.counter("productos.creados", "categoria", evento.getProducto().getCategoria()).increment();
        // meterRegistry.gauge("productos.precio.promedio", evento.getProducto().getPrecio().doubleValue());
        
        logger.info("📊 Métricas actualizadas - Categoría: {} - Precio: ${}", 
                   evento.getProducto().getCategoria(), 
                   evento.getProducto().getPrecio());
    }
}