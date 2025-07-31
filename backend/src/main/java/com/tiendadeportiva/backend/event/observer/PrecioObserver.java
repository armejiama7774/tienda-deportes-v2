package com.tiendadeportiva.backend.event.observer;

import com.tiendadeportiva.backend.event.ProductoEvent;
import com.tiendadeportiva.backend.event.ProductoEventType;
import com.tiendadeportiva.backend.event.ProductoObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Observador específico para monitorear cambios de precios
 * Implementa alertas automáticas para cambios significativos de precio
 * 
 * Funcionalidades:
 * - Detecta cambios de precio y descuentos aplicados
 * - Genera alertas para cambios significativos
 * - Registra historial de cambios para análisis
 * - Monitorea la aplicación de descuentos
 */
@Component
public class PrecioObserver implements ProductoObserver {
    
    private static final Logger logger = LoggerFactory.getLogger(PrecioObserver.class);
    
    // Configuración de umbrales
    private static final BigDecimal UMBRAL_CAMBIO_SIGNIFICATIVO = new BigDecimal("0.15"); // 15%
    private static final BigDecimal UMBRAL_ALERTA_PRECIO = new BigDecimal("0.25"); // 25%
    
    @Override
    public void onProductoEvent(ProductoEvent event) {
        try {
            if (event.getTipo().isEventoDePrecio()) {
                procesarEventoDePrecio(event);
            }
        } catch (Exception e) {
            logger.error("Error procesando evento de precio para producto {}: {}", 
                        event.getProducto().getId(), e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isInterestedIn(ProductoEventType eventType) {
        return eventType.isEventoDePrecio();
    }
    
    @Override
    public int getPriority() {
        return 2; // Alta prioridad para cambios de precio
    }
    
    @Override
    public boolean isAsynchronous() {
        return true; // Los análisis de precio pueden ser asíncronos
    }
    
    /**
     * Procesa eventos específicos de precio
     */
    private void procesarEventoDePrecio(ProductoEvent event) {
        switch (event.getTipo()) {
            case PRECIO_CAMBIADO:
                analizarCambioDePrecio(event);
                break;
            case DESCUENTO_APLICADO:
                procesarDescuentoAplicado(event);
                break;
            default:
                logger.debug("Tipo de evento de precio no manejado: {}", event.getTipo());
        }
    }
    
    /**
     * Analiza cambios de precio usando la información disponible en el evento
     */
    private void analizarCambioDePrecio(ProductoEvent event) {
        BigDecimal precioActual = event.getProducto().getPrecio();
        
        logger.info("Cambio de precio detectado para producto '{}' (ID: {}): Precio actual: {}",
                   event.getProducto().getNombre(), 
                   event.getProducto().getId(), 
                   precioActual);
        
        // Si hay datos adicionales sobre el precio anterior, podemos analizarlo
        if (event.getDatosAdicionales() instanceof BigDecimal) {
            BigDecimal precioAnterior = (BigDecimal) event.getDatosAdicionales();
            analizarComparacionPrecios(event, precioAnterior, precioActual);
        }
        
        // Verificar si el precio requiere atención especial
        verificarPrecioEspecial(event, precioActual);
        
        registrarCambioEnHistorial(event);
    }
    
    /**
     * Analiza la comparación entre precio anterior y nuevo
     */
    private void analizarComparacionPrecios(ProductoEvent event, BigDecimal precioAnterior, BigDecimal precioNuevo) {
        if (precioAnterior.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal porcentajeCambio = calcularPorcentajeCambio(precioAnterior, precioNuevo);
            
            logger.info("Comparación de precios - Producto '{}': {} -> {} (cambio: {}%)",
                       event.getProducto().getNombre(), precioAnterior, precioNuevo, 
                       porcentajeCambio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            
            if (esCambioSignificativo(porcentajeCambio)) {
                generarAlertaCambioSignificativo(event, precioAnterior, precioNuevo, porcentajeCambio);
            }
        }
    }
    
    /**
     * Procesa eventos de descuentos aplicados
     */
    private void procesarDescuentoAplicado(ProductoEvent event) {
        logger.info("💳 Descuento aplicado al producto '{}' (ID: {}): {}",
                   event.getProducto().getNombre(),
                   event.getProducto().getId(),
                   event.getDescripcion() != null ? event.getDescripcion() : "Descuento genérico");
        
        // Verificar que el descuento sea razonable
        validarDescuento(event);
        
        // Notificar descuento a clientes interesados
        notificarDescuento(event);
    }
    
    /**
     * Verifica si un precio requiere atención especial
     */
    private void verificarPrecioEspecial(ProductoEvent event, BigDecimal precio) {
        // Verificar precios muy altos o muy bajos
        if (precio.compareTo(new BigDecimal("1000")) > 0) {
            logger.warn("⚠️  Precio alto detectado en producto '{}': {}",
                       event.getProducto().getNombre(), precio);
        } else if (precio.compareTo(new BigDecimal("1")) < 0) {
            logger.warn("⚠️  Precio muy bajo detectado en producto '{}': {}",
                       event.getProducto().getNombre(), precio);
        }
    }
    
    /**
     * Calcula el porcentaje de cambio entre dos precios
     */
    private BigDecimal calcularPorcentajeCambio(BigDecimal precioAnterior, BigDecimal precioNuevo) {
        if (precioAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE; // 100% de cambio si el precio anterior era 0
        }
        
        return precioNuevo.subtract(precioAnterior)
                         .divide(precioAnterior, 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Determina si un cambio de precio es significativo
     */
    private boolean esCambioSignificativo(BigDecimal porcentajeCambio) {
        return porcentajeCambio.abs().compareTo(UMBRAL_CAMBIO_SIGNIFICATIVO) > 0;
    }
    
    /**
     * Genera alerta para cambios significativos de precio
     */
    private void generarAlertaCambioSignificativo(ProductoEvent event, BigDecimal precioAnterior, 
                                                 BigDecimal precioNuevo, BigDecimal porcentajeCambio) {
        
        String tipoAlerta = porcentajeCambio.compareTo(BigDecimal.ZERO) > 0 ? "AUMENTO" : "DISMINUCIÓN";
        BigDecimal porcentajeAbs = porcentajeCambio.multiply(new BigDecimal("100")).abs()
                                                  .setScale(2, RoundingMode.HALF_UP);
        
        if (porcentajeCambio.abs().compareTo(UMBRAL_ALERTA_PRECIO) > 0) {
            logger.warn("🚨 ALERTA DE PRECIO: {} significativo del {}% en producto '{}' ({} -> {})",
                       tipoAlerta, porcentajeAbs, event.getProducto().getNombre(), 
                       precioAnterior, precioNuevo);
            
            enviarAlertaEmpresarial(event, tipoAlerta, porcentajeAbs);
        } else {
            logger.info("💰 Cambio de precio significativo: {} del {}% en producto '{}'",
                       tipoAlerta, porcentajeAbs, event.getProducto().getNombre());
        }
    }
    
    /**
     * Valida que el descuento aplicado sea coherente
     */
    private void validarDescuento(ProductoEvent event) {
        logger.debug("Validando descuento para producto '{}': {}", 
                    event.getProducto().getNombre(), 
                    event.getDescripcion());
        
        // Aquí se implementarían validaciones de negocio:
        // - Verificar que no hay descuentos conflictivos
        // - Validar que el precio con descuento sea positivo
        // - Comprobar límites de descuento por categoría
    }
    
    /**
     * Notifica descuento a clientes interesados
     */
    private void notificarDescuento(ProductoEvent event) {
        logger.info("🔔 Preparando notificaciones de descuento para producto '{}'", 
                   event.getProducto().getNombre());
        
        // Integración futura con sistema de notificaciones push/email
        // Por ahora solo logging para demostración educativa
    }
    
    /**
     * Registra el cambio en el historial para análisis posterior
     */
    private void registrarCambioEnHistorial(ProductoEvent event) {
        logger.debug("📝 Registrando cambio de precio en historial: Producto='{}', Evento={}, Usuario={}",
                    event.getProducto().getNombre(), 
                    event.getTipo(), 
                    event.getUsuario() != null ? event.getUsuario() : "Sistema");
        
        // Aquí se podría persistir en base de datos para análisis de tendencias
        // Por ahora solo logging para demostración educativa
    }
    
    /**
     * Envía alerta empresarial para cambios críticos de precio
     */
    private void enviarAlertaEmpresarial(ProductoEvent event, String tipoAlerta, BigDecimal porcentaje) {
        logger.warn("📊 ALERTA EMPRESARIAL: {} crítico de {}% en producto '{}' - Requiere revisión",
                   tipoAlerta, porcentaje, event.getProducto().getNombre());
        
        // Integración futura con sistemas de alertas empresariales
        // Slack, Teams, email a gerencia, dashboard de alertas, etc.
    }
}
