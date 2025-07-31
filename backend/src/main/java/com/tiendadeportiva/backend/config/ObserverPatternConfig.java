package com.tiendadeportiva.backend.config;

import com.tiendadeportiva.backend.event.ProductoEventPublisher;
import com.tiendadeportiva.backend.event.ProductoObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * Configuración automática del sistema Observer Pattern.
 * 
 * FUNCIONALIDADES:
 * - Auto-registro de todos los observadores al startup
 * - Logging detallado del proceso de inicialización  
 * - Verificación de estado del sistema de eventos
 * - Manejo de errores durante el registro
 * 
 * EDUCATIVO PARA JUNIORS:
 * - Demuestra uso de Spring's @EventListener para startup hooks
 * - Ejemplo de inyección de dependencias con List<Interface>
 * - Configuración automática vs manual de componentes
 * - Logging de inicialización para debugging
 */
@Configuration
public class ObserverPatternConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ObserverPatternConfig.class);
    
    private final ProductoEventPublisher eventPublisher;
    private final List<ProductoObserver> observers;
    
    public ObserverPatternConfig(ProductoEventPublisher eventPublisher, 
                                List<ProductoObserver> observers) {
        this.eventPublisher = eventPublisher;
        this.observers = observers;
    }
    
    /**
     * Se ejecuta automáticamente cuando Spring termina de cargar el contexto.
     * Registra todos los observadores encontrados en el container.
     */
    @EventListener
    public void onApplicationStartup(ContextRefreshedEvent event) {
        logger.info("🚀 Iniciando configuración del Observer Pattern...");
        
        if (observers.isEmpty()) {
            logger.warn("⚠️ No se encontraron observadores para registrar");
            return;
        }
        
        logger.info("🔍 Encontrados {} observadores para registrar", observers.size());
        
        int registradosExitosamente = 0;
        
        for (ProductoObserver observer : observers) {
            try {
                boolean registrado = eventPublisher.addObserver(observer);
                
                if (registrado) {
                    registradosExitosamente++;
                    logger.debug("✅ Observer registrado: {} (Prioridad: {}, Asíncrono: {})", 
                               observer.getClass().getSimpleName(),
                               observer.getPriority(),
                               observer.isAsynchronous());
                } else {
                    logger.warn("⚠️ Observer ya estaba registrado: {}", 
                               observer.getClass().getSimpleName());
                }
                
            } catch (Exception e) {
                logger.error("❌ Error registrando observer {}: {}", 
                           observer.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        
        // Mostrar resumen final
        logger.info("📊 Configuración Observer Pattern completada:");
        logger.info("   📋 Total observadores encontrados: {}", observers.size());
        logger.info("   ✅ Registrados exitosamente: {}", registradosExitosamente);
        logger.info("   📈 Estado del EventPublisher: {}", eventPublisher.getStats());
        
        // Verificar configuración
        if (registradosExitosamente == 0) {
            logger.error("💥 CRÍTICO: Ningún observer se registró correctamente");
        } else if (registradosExitosamente < observers.size()) {
            logger.warn("⚠️ No todos los observers se registraron correctamente");
        } else {
            logger.info("🎉 Todos los observers configurados exitosamente");
        }
        
        // Log de configuración por tipo
        logObserversByType();
    }
    
    /**
     * Registra información detallada sobre los tipos de observadores
     */
    private void logObserversByType() {
        logger.info("📋 Distribución de observadores por tipo:");
        
        for (ProductoObserver observer : observers) {
            String tipo = observer.isAsynchronous() ? "ASÍNCRONO" : "SÍNCRONO";
            String intereses = getObserverInterests(observer);
            
            logger.info("   🔸 {} - Tipo: {}, Prioridad: {}, Intereses: {}", 
                       observer.getClass().getSimpleName(),
                       tipo,
                       observer.getPriority(),
                       intereses);
        }
    }
    
    /**
     * Determina en qué tipos de eventos está interesado un observador
     */
    private String getObserverInterests(ProductoObserver observer) {
        StringBuilder interests = new StringBuilder();
        
        // Verificar interés en tipos principales de eventos
        com.tiendadeportiva.backend.event.ProductoEventType[] eventTypes = 
            com.tiendadeportiva.backend.event.ProductoEventType.values();
        
        for (com.tiendadeportiva.backend.event.ProductoEventType eventType : eventTypes) {
            try {
                if (observer.isInterestedIn(eventType)) {
                    if (interests.length() > 0) {
                        interests.append(", ");
                    }
                    interests.append(eventType.name());
                }
            } catch (Exception e) {
                // Ignorar errores en esta verificación
            }
        }
        
        return interests.length() > 0 ? interests.toString() : "TODOS";
    }
    
    /**
     * Método de utilidad para obtener estadísticas del sistema de observadores
     */
    public String getSystemStats() {
        return String.format("Observer Pattern Stats - Publisher: %s, Observers registrados: %d", 
                           eventPublisher.getStats(), 
                           eventPublisher.getObserverCount());
    }
}
