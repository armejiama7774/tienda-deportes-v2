package com.tiendadeportiva.backend.command.impl;

import com.tiendadeportiva.backend.command.Command;
import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Implementación del manejador de comandos.
 * 
 * ADAPTADOR de infraestructura para el puerto CommandHandler.
 * Maneja la ejecución, logging y métricas de comandos.
 */
@Service
public class CommandHandlerImpl implements CommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandHandlerImpl.class);
    
    @Override
    public <T> T handle(Command<T> command) throws CommandExecutionException {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("🎯 Ejecutando comando: {}", command.getDescription());
            
            // Validar comando
            if (!command.isValid()) {
                throw new CommandExecutionException(
                    command.getClass().getSimpleName(),
                    "INVALID_COMMAND",
                    "El comando no es válido: " + command.getDescription()
                );
            }
            
            // Ejecutar comando
            T result = command.execute();
            
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("✅ Comando ejecutado exitosamente en {}ms: {}", 
                       executionTime, command.getDescription());
            
            return result;
            
        } catch (CommandExecutionException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("❌ Error ejecutando comando en {}ms: {} - Error: {}", 
                        executionTime, command.getDescription(), e.getMessage());
            throw e;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("❌ Error inesperado ejecutando comando en {}ms: {} - Error: {}", 
                        executionTime, command.getDescription(), e.getMessage(), e);
            
            throw new CommandExecutionException(
                command.getClass().getSimpleName(),
                "UNEXPECTED_ERROR",
                "Error inesperado: " + e.getMessage(),
                e
            );
        }
    }
    
    @Async
    @Override
    public <T> CompletableFuture<T> handleAsync(Command<T> command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return handle(command);
            } catch (CommandExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}