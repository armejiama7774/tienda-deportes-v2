package com.tiendadeportiva.backend.service.descuento.impl;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.descuento.DescuentoContexto;
import com.tiendadeportiva.backend.service.descuento.IDescuentoStrategy;
import com.tiendadeportiva.backend.util.MonetaryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;

/**
 * Estrategia de descuento basada en temporadas.
 * 
 * PATRÓN DE DISEÑO: Strategy Pattern - Implementación Concreta
 * 
 * MEJORA APLICADA - Precisión BigDecimal:
 * - Normalización a 2 decimales para compatibilidad monetaria
 * - RoundingMode.HALF_UP para redondeo estándar
 * - Consistencia en todas las operaciones financieras
 * 
 * CASO DE USO PROFESIONAL:
 * - Descuentos estacionales (liquidación de invierno, ofertas de verano)
 * - Promociones por temporadas altas/bajas
 * - Descuentos dinámicos basados en fechas
 * 
 * BENEFICIOS PARA JUNIORS:
 * - Ejemplo de manejo correcto de BigDecimal en finanzas
 * - Importancia de la precisión monetaria
 * - Buenas prácticas de redondeo
 * 
 * @author Equipo Desarrollo
 * @version 2.1.1
 * @since Fase 2 - Mejoras con Patrones de Diseño
 */
@Component
public class DescuentoPorTemporadaStrategy implements IDescuentoStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(DescuentoPorTemporadaStrategy.class);
    
    /**
     * CONSTANTE DE PRECISIÓN MONETARIA
     * 
     * BUENA PRÁCTICA PROFESIONAL:
     * - Siempre definir precisión monetaria como constante
     * - 2 decimales es estándar para monedas
     * - HALF_UP es el redondeo bancario estándar
     */
    private static final int ESCALA_MONETARIA = 2;
    private static final RoundingMode MODO_REDONDEO = RoundingMode.HALF_UP;
    
    // Configuración de descuentos por temporada
    private static final Map<String, BigDecimal> DESCUENTOS_POR_TEMPORADA = Map.of(
        "VERANO", new BigDecimal("0.15"),    // 15%
        "INVIERNO", new BigDecimal("0.20"),  // 20%
        "OTOÑO", new BigDecimal("0.10"),     // 10%
        "PRIMAVERA", new BigDecimal("0.05")  // 5%
    );
    
    // Categorías aplicables por temporada
    private static final Map<String, String[]> CATEGORIAS_POR_TEMPORADA = Map.of(
        "VERANO", new String[]{"Camisetas", "Shorts", "Trajes de baño"},
        "INVIERNO", new String[]{"Abrigos", "Sudaderas", "Pantalones"},
        "OTOÑO", new String[]{"*"}, // Todas las categorías
        "PRIMAVERA", new String[]{"Zapatillas", "Calzado deportivo"}
    );
    
    @Override
    public BigDecimal calcularDescuento(Producto producto, DescuentoContexto contexto) {
        logger.debug("🌡️ Calculando descuento por temporada para producto: {} en temporada: {}", 
                    producto.getNombre(), contexto.getTemporada());
        
        String temporada = obtenerTemporadaEfectiva(contexto);
        
        if (!esTemporadaValida(temporada)) {
            logger.debug("❌ Temporada no válida: {}", temporada);
            return BigDecimal.ZERO;
        }
        
        if (!esCategoriaAplicable(producto.getCategoria(), temporada)) {
            logger.debug("❌ Categoría '{}' no aplicable para temporada '{}'", 
                        producto.getCategoria(), temporada);
            return BigDecimal.ZERO;
        }
        
        BigDecimal porcentajeDescuento = DESCUENTOS_POR_TEMPORADA.get(temporada);
        
        // ✅ MEJORA: Usar utilidad monetaria
        BigDecimal montoDescuento = MonetaryUtils.multiplicarYNormalizar(
            producto.getPrecio(), 
            porcentajeDescuento
        );
        
        logger.info("✅ Descuento por temporada aplicado: {}% (${}) para producto: {} en temporada: {}", 
                   porcentajeDescuento.multiply(new BigDecimal("100")), 
                   montoDescuento, producto.getNombre(), temporada);
        
        return montoDescuento;
    }
    
    @Override
    public boolean esAplicable(Producto producto, DescuentoContexto contexto) {
        String temporada = obtenerTemporadaEfectiva(contexto);
        boolean aplicable = esTemporadaValida(temporada) && 
                           esCategoriaAplicable(producto.getCategoria(), temporada);
        
        logger.debug("🔍 Evaluando aplicabilidad: producto={}, temporada={}, aplicable={}", 
                    producto.getNombre(), temporada, aplicable);
        
        return aplicable;
    }
    
    @Override
    public String getNombreEstrategia() {
        return "Descuento por Temporada";
    }
    
    @Override
    public int getPrioridad() {
        return 5; // Prioridad media
    }
    
    /**
     * Obtiene la temporada efectiva para el cálculo.
     * Si no se especifica en el contexto, la calcula basada en la fecha actual.
     */
    private String obtenerTemporadaEfectiva(DescuentoContexto contexto) {
        // Si el contexto especifica una temporada, usarla
        if (contexto.getTemporada() != null && !contexto.getTemporada().isBlank()) {
            return contexto.getTemporada().toUpperCase();
        }
        
        // Si no, calcular basada en la fecha
        return calcularTemporadaPorFecha(contexto.getFechaCalculo());
    }
    
    /**
     * Calcula la temporada basada en la fecha.
     */
    private String calcularTemporadaPorFecha(LocalDateTime fecha) {
        Month mes = fecha.getMonth();
        
        return switch (mes) {
            case DECEMBER, JANUARY, FEBRUARY -> "INVIERNO";
            case MARCH, APRIL, MAY -> "PRIMAVERA";
            case JUNE, JULY, AUGUST -> "VERANO";
            case SEPTEMBER, OCTOBER, NOVEMBER -> "OTOÑO";
        };
    }
    
    /**
     * Verifica si la temporada es válida para descuentos.
     */
    private boolean esTemporadaValida(String temporada) {
        return temporada != null && DESCUENTOS_POR_TEMPORADA.containsKey(temporada);
    }
    
    /**
     * Verifica si la categoría del producto aplica para la temporada.
     */
    private boolean esCategoriaAplicable(String categoriaProducto, String temporada) {
        if (categoriaProducto == null || temporada == null) {
            return false;
        }
        
        String[] categoriasAplicables = CATEGORIAS_POR_TEMPORADA.get(temporada);
        if (categoriasAplicables == null) {
            return false;
        }
        
        // Si contiene "*", aplica a todas las categorías
        if (categoriasAplicables.length == 1 && "*".equals(categoriasAplicables[0])) {
            return true;
        }
        
        // Verificar si la categoría está en la lista
        for (String categoria : categoriasAplicables) {
            if (categoria.equalsIgnoreCase(categoriaProducto)) {
                return true;
            }
        }
        
        return false;
    }
}