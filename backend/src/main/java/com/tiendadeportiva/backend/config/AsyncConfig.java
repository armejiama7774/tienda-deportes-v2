package com.tiendadeportiva.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono de eventos.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA:
 * - Permite que las notificaciones no bloqueen la transacción principal
 * - Mejora la performance de la API
 * - Preparación para arquitectura de microservicios
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Pool de threads para procesamiento asíncrono de eventos
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);      // Mínimo 2 threads
        executor.setMaxPoolSize(5);       // Máximo 5 threads  
        executor.setQueueCapacity(25);    // Cola de 25 tareas
        executor.setThreadNamePrefix("tienda-async-");
        executor.initialize();
        return executor;
    }
}