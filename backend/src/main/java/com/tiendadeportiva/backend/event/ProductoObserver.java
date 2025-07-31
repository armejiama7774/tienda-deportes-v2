package com.tiendadeportiva.backend.event;

/**
 * Interface que define el contrato para observadores de eventos de productos.
 * 
 * EDUCATIVO PARA JUNIORS:
 * - Esta interface define el contrato que deben cumplir todos los observadores
 * - Método único y simple para mantener la interface pequeña (ISP - Interface Segregation)
 * - Permite múltiples implementaciones con diferentes comportamientos
 * - Fácil testing mediante mocks o implementaciones test
 * 
 * OBSERVER PATTERN - PARTE 3:
 * - Define CÓMO los observadores serán notificados
 * - Desacopla completamente el subject de los observadores
 * - Permite agregar nuevos observadores sin modificar código existente
 * - Base para implementaciones específicas (StockObserver, PrecioObserver, etc.)
 * 
 * PRINCIPIOS SOLID APLICADOS:
 * - Single Responsibility: Solo define cómo recibir notificaciones
 * - Open/Closed: Abierto para nuevas implementaciones, cerrado para modificación
 * - Interface Segregation: Interface pequeña y específica
 * - Dependency Inversion: Dependemos de abstracción, no de implementaciones
 */
@FunctionalInterface
public interface ProductoObserver {
    
    /**
     * Método llamado cuando ocurre un evento de producto.
     * 
     * IMPORTANTE PARA IMPLEMENTADORES:
     * - Este método debe ser rápido y no bloquear el hilo principal
     * - Para operaciones pesadas, considerar ejecución asíncrona
     * - Manejar excepciones internamente para no afectar otros observadores
     * - Logging adecuado para debugging y monitoreo
     * 
     * @param event El evento que ocurrió, nunca null
     * @throws RuntimeException Solo en casos excepcionales que requieran detener el procesamiento
     */
    void onProductoEvent(ProductoEvent event);
    
    // =============================================
    // MÉTODOS DEFAULT PARA FUNCIONALIDAD ADICIONAL
    // =============================================
    
    /**
     * Verifica si este observador está interesado en un tipo específico de evento.
     * Por defecto acepta todos los eventos, pero puede ser sobrescrito.
     * 
     * @param eventType Tipo de evento a verificar
     * @return true si está interesado en este tipo de evento
     */
    default boolean isInterestedIn(ProductoEventType eventType) {
        return true; // Por defecto, acepta todos los eventos
    }
    
    /**
     * Obtiene el nombre identificador de este observador.
     * Útil para logging y debugging.
     * 
     * @return Nombre identificador del observador
     */
    default String getObserverName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Obtiene la prioridad de este observador.
     * Observadores con mayor prioridad se ejecutan primero.
     * 
     * @return Prioridad del observador (0 = normal, mayor número = mayor prioridad)
     */
    default int getPriority() {
        return 0; // Prioridad normal por defecto
    }
    
    /**
     * Verifica si este observador debe ejecutarse de manera asíncrona.
     * Por defecto es síncrono para mantener simplicidad.
     * 
     * @return true si debe ejecutarse asíncronamente
     */
    default boolean isAsynchronous() {
        return false; // Síncrono por defecto
    }
}
