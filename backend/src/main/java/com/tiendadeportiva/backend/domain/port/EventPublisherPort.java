package com.tiendadeportiva.backend.domain.port;

/**
 * Puerto del dominio para publicación de eventos.
 * 
 * ARQUITECTURA HEXAGONAL - Fase 2:
 * - Abstrae el mecanismo de publicación de eventos
 * - Independiente de Spring Events o cualquier otra tecnología
 * - Facilita testing con publishers mock
 * - Permite cambiar a message brokers sin afectar dominio
 */
public interface EventPublisherPort {
    
    /**
     * Publica un evento de dominio
     * @param event Evento a publicar
     */
    void publishEvent(Object event);
    
    /**
     * Publica un evento de dominio de forma asíncrona
     * @param event Evento a publicar
     * @return CompletableFuture para manejo asíncrono
     */
    java.util.concurrent.CompletableFuture<Void> publishEventAsync(Object event);
}