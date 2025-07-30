package com.tiendadeportiva.backend.domain.port;

import com.tiendadeportiva.backend.model.Producto;

/**
 * Puerto del dominio para servicios de notificación.
 * 
 * ARQUITECTURA HEXAGONAL - Fase 2:
 * - Define contratos para notificaciones externas
 * - Independiente de proveedores específicos (SendGrid, AWS SES, etc.)
 * - Facilita testing y cambio de proveedores
 */
public interface NotificationPort {
    
    /**
     * Envía notificación por email sobre creación de producto
     * @param producto Producto creado
     * @param destinatario Email del destinatario
     * @return true si se envió exitosamente
     */
    boolean sendProductCreatedEmail(Producto producto, String destinatario);
    
    /**
     * Envía notificación por email sobre actualización de producto
     * @param producto Producto actualizado
     * @param destinatario Email del destinatario
     * @return true si se envió exitosamente
     */
    boolean sendProductUpdatedEmail(Producto producto, String destinatario);
    
    /**
     * Envía notificación por email sobre eliminación de producto
     * @param productoNombre Nombre del producto eliminado
     * @param destinatario Email del destinatario
     * @return true si se envió exitosamente
     */
    boolean sendProductDeletedEmail(String productoNombre, String destinatario);
}