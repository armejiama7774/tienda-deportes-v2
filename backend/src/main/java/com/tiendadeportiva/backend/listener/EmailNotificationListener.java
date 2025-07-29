package com.tiendadeportiva.backend.listener;

import com.tiendadeportiva.backend.event.ProductoCreadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener especializado en notificaciones por email.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA:
 * - Responsabilidad única: Solo maneja emails
 * - Asíncrono: No bloquea la creación del producto
 * - Testeable: Se puede testear independientemente
 * - Desacoplado: No conoce sobre ProductoService
 */
@Component
public class EmailNotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationListener.class);
    
    /**
     * Maneja notificaciones por email cuando se crea un producto
     * 
     * @Async: Se ejecuta asíncronamente para no bloquear la transacción principal
     */
    @Async
    @EventListener
    public void manejarProductoCreado(ProductoCreadoEvent evento) {
        try {
            logger.info("📧 Procesando notificación por email para producto: {}", 
                       evento.getProducto().getNombre());
            
            enviarEmailAdministradores(evento);
            
            logger.info("✅ Email enviado exitosamente para producto: {}", 
                       evento.getProducto().getNombre());
            
        } catch (Exception e) {
            // 🎯 MEJORA: Manejo de errores específico para emails
            logger.error("❌ Error enviando email para producto {}: {}", 
                        evento.getProducto().getId(), e.getMessage(), e);
            
            // TODO: En producción, aquí podríamos:
            // - Reintentar con backoff exponencial
            // - Enviar a cola de dead letter
            // - Alertar al equipo de operations
        }
    }
    
    /**
     * Envía email a administradores
     * EVOLUCIÓN: Método más especializado y testeable
     */
    private void enviarEmailAdministradores(ProductoCreadoEvent evento) {
        // Simular delay de servicio externo (async, no bloquea)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        // TODO: Integración real con servicio de email (SendGrid, SES, etc.)
        logger.info("📧 Email enviado a administradores sobre producto: {} - Precio: ${} - Creado por: {}", 
                   evento.getProducto().getNombre(), 
                   evento.getProducto().getPrecio(),
                   evento.getUsuarioCreador());
    }
}