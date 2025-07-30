package com.tiendadeportiva.backend.command;

/**
 * Interfaz base para el patrón Command.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2:
 * - Encapsula operaciones complejas como objetos
 * - Permite deshacer operaciones (undo)
 * - Facilita logging y auditoría de operaciones
 * - Preparación para CQRS en fases futuras
 * 
 * @param <T> Tipo de resultado que devuelve el comando
 */
public interface Command<T> {
    
    /**
     * Ejecuta el comando
     * @return Resultado de la ejecución
     * @throws CommandExecutionException Si falla la ejecución
     */
    T execute() throws CommandExecutionException;
    
    /**
     * Revierte el comando (si es posible)
     * @throws CommandExecutionException Si falla la reversión
     */
    default void undo() throws CommandExecutionException {
        throw new UnsupportedOperationException("Este comando no soporta deshacer");
    }
    
    /**
     * Valida si el comando puede ejecutarse
     * @return true si es válido para ejecutar
     */
    default boolean isValid() {
        return true;
    }
    
    /**
     * Descripción del comando para logging/auditoría
     * @return Descripción legible del comando
     */
    String getDescription();
}
/*
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ProductoController│────▶│ ProductoService │────▶│Command<Producto>│
│    (CLIENT)     │     │   (INVOKER)     │     │  <<interface>>  │
│                 │     │                 │     │                 │
│ + crearProducto │     │ + crearProducto │     │ + execute()     │
│   (RequestBody) │     │ - Crea comando  │     │ + undo()        │
│                 │     │ - Solicita exec │     │ + isValid()     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                       │                       △
         │                       │                       │
         │                       │                       │
         │                       ▼               ┌───────────────────┐
         │               ┌──────────────┐        │CrearProductoCommand│
         │               │CommandHandler│◀───────│ (CONCRETE COMMAND) │
         │               │ (RECEIVER)   │        │                   │
         └──────────────▶│              │        │ - producto        │
                         │ + handle()   │        │ - repository      │
                         │ + handleAsync│        │ - eventPublisher  │
                         │ - logging    │        │ + execute()       │
                         │ - metrics    │        │ + undo()          │
                         │ - validation │        │ + isValid()       │
                         └──────────────┘        └───────────────────┘
*/

