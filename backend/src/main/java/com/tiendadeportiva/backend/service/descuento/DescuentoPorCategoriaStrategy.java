package com.tiendadeportiva.backend.service.descuento;

import com.tiendadeportiva.backend.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Estrategia de descuento por categoría de producto.
 * 
 * FASE 2: ARQUITECTURA HEXAGONAL - STRATEGY PATTERN
 * 
 * PATRÓN DE DISEÑO: Strategy Pattern Implementation
 * - Encapsula el algoritmo de descuento por categoría
 * - Configurable externamente sin cambiar código
 * - Fácil de testear independientemente
 * - Preparado para configuración dinámica (base de datos, properties)
 * 
 * CASOS DE USO:
 * - Descuentos estacionales por categoría
 * - Promociones específicas de líneas de producto
 * - Liquidación de inventario por categoría
 * 
 * @author Equipo Desarrollo
 * @version 2.0
 * @since Fase 2 - Strategy Pattern Implementation
 */
@Component
public class DescuentoPorCategoriaStrategy implements IDescuentoStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(DescuentoPorCategoriaStrategy.class);
    
    // =============================================
    // CONFIGURACIÓN DE DESCUENTOS POR CATEGORÍA
    // =============================================
    
    /**
     * Mapa de descuentos por categoría.
     * 
     * EVOLUCIÓN FUTURA: Esto se moverá a:
     * - Base de datos (configuración dinámica)
     * - Properties externas (Spring @ConfigurationProperties)
     * - Microservicio de configuración (Spring Cloud Config)
     */
    private static final Map<String, BigDecimal> DESCUENTOS_POR_CATEGORIA = Map.of(
        "Zapatos", new BigDecimal("0.10"),        // 10% descuento
        "Camisetas", new BigDecimal("0.05"),      // 5% descuento
        "Pantalones", new BigDecimal("0.08"),     // 8% descuento
        "Accesorios", new BigDecimal("0.15"),     // 15% descuento
        "Equipamiento", new BigDecimal("0.12")    // 12% descuento
    );
    
    /**
     * Prioridad de esta estrategia.
     * Valores más bajos = mayor prioridad
     */
    private static final int PRIORIDAD = 10;
    
    @Override
    public BigDecimal calcularDescuento(Producto producto, DescuentoContexto contexto) {
        logger.debug("Calculando descuento por categoría para producto: {} (categoría: {})", 
                    producto != null ? producto.getNombre() : "null", 
                    producto != null ? producto.getCategoria() : "null");
        
        try {
            // Validaciones básicas
            if (producto == null || producto.getCategoria() == null) {
                logger.warn("Producto o categoría nulos, no se aplica descuento");
                return BigDecimal.ZERO;
            }
            
            if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Precio inválido para producto {}: {}", producto.getNombre(), producto.getPrecio());
                return BigDecimal.ZERO;
            }
            
            // Obtener descuento por categoría
            String categoria = producto.getCategoria().trim();
            BigDecimal porcentajeDescuento = DESCUENTOS_POR_CATEGORIA.get(categoria);
            
            if (porcentajeDescuento == null) {
                logger.debug("No hay descuento configurado para categoría: {}", categoria);
                return BigDecimal.ZERO;
            }
            
            // Calcular monto del descuento
            BigDecimal montoDescuento = producto.getPrecio()
                    .multiply(porcentajeDescuento)
                    .setScale(2, RoundingMode.HALF_UP);
            
            logger.info("Descuento por categoría aplicado - Producto: {}, Categoría: {}, Porcentaje: {}%, Monto: ${}", 
                       producto.getNombre(), categoria, 
                       porcentajeDescuento.multiply(new BigDecimal("100")), montoDescuento);
            
            return montoDescuento;
            
        } catch (Exception e) {
            logger.error("Error calculando descuento por categoría para producto {}: {}", 
                        producto != null ? producto.getNombre() : "null", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public boolean esAplicable(Producto producto, DescuentoContexto contexto) {
        // Esta estrategia siempre es aplicable si hay categoría válida
        boolean aplicable = producto != null 
                          && producto.getCategoria() != null 
                          && !producto.getCategoria().trim().isEmpty()
                          && DESCUENTOS_POR_CATEGORIA.containsKey(producto.getCategoria().trim());
        
        logger.debug("Estrategia por categoría es aplicable: {} para producto: {} (categoría: {})", 
                    aplicable, producto != null ? producto.getNombre() : "null", 
                    producto != null ? producto.getCategoria() : "null");
        
        return aplicable;
    }
    
    @Override
    public String getNombreEstrategia() {
        return "Descuento por Categoría";
    }
    
    @Override
    public int getPrioridad() {
        return PRIORIDAD;
    }
    
    // =============================================
    // MÉTODOS DE UTILIDAD Y CONFIGURACIÓN
    // =============================================
    
    /**
     * Obtiene el porcentaje de descuento para una categoría específica.
     * 
     * @param categoria Categoría del producto
     * @return Porcentaje de descuento (0.10 = 10%) o null si no existe
     */
    public BigDecimal getPorcentajeDescuentoPorCategoria(String categoria) {
        return DESCUENTOS_POR_CATEGORIA.get(categoria);
    }
    
    /**
     * Verifica si una categoría tiene descuento configurado.
     * 
     * @param categoria Categoría a verificar
     * @return true si hay descuento, false si no
     */
    public boolean tieneDescuentoConfigurdo(String categoria) {
        return categoria != null && DESCUENTOS_POR_CATEGORIA.containsKey(categoria.trim());
    }
    
    /**
     * Obtiene todas las categorías con descuento configurado.
     * 
     * @return Set de categorías con descuento
     */
    public java.util.Set<String> getCategoriasConDescuento() {
        return DESCUENTOS_POR_CATEGORIA.keySet();
    }
    
    /**
     * Genera información detallada de la estrategia para logging.
     */
    public String generarInformacionEstrategia() {
        StringBuilder info = new StringBuilder();
        info.append("DescuentoPorCategoriaStrategy {\n");
        info.append("  Nombre: ").append(getNombreEstrategia()).append("\n");
        info.append("  Prioridad: ").append(getPrioridad()).append("\n");
        info.append("  Categorías configuradas: ").append(DESCUENTOS_POR_CATEGORIA.size()).append("\n");
        
        DESCUENTOS_POR_CATEGORIA.forEach((categoria, descuento) -> {
            BigDecimal porcentaje = descuento.multiply(new BigDecimal("100"));
            // Formatear sin decimales si es número entero
            String porcentajeStr = porcentaje.stripTrailingZeros().toPlainString();
            info.append("    ").append(categoria).append(": ")
                .append(porcentajeStr).append("%\n");
        });
        
        info.append("}");
        return info.toString();
    }
}
