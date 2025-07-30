package com.tiendadeportiva.backend.adapter.event;

import com.tiendadeportiva.backend.domain.port.EventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adaptador que implementa EventPublisherPort usando Spring Events.
 * 
 * ARQUITECTURA HEXAGONAL - Fase 2:
 * - Adaptador de infraestructura para publicación de eventos
 * - Traduce llamadas del dominio a Spring Events
 * - Facilita cambio a message brokers en fases futuras
 * - Mantiene el dominio independiente de Spring
 */
@Component
public class SpringEventPublisherAdapter implements EventPublisherPort {
    
    private final ApplicationEventPublisher springEventPublisher;
    
    public SpringEventPublisherAdapter(ApplicationEventPublisher springEventPublisher) {
        this.springEventPublisher = springEventPublisher;
    }
    
    @Override
    public void publishEvent(Object event) {
        springEventPublisher.publishEvent(event);
    }
    
    @Override
    public CompletableFuture<Void> publishEventAsync(Object event) {
        return CompletableFuture.runAsync(() -> {
            springEventPublisher.publishEvent(event);
        });
    }
}