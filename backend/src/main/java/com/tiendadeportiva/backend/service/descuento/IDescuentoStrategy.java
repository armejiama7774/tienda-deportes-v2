package com.tiendadeportiva.backend.service.descuento;

import com.tiendadeportiva.backend.model.Producto;
import java.math.BigDecimal;

/**
 * Interface para estrategias de descuento.
 * 
 * FASE 2: ARQUITECTURA HEXAGONAL - STRATEGY PATTERN
 * 
 * PATRÓN DE DISEÑO: Strategy Pattern
 * - Define el contrato común para todas las estrategias de descuento
 * - Permite intercambiar algoritmos de descuento dinámicamente
 * - Facilita la extensión sin modificar código existente (Open/Closed Principle)
 * 
 * CASOS DE USO PROFESIONALES:
 * - Descuentos por temporada (Black Friday, rebajas de verano)
 * - Descuentos por volumen de compra
 * - Descuentos por tipo de cliente (VIP, estudiante, empleado)
 * - Descuentos por categoría de producto
 * 
 * BENEFICIOS PARA JUNIORS:
 * - Ejemplo claro de Strategy Pattern en acción
 * - Separación de responsabilidades (cada descuento es independiente)
 * - Fácil testing (cada estrategia se prueba por separado)
 * - Extensibilidad sin romper código existente
 * 
 * @author Equipo Desarrollo
 * @version 2.1.0
 * @since Fase 2 - Mejoras con Patrones de Diseño
 */
public interface IDescuentoStrategy {
    
    /**
     * Calcula el descuento aplicable a un producto.
     * 
     * IMPORTANTE: 
     * - Retorna BigDecimal.ZERO si no aplica descuento
     * - El valor retornado es el MONTO del descuento, no el precio final
     * - Usar MonetaryUtils para operaciones monetarias precisas
     * 
     * @param producto El producto al que se aplicará el descuento
     * @param cantidad Cantidad de productos (para descuentos por volumen)
     * @return Monto del descuento a aplicar (cero si no aplica)
     */
    BigDecimal calcularDescuento(Producto producto, Integer cantidad);
    
    /**
     * Verifica si esta estrategia es aplicable al producto dado.
     * 
     * PROPÓSITO: Optimización - evitar cálculos innecesarios
     * Solo se calculará el descuento si esta validación retorna true.
     * 
     * @param producto El producto a evaluar
     * @param cantidad Cantidad de productos
     * @return true si la estrategia puede aplicar descuento, false si no
     */
    boolean esAplicable(Producto producto, Integer cantidad);
    
    /**
     * Obtiene el nombre descriptivo de la estrategia.
     * Útil para logging, auditoría y mostrar al usuario qué descuento se aplicó.
     * 
     * @return Nombre legible de la estrategia (ej: "Descuento por Temporada")
     */
    String getNombreEstrategia();
    
    /**
     * Obtiene la prioridad de la estrategia.
     * Usado cuando múltiples estrategias son aplicables.
     * 
     * CONVENCIÓN: Mayor número = mayor prioridad
     * - 1-3: Descuentos básicos (categoría, stock)
     * - 4-6: Descuentos estacionales
     * - 7-9: Descuentos por volumen
     * - 10+: Descuentos especiales (VIP, empleados)
     * 
     * @return Prioridad numérica de la estrategia
     */
    int getPrioridad();
}