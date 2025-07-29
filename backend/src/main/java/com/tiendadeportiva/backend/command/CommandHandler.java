package com.tiendadeportiva.backend.command;

/**
 * Puerto para manejo de comandos.
 * 
 * EVOLUCIÓN HACIA ARQUITECTURA HEXAGONAL:
 * - Define el puerto (interfaz) para comandos
 * - Separación entre dominio e infraestructura
 * - Preparación para múltiples adaptadores
 */
public interface CommandHandler {
    
    /**
     * Ejecuta un comando de forma síncrona
     * @param command Comando a ejecutar
     * @return Resultado de la ejecución
     * @throws CommandExecutionException Si falla la ejecución
     */
    <T> T handle(Command<T> command) throws CommandExecutionException;
    
    /**
     * Ejecuta un comando de forma asíncrona
     * @param command Comando a ejecutar
     * @return Future con el resultado
     */
    <T> java.util.concurrent.CompletableFuture<T> handleAsync(Command<T> command);
}