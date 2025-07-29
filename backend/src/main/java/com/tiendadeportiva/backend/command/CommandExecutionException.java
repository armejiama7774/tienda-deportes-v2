package com.tiendadeportiva.backend.command;

/**
 * Excepción específica para errores en ejecución de comandos.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2:
 * - Manejo específico de errores en Command Pattern
 * - Información detallada para debugging y logging
 * - Códigos de error estructurados
 */
public class CommandExecutionException extends Exception {
    
    private final String commandType;
    private final String errorCode;
    
    public CommandExecutionException(String commandType, String errorCode, String message) {
        super(message);
        this.commandType = commandType;
        this.errorCode = errorCode;
    }
    
    public CommandExecutionException(String commandType, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.commandType = commandType;
        this.errorCode = errorCode;
    }
    
    public String getCommandType() {
        return commandType;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return String.format("CommandExecutionException{commandType='%s', errorCode='%s', message='%s'}", 
                           commandType, errorCode, getMessage());
    }
}