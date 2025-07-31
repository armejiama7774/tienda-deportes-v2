package com.tiendadeportiva.backend.event;

import com.tiendadeportiva.backend.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementación concreta del ProductoSubject para gestión de eventos.
 * 
 * CARACTERÍSTICAS EDUCATIVAS:
 * - Thread-safe usando CopyOnWriteArrayList
 * - Soporte para observadores síncronos y asíncronos
 * - Manejo de errores robusto que no interrumpe otros observadores
 * - Logging detallado para debugging y monitoreo
 * - Ordenamiento por prioridad de observadores
 * 
 * OBSERVER PATTERN - IMPLEMENTACIÓN CONCRETA:
 * - Gestiona la lista de observadores registrados
 * - Notifica a todos los observadores interesados en cada evento
 * - Proporciona métodos de conveniencia para crear eventos comunes
 */
@Component
public class ProductoEventPublisher implements ProductoSubject {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoEventPublisher.class);
    
    // Thread-safe list para entornos concurrentes
    private final List<ProductoObserver> observadores = new CopyOnWriteArrayList<>();
    
    @Override
    public boolean addObserver(ProductoObserver observer) {
        if (observer != null && !observadores.contains(observer)) {
            observadores.add(observer);
            
            // Ordenar por prioridad (menor número = mayor prioridad)
            observadores.sort((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()));
            
            logger.info("🔔 Observer agregado: {} (Prioridad: {}) - Total observadores: {}", 
                       observer.getClass().getSimpleName(), 
                       observer.getPriority(), 
                       observadores.size());
            
            return true;
        }
        return false;
    }
    
    @Override
    public boolean removeObserver(ProductoObserver observer) {
        if (observadores.remove(observer)) {
            logger.info("🔕 Observer removido: {} - Total observadores: {}", 
                       observer.getClass().getSimpleName(), 
                       observadores.size());
            return true;
        }
        return false;
    }
    
    @Override
    public int getObserverCount() {
        return observadores.size();
    }
    
    @Override
    public void clearObservers() {
        int count = observadores.size();
        observadores.clear();
        logger.info("🧹 Todos los observadores eliminados - Total removidos: {}", count);
    }
    
    @Override
    public void notifyObservers(ProductoEvent event) {
        if (event == null) {
            logger.warn("⚠️ Intento de notificar evento nulo - ignorando");
            return;
        }
        
        logger.debug("📢 Notificando evento: {} para producto '{}' a {} observadores", 
                    event.getTipo(), 
                    event.getProducto().getNombre(), 
                    observadores.size());
        
        List<ProductoObserver> observadoresInteresados = new ArrayList<>();
        List<ProductoObserver> observadoresAsincronos = new ArrayList<>();
        
        // Filtrar observadores interesados y separarlos por tipo de ejecución
        for (ProductoObserver observer : observadores) {
            try {
                if (observer.isInterestedIn(event.getTipo())) {
                    if (observer.isAsynchronous()) {
                        observadoresAsincronos.add(observer);
                    } else {
                        observadoresInteresados.add(observer);
                    }
                }
            } catch (Exception e) {
                logger.error("❌ Error verificando interés del observer {}: {}", 
                           observer.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        
        logger.debug("📊 Distribución de observadores - Síncronos: {}, Asíncronos: {}", 
                    observadoresInteresados.size(), observadoresAsincronos.size());
        
        // Ejecutar observadores síncronos primero
        ejecutarObservadoresSincronos(event, observadoresInteresados);
        
        // Ejecutar observadores asíncronos
        ejecutarObservadoresAsincronos(event, observadoresAsincronos);
    }
    
    /**
     * Ejecuta observadores síncronos en el hilo actual
     */
    private void ejecutarObservadoresSincronos(ProductoEvent event, List<ProductoObserver> observadores) {
        for (ProductoObserver observer : observadores) {
            try {
                long inicio = System.currentTimeMillis();
                observer.onProductoEvent(event);
                long duracion = System.currentTimeMillis() - inicio;
                
                logger.debug("✅ Observer síncrono ejecutado: {} - Tiempo: {}ms", 
                           observer.getClass().getSimpleName(), duracion);
                           
            } catch (Exception e) {
                logger.error("❌ Error en observer síncrono {}: {}", 
                           observer.getClass().getSimpleName(), e.getMessage(), e);
                // Continuar con otros observadores
            }
        }
    }
    
    /**
     * Ejecuta observadores asíncronos en hilos separados
     */
    private void ejecutarObservadoresAsincronos(ProductoEvent event, List<ProductoObserver> observadores) {
        for (ProductoObserver observer : observadores) {
            CompletableFuture.runAsync(() -> {
                try {
                    long inicio = System.currentTimeMillis();
                    observer.onProductoEvent(event);
                    long duracion = System.currentTimeMillis() - inicio;
                    
                    logger.debug("✅ Observer asíncrono ejecutado: {} - Tiempo: {}ms", 
                               observer.getClass().getSimpleName(), duracion);
                               
                } catch (Exception e) {
                    logger.error("❌ Error en observer asíncrono {}: {}", 
                               observer.getClass().getSimpleName(), e.getMessage(), e);
                }
            }).exceptionally(throwable -> {
                logger.error("💥 Error fatal en observer asíncrono {}: {}", 
                           observer.getClass().getSimpleName(), throwable.getMessage(), throwable);
                return null;
            });
        }
    }
    
    // =============================================
    // MÉTODOS DE CONVENIENCIA PARA EVENTOS COMUNES
    // =============================================
    
    public void notifyProductoCreado(Producto producto, String usuario) {
        ProductoEvent event = ProductoEvent.builder()
                .conTipo(ProductoEventType.PRODUCTO_CREADO)
                .conProducto(producto)
                .conUsuario(usuario)
                .conDescripcion("Producto creado exitosamente en el sistema")
                .build();
        
        notifyObservers(event);
    }
    
    public void notifyProductoActualizado(Producto producto, String usuario) {
        ProductoEvent event = ProductoEvent.builder()
                .conTipo(ProductoEventType.PRODUCTO_ACTUALIZADO)
                .conProducto(producto)
                .conUsuario(usuario)
                .conDescripcion("Información del producto actualizada")
                .build();
        
        notifyObservers(event);
    }
    
    public void notifyProductoEliminado(Producto producto, String usuario) {
        ProductoEvent event = ProductoEvent.builder()
                .conTipo(ProductoEventType.PRODUCTO_ELIMINADO)
                .conProducto(producto)
                .conUsuario(usuario)
                .conDescripcion("Producto eliminado del catálogo")
                .build();
        
        notifyObservers(event);
    }
    
    public void notifyStockActualizado(Producto producto, String usuario) {
        ProductoEvent event = ProductoEvent.builder()
                .conTipo(ProductoEventType.STOCK_ACTUALIZADO)
                .conProducto(producto)
                .conUsuario(usuario)
                .conDescripcion("Stock del producto actualizado")
                .build();
        
        notifyObservers(event);
    }
    
    public void notifyPrecioCambiado(Producto producto, String usuario, Object datosAdicionales) {
        ProductoEvent event = ProductoEvent.builder()
                .conTipo(ProductoEventType.PRECIO_CAMBIADO)
                .conProducto(producto)
                .conUsuario(usuario)
                .conDescripcion("Precio del producto modificado")
                .conDatosAdicionales(datosAdicionales)
                .build();
        
        notifyObservers(event);
    }
    
    public void notifyDescuentoAplicado(Producto producto, String usuario, String detallesDescuento) {
        ProductoEvent event = ProductoEvent.builder()
                .conTipo(ProductoEventType.DESCUENTO_APLICADO)
                .conProducto(producto)
                .conUsuario(usuario)
                .conDescripcion(detallesDescuento)
                .build();
        
        notifyObservers(event);
    }
    
    /**
     * Método de utilidad para obtener estadísticas de observadores
     */
    public ObserverStats getStats() {
        int sincronos = 0;
        int asincronos = 0;
        
        for (ProductoObserver observer : observadores) {
            if (observer.isAsynchronous()) {
                asincronos++;
            } else {
                sincronos++;
            }
        }
        
        return new ObserverStats(observadores.size(), sincronos, asincronos);
    }
    
    /**
     * Clase interna para estadísticas de observadores
     */
    public static class ObserverStats {
        private final int total;
        private final int sincronos;
        private final int asincronos;
        
        public ObserverStats(int total, int sincronos, int asincronos) {
            this.total = total;
            this.sincronos = sincronos;
            this.asincronos = asincronos;
        }
        
        public int getTotal() { return total; }
        public int getSincronos() { return sincronos; }
        public int getAsincronos() { return asincronos; }
        
        @Override
        public String toString() {
            return String.format("ObserverStats{total=%d, síncronos=%d, asíncronos=%d}", 
                               total, sincronos, asincronos);
        }
    }
}
