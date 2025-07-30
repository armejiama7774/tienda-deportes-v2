package com.tiendadeportiva.backend.service.descuento;

import com.tiendadeportiva.backend.model.Producto;
import java.math.BigDecimal;

/**
 * Strategy Pattern para cálculo de descuentos.
 * 
 * PATRÓN DE DISEÑO: Strategy
 * 
 * ¿POR QUÉ STRATEGY PATTERN?
 * - Permite definir una familia de algoritmos de descuento
 * - Hace que sean intercambiables en tiempo de ejecución
 * - Elimina condicionales complejas (if/else, switch)
 * - Facilita testing individual de cada estrategia
 * - Cumple con Open/Closed Principle (SOLID)
 * 
 * CASOS DE USO PROFESIONALES:
 * - Sistemas de descuentos en e-commerce
 * - Cálculo de impuestos por región
 * - Algoritmos de pricing dinámico
 * - Métodos de pago diferentes
 * - Estrategias de envío
 * 
 * BENEFICIOS PARA JUNIOR DEVELOPERS:
 * - Código más limpio y mantenible
 * - Fácil agregar nuevos tipos de descuento
 * - Testing simplificado
 * - Separación clara de responsabilidades
 * 
 * @author Equipo Desarrollo
 * @version 2.1
 * @since Fase 2 - Mejoras con Patrones de Diseño
 */
public interface IDescuentoStrategy {
    
    /**
     * Calcula el descuento aplicable a un producto.
     * 
     * @param producto El producto al que se aplicará el descuento
     * @param contexto Información adicional para el cálculo (temporada, usuario, etc.)
     * @return Monto del descuento a aplicar
     */
    BigDecimal calcularDescuento(Producto producto, DescuentoContexto contexto);
    
    /**
     * Determina si esta estrategia es aplicable al producto y contexto dados.
     * 
     * @param producto El producto a evaluar
     * @param contexto El contexto de descuento
     * @return true si la estrategia es aplicable, false en caso contrario
     */
    boolean esAplicable(Producto producto, DescuentoContexto contexto);
    
    /**
     * Obtiene el nombre descriptivo de la estrategia.
     * Útil para logging y debugging.
     * 
     * @return Nombre de la estrategia
     */
    String getNombreEstrategia();
    
    /**
     * Obtiene la prioridad de la estrategia.
     * Las estrategias con mayor prioridad se evalúan primero.
     * 
     * @return Prioridad (mayor número = mayor prioridad)
     */
    default int getPrioridad() {
        return 0;
    }
}