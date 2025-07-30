package com.tiendadeportiva.backend.service.descuento.impl;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.descuento.DescuentoContexto;
import com.tiendadeportiva.backend.service.descuento.IDescuentoStrategy;
import com.tiendadeportiva.backend.util.MonetaryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map;

/**
 * Estrategia de descuento basada en cantidad de productos en el carrito.
 * 
 * PATRÓN DE DISEÑO: Strategy Pattern - Implementación Concreta
 * 
 * CORRECCIÓN APLICADA - Fase 2: Arquitectura Hexagonal:
 * - Uso consistente de MonetaryUtils para precisión
 * - BigDecimal.ZERO normalizado a 2 decimales
 * - Formato consistente en información de rangos
 * 
 * CASO DE USO PROFESIONAL:
 * - Descuentos por volumen de compra
 * - Incentivos para aumentar el ticket promedio
 * - Descuentos escalonados por cantidad
 * 
 * BENEFICIOS PARA JUNIORS:
 * - Uso correcto de NavigableMap para rangos eficientes
 * - Lógica escalonada común en e-commerce
 * - Configuración flexible y mantenible
 * - Precisión monetaria consistente
 * 
 * @author Equipo Desarrollo
 * @version 2.1.1
 * @since Fase 2 - Mejoras con Patrones de Diseño
 */
