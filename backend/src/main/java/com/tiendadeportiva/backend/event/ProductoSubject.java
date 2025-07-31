package com.tiendadeportiva.backend.event;

/**
 * Interface que define el contrato para subjects (publicadores) de eventos de productos.
 * 
 * EDUCATIVO PARA JUNIORS:
 * - Esta interface define las operaciones básicas de un Subject en Observer Pattern
 * - Permite suscribir, desuscribir y notificar observadores
 * - Mantiene la separación entre quien publica eventos y quien los consume
 * - Base para implementaciones que manejan listas de observadores
 * 
 * OBSERVER PATTERN - PARTE 4:
 * - Define CÓMO los subjects (publicadores) manejan observadores
 * - Operaciones CRUD para gestión de observadores
 * - Método de notificación para disparar eventos
 * - Permite múltiples implementaciones (local, distribuida, etc.)
 * 
 * CASOS DE USO:
 * - ProductoService puede implementar esta interface
 * - EventManager puede ser una implementación centralizada
 * - Permite testing fácil con implementaciones mock
 */
public interface ProductoSubject {
    
    // =============================================
    // GESTIÓN DE OBSERVADORES
    // =============================================
    
    /**
     * Registra un observador para recibir notificaciones de eventos.
     * 
     * IMPLEMENTACIÓN RECOMENDADA:
     * - Evitar duplicados del mismo observador
     * - Mantener orden basado en prioridad si es necesario
     * - Thread-safe si se usa en ambiente concurrente
     * 
     * @param observer El observador a registrar, no debe ser null
     * @return true si se registró exitosamente, false si ya estaba registrado
     */
    boolean addObserver(ProductoObserver observer);
    
    /**
     * Desregistra un observador para que deje de recibir notificaciones.
     * 
     * @param observer El observador a desregistrar
     * @return true si se desregistró exitosamente, false si no estaba registrado
     */
    boolean removeObserver(ProductoObserver observer);
    
    /**
     * Obtiene el número actual de observadores registrados.
     * Útil para monitoreo y debugging.
     * 
     * @return Cantidad de observadores registrados
     */
    int getObserverCount();
    
    /**
     * Verifica si hay observadores registrados.
     * 
     * @return true si hay al menos un observador registrado
     */
    default boolean hasObservers() {
        return getObserverCount() > 0;
    }
    
    /**
     * Limpia todos los observadores registrados.
     * Útil para cleanup en tests o shutdown del sistema.
     */
    void clearObservers();
    
    // =============================================
    // NOTIFICACIÓN DE EVENTOS
    // =============================================
    
    /**
     * Notifica a todos los observadores sobre un evento de producto.
     * 
     * IMPLEMENTACIÓN RECOMENDADA:
     * - Filtrar observadores según su interés en el tipo de evento
     * - Manejar excepciones de observadores para no afectar a otros
     * - Logging adecuado para debugging
     * - Considerar ejecución asíncrona para observadores que lo requieran
     * 
     * @param event El evento a notificar, no debe ser null
     */
    void notifyObservers(ProductoEvent event);
    
    /**
     * Método de conveniencia para crear y notificar un evento rápidamente.
     * 
     * @param tipo Tipo de evento
     * @param producto Producto afectado
     */
    default void notifyEvent(ProductoEventType tipo, com.tiendadeportiva.backend.model.Producto producto) {
        ProductoEvent event = ProductoEvent.of(tipo, producto);
        notifyObservers(event);
    }
    
    /**
     * Método de conveniencia para crear y notificar un evento con descripción.
     * 
     * @param tipo Tipo de evento
     * @param producto Producto afectado
     * @param descripcion Descripción adicional
     */
    default void notifyEvent(ProductoEventType tipo, com.tiendadeportiva.backend.model.Producto producto, String descripcion) {
        ProductoEvent event = ProductoEvent.of(tipo, producto, descripcion);
        notifyObservers(event);
    }
}
