package com.tiendadeportiva.backend.service.descuento;

import com.tiendadeportiva.backend.model.Producto;
import java.math.BigDecimal;

/**
 * Interface para estrategias de descuento.
 * 
 * FASE 2: ARQUITECTURA HEXAGONAL - STRATEGY PATTERN
 * 
 * MEJORA APLICADA: Uso de DescuentoContexto en lugar de parámetros individuales
 * - Más extensible y preparado para casos de uso complejos
 * - Compatible con arquitectura de microservicios futura
 * - Facilita testing con diferentes escenarios
 * 
 * @author Equipo Desarrollo
 * @version 2.1.1
 * @since Fase 2 - Mejoras con Patrones de Diseño
 */
public interface IDescuentoStrategy {
    
    /**
     * Calcula el descuento aplicable a un producto dado el contexto.
     * 
     * MEJORA: Ahora recibe un contexto completo en lugar de solo cantidad
     * - Permite descuentos complejos (VIP + código + temporada)
     * - Escalable para futuras necesidades
     * - Mantiene la interface estable
     * 
     * @param producto El producto al que se aplicará el descuento
     * @param contexto Contexto completo de la compra
     * @return Monto del descuento a aplicar (cero si no aplica)
     */
    BigDecimal calcularDescuento(Producto producto, DescuentoContexto contexto);
    
    /**
     * Verifica si esta estrategia es aplicable al producto y contexto dados.
     * 
     * @param producto El producto a evaluar
     * @param contexto Contexto de la compra
     * @return true si la estrategia puede aplicar descuento
     */
    boolean esAplicable(Producto producto, DescuentoContexto contexto);
    
    /**
     * Obtiene el nombre descriptivo de la estrategia.
     */
    String getNombreEstrategia();
    
    /**
     * Obtiene la prioridad de la estrategia.
     */
    int getPrioridad();
}