@Component
public class DescuentoPorCantidadStrategy implements IDescuentoStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(DescuentoPorCantidadStrategy.class);
    
    /**
     * Configuración de descuentos escalonados por cantidad.
     * 
     * ESTRUCTURA DE DATOS: NavigableMap para búsquedas eficientes
     * Clave: Cantidad mínima, Valor: Porcentaje de descuento
     */
    private static final NavigableMap<Integer, BigDecimal> DESCUENTOS_POR_CANTIDAD = new TreeMap<>(Map.of(
        1, new BigDecimal("0.00"),            // 1-2 productos: 0%
        3, new BigDecimal("0.05"),            // 3-5 productos: 5%
        6, new BigDecimal("0.10"),            // 6-10 productos: 10%
        11, new BigDecimal("0.15")            // 11+ productos: 15%
    ));
    
    @Override
    public BigDecimal calcularDescuento(Producto producto, DescuentoContexto contexto) {
        Integer cantidadEnCarrito = contexto.getCantidadEnCarrito();
        
        if (cantidadEnCarrito == null || cantidadEnCarrito <= 0) {
            logger.debug("❌ Cantidad en carrito no válida: {}", cantidadEnCarrito);
            // ✅ CORRECCIÓN: Retornar BigDecimal con precisión monetaria
            return MonetaryUtils.normalizarPrecisionMonetaria(BigDecimal.ZERO);
        }
        
        BigDecimal porcentajeDescuento = obtenerPorcentajeDescuento(cantidadEnCarrito);
        
        // ✅ CORRECCIÓN: Comparar usando compareTo para BigDecimal
        if (porcentajeDescuento.compareTo(BigDecimal.ZERO) == 0) {
            logger.debug("📦 Sin descuento por cantidad: {} productos", cantidadEnCarrito);
            // ✅ CORRECCIÓN: Retornar BigDecimal normalizado
            return MonetaryUtils.normalizarPrecisionMonetaria(BigDecimal.ZERO);
        }
        
        // ✅ CORRECCIÓN: Usar MonetaryUtils para operaciones monetarias
        BigDecimal montoDescuento = MonetaryUtils.multiplicarYNormalizar(
            producto.getPrecio(), 
            porcentajeDescuento
        );
        
        logger.info("✅ Descuento por cantidad aplicado: {}% (${}) para {} productos - Producto: {}", 
                   porcentajeDescuento.multiply(new BigDecimal("100")), 
                   montoDescuento, cantidadEnCarrito, producto.getNombre());
        
        return montoDescuento;
    }
    
    @Override
    public boolean esAplicable(Producto producto, DescuentoContexto contexto) {
        Integer cantidadEnCarrito = contexto.getCantidadEnCarrito();
        boolean aplicable = cantidadEnCarrito != null && 
                           cantidadEnCarrito > 0 && 
                           obtenerPorcentajeDescuento(cantidadEnCarrito).compareTo(BigDecimal.ZERO) > 0;
        
        logger.debug("🔍 Evaluando aplicabilidad por cantidad: cantidad={}, aplicable={}", 
                    cantidadEnCarrito, aplicable);
        
        return aplicable;
    }
    
    @Override
    public String getNombreEstrategia() {
        return "Descuento por Cantidad";
    }
    
    @Override
    public int getPrioridad() {
        return 7; // Prioridad alta - incentivo importante para el negocio
    }
    
    /**
     * Obtiene el porcentaje de descuento basado en la cantidad.
     * 
     * ALGORITMO EFICIENTE: Usa NavigableMap.floorEntry() para encontrar
     * el rango apropiado en tiempo O(log n)
     * 
     * BENEFICIO PARA JUNIORS: Ejemplo de uso de estructuras de datos avanzadas
     */
    private BigDecimal obtenerPorcentajeDescuento(Integer cantidad) {
        // floorEntry encuentra la entrada más grande <= cantidad
        var entrada = DESCUENTOS_POR_CANTIDAD.floorEntry(cantidad);
        
        if (entrada == null) {
            logger.debug("🚫 No se encontró descuento para cantidad: {}", cantidad);
            return new BigDecimal("0.00"); // ✅ CORRECCIÓN: Usar formato consistente
        }
        
        BigDecimal porcentaje = entrada.getValue();
        logger.debug("📊 Cantidad: {} -> Descuento: {}%", cantidad, 
                    porcentaje.multiply(new BigDecimal("100")));
        
        return porcentaje;
    }
    
    /**
     * Método de utilidad para obtener información de los rangos configurados.
     * 
     * ✅ CORRECCIÓN: Formato consistente de porcentajes
     * BENEFICIO PARA JUNIORS: Ejemplo de StringBuilder para concatenación eficiente
     */
    public String obtenerInformacionRangos() {
        StringBuilder info = new StringBuilder("Rangos de descuento por cantidad:\n");
        
        Integer cantidadAnterior = null;
        for (var entrada : DESCUENTOS_POR_CANTIDAD.entrySet()) {
            Integer cantidadMinima = entrada.getKey();
            BigDecimal porcentaje = entrada.getValue();
            
            if (cantidadAnterior != null) {
                // ✅ CORRECCIÓN: Formato de porcentaje sin decimales innecesarios
                String porcentajeFormateado = formatearPorcentaje(
                    DESCUENTOS_POR_CANTIDAD.get(cantidadAnterior)
                );
                
                info.append(String.format("- %d-%d productos: %s%%\n", 
                    cantidadAnterior, cantidadMinima - 1, porcentajeFormateado));
            }
            
            cantidadAnterior = cantidadMinima;
        }
        
        // Último rango (cantidadMinima+)
        if (cantidadAnterior != null) {
            String porcentajeFormateado = formatearPorcentaje(
                DESCUENTOS_POR_CANTIDAD.get(cantidadAnterior)
            );
            
            info.append(String.format("- %d+ productos: %s%%\n", 
                cantidadAnterior, porcentajeFormateado));
        }
        
        return info.toString();
    }
    
    /**
     * Formatea un porcentaje BigDecimal para mostrar sin decimales innecesarios.
     * 
     * OBJETIVO DIDÁCTICO:
     * - Mostrar cómo formatear BigDecimal para presentación
     * - Eliminar decimales .00 cuando no son necesarios
     * - Mantener decimales cuando son significativos
     * 
     * EJEMPLOS:
     * - 0.05 -> "5" (no "5.00")
     * - 0.15 -> "15" (no "15.00")
     * - 0.125 -> "12.5" (mantener decimal significativo)
     */
    private String formatearPorcentaje(BigDecimal porcentajeDecimal) {
        BigDecimal porcentaje = porcentajeDecimal.multiply(new BigDecimal("100"));
        
        // Si el porcentaje es un número entero, no mostrar decimales
        if (porcentaje.stripTrailingZeros().scale() <= 0) {
            return porcentaje.setScale(0).toString();
        } else {
            // Si tiene decimales significativos, mostrarlos
            return porcentaje.stripTrailingZeros().toString();
        }
    }
}