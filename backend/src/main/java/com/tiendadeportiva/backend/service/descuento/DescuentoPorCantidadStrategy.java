package com.tiendadeportiva.backend.service.descuento;

import com.tiendadeportiva.backend.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Estrategia de descuento por cantidad (volumen de compra).
 * 
 * FASE 2: ARQUITECTURA HEXAGONAL - STRATEGY PATTERN
 * 
 * PATRÓN DE DISEÑO: Strategy Pattern Implementation
 * - Encapsula algoritmo de descuento escalonado por cantidad
 * - Lógica de negocio independiente y testeable
 * - Configuración escalable para diferentes umbrales
 * - Preparado para reglas dinámicas por usuario o temporada
 * 
 * CASOS DE USO:
 * - Descuentos por volumen para mayoristas
 * - Incentivos para compras grandes
 * - Promociones "compra más, ahorra más"
 * - Descuentos especiales para clientes VIP
 * 
 * @author Equipo Desarrollo
 * @version 2.0
 * @since Fase 2 - Strategy Pattern Implementation
 */
@Component
public class DescuentoPorCantidadStrategy implements IDescuentoStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(DescuentoPorCantidadStrategy.class);
    
    // =============================================
    // CONFIGURACIÓN DE DESCUENTOS ESCALONADOS
    // =============================================
    
    /**
     * Configuración de descuentos escalonados por cantidad.
     * 
     * TreeMap para ordenamiento automático por cantidad mínima.
     * Clave: Cantidad mínima
     * Valor: Porcentaje de descuento (0.05 = 5%)
     * 
     * EVOLUCIÓN FUTURA:
     * - Configuración por tipo de usuario (VIP, mayorista, etc.)
     * - Umbrales dinámicos según temporada
     * - Configuración externa (properties, base de datos)
     */
    private static final NavigableMap<Integer, BigDecimal> ESCALONES_DESCUENTO = new TreeMap<>();
    
    static {
        // Configuración de escalones de descuento
        ESCALONES_DESCUENTO.put(3, new BigDecimal("0.05"));   // 3+ items: 5%
        ESCALONES_DESCUENTO.put(5, new BigDecimal("0.10"));   // 5+ items: 10%
        ESCALONES_DESCUENTO.put(10, new BigDecimal("0.15"));  // 10+ items: 15%
        ESCALONES_DESCUENTO.put(20, new BigDecimal("0.20"));  // 20+ items: 20%
        ESCALONES_DESCUENTO.put(50, new BigDecimal("0.25"));  // 50+ items: 25%
    }
    
    /**
     * Prioridad de esta estrategia.
     * Valores más bajos = mayor prioridad
     */
    private static final int PRIORIDAD = 5;  // Mayor prioridad que categoría
    
    @Override
    public BigDecimal calcularDescuento(Producto producto, DescuentoContexto contexto) {
        logger.debug("Calculando descuento por cantidad para producto: {} (contexto: {})", 
                    producto != null ? producto.getNombre() : "null", contexto);
        
        try {
            // Validaciones básicas
            if (producto == null || contexto == null) {
                logger.warn("Producto o contexto nulos, no se aplica descuento por cantidad");
                return BigDecimal.ZERO;
            }
            
            if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Precio inválido para producto {}: {}", producto.getNombre(), producto.getPrecio());
                return BigDecimal.ZERO;
            }
            
            Integer cantidadEnCarrito = contexto.getCantidadEnCarrito();
            if (cantidadEnCarrito == null || cantidadEnCarrito <= 0) {
                logger.debug("Cantidad en carrito inválida o cero: {}", cantidadEnCarrito);
                return BigDecimal.ZERO;
            }
            
            // Encontrar el escalón de descuento aplicable
            BigDecimal porcentajeDescuento = encontrarDescuentoAplicable(cantidadEnCarrito);
            
            if (porcentajeDescuento.compareTo(BigDecimal.ZERO) == 0) {
                logger.debug("No hay descuento por cantidad para {} items", cantidadEnCarrito);
                return BigDecimal.ZERO;
            }
            
            // Aplicar multiplicador especial para usuarios VIP
            BigDecimal porcentajeFinal = aplicarModificadorVIP(porcentajeDescuento, contexto);
            
            // Calcular monto del descuento
            BigDecimal montoDescuento = producto.getPrecio()
                    .multiply(porcentajeFinal)
                    .setScale(2, RoundingMode.HALF_UP);
            
            logger.info("Descuento por cantidad aplicado - Producto: {}, Cantidad: {}, Porcentaje: {}%, Monto: ${}", 
                       producto.getNombre(), cantidadEnCarrito, 
                       porcentajeFinal.multiply(new BigDecimal("100")), montoDescuento);
            
            return montoDescuento;
            
        } catch (Exception e) {
            logger.error("Error calculando descuento por cantidad para producto {}: {}", 
                        producto != null ? producto.getNombre() : "null", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public boolean esAplicable(Producto producto, DescuentoContexto contexto) {
        boolean aplicable = producto != null 
                          && contexto != null 
                          && contexto.getCantidadEnCarrito() != null 
                          && contexto.getCantidadEnCarrito() >= getUmbralMinimo();
        
        logger.debug("Estrategia por cantidad es aplicable: {} para cantidad: {}", 
                    aplicable, contexto != null ? contexto.getCantidadEnCarrito() : "null");
        
        return aplicable;
    }
    
    @Override
    public String getNombreEstrategia() {
        return "Descuento por Cantidad";
    }
    
    @Override
    public int getPrioridad() {
        return PRIORIDAD;
    }
    
    // =============================================
    // LÓGICA DE NEGOCIO ESPECÍFICA
    // =============================================
    
    /**
     * Encuentra el descuento aplicable según la cantidad.
     * 
     * @param cantidad Cantidad de items en el carrito
     * @return Porcentaje de descuento aplicable
     */
    private BigDecimal encontrarDescuentoAplicable(Integer cantidad) {
        // floorEntry devuelve la entrada con la clave más grande <= cantidad
        var entrada = ESCALONES_DESCUENTO.floorEntry(cantidad);
        return entrada != null ? entrada.getValue() : BigDecimal.ZERO;
    }
    
    /**
     * Aplica modificador especial para usuarios VIP.
     * 
     * @param descuentoBase Descuento base calculado
     * @param contexto Contexto de la compra
     * @return Descuento modificado
     */
    private BigDecimal aplicarModificadorVIP(BigDecimal descuentoBase, DescuentoContexto contexto) {
        if (contexto.esUsuarioVip()) {
            // Usuarios VIP obtienen 20% adicional sobre el descuento base
            BigDecimal bonoVip = descuentoBase.multiply(new BigDecimal("0.20"));
            BigDecimal descuentoTotal = descuentoBase.add(bonoVip);
            
            // Limitar descuento máximo al 30%
            BigDecimal limiteMaximo = new BigDecimal("0.30");
            if (descuentoTotal.compareTo(limiteMaximo) > 0) {
                logger.debug("Descuento VIP limitado al máximo 30% (era {}%)", 
                           descuentoTotal.multiply(new BigDecimal("100")));
                return limiteMaximo;
            }
            
            logger.debug("Aplicado bono VIP: {}% base + {}% bono = {}% total", 
                       descuentoBase.multiply(new BigDecimal("100")),
                       bonoVip.multiply(new BigDecimal("100")),
                       descuentoTotal.multiply(new BigDecimal("100")));
            
            return descuentoTotal;
        }
        
        return descuentoBase;
    }
    
    // =============================================
    // MÉTODOS DE UTILIDAD Y CONFIGURACIÓN
    // =============================================
    
    /**
     * Obtiene el umbral mínimo para aplicar descuento.
     * 
     * @return Cantidad mínima de items
     */
    public Integer getUmbralMinimo() {
        return ESCALONES_DESCUENTO.firstKey();
    }
    
    /**
     * Obtiene el umbral máximo configurado.
     * 
     * @return Cantidad máxima configurada
     */
    public Integer getUmbralMaximo() {
        return ESCALONES_DESCUENTO.lastKey();
    }
    
    /**
     * Obtiene el descuento máximo posible.
     * 
     * @return Porcentaje máximo de descuento
     */
    public BigDecimal getDescuentoMaximo() {
        return ESCALONES_DESCUENTO.lastEntry().getValue();
    }
    
    /**
     * Verifica si una cantidad específica califica para descuento.
     * 
     * @param cantidad Cantidad a verificar
     * @return true si califica, false si no
     */
    public boolean calificaParaDescuento(Integer cantidad) {
        return cantidad != null && cantidad >= getUmbralMinimo();
    }
    
    /**
     * Obtiene información del próximo escalón de descuento.
     * 
     * @param cantidadActual Cantidad actual en carrito
     * @return Información del siguiente escalón o null si está en el máximo
     */
    public EscalonInfo getProximoEscalon(Integer cantidadActual) {
        if (cantidadActual == null) return null;
        
        // Buscar el siguiente escalón
        var siguienteEscalon = ESCALONES_DESCUENTO.higherEntry(cantidadActual);
        if (siguienteEscalon == null) {
            return null; // Ya está en el escalón máximo
        }
        
        Integer cantidadFaltante = siguienteEscalon.getKey() - cantidadActual;
        return new EscalonInfo(siguienteEscalon.getKey(), siguienteEscalon.getValue(), cantidadFaltante);
    }
    
    /**
     * Genera información detallada de la estrategia para logging.
     */
    public String generarInformacionEstrategia() {
        StringBuilder info = new StringBuilder();
        info.append("DescuentoPorCantidadStrategy {\n");
        info.append("  Nombre: ").append(getNombreEstrategia()).append("\n");
        info.append("  Prioridad: ").append(getPrioridad()).append("\n");
        info.append("  Escalones configurados: ").append(ESCALONES_DESCUENTO.size()).append("\n");
        info.append("  Umbral mínimo: ").append(getUmbralMinimo()).append(" items\n");
        
        // Formatear descuento máximo sin decimales si es número entero
        BigDecimal descuentoMaximoPorc = getDescuentoMaximo().multiply(new BigDecimal("100"));
        String descuentoMaximoStr = descuentoMaximoPorc.stripTrailingZeros().toPlainString();
        info.append("  Descuento máximo: ").append(descuentoMaximoStr).append("%\n");
        
        info.append("  Escalones:\n");
        ESCALONES_DESCUENTO.forEach((cantidad, descuento) -> {
            BigDecimal porcentaje = descuento.multiply(new BigDecimal("100"));
            String porcentajeStr = porcentaje.stripTrailingZeros().toPlainString();
            info.append("    ").append(cantidad).append("+ items: ")
                .append(porcentajeStr).append("%\n");
        });
        
        info.append("}");
        return info.toString();
    }
    
    // =============================================
    // CLASES DE APOYO
    // =============================================
    
    /**
     * Información sobre un escalón de descuento.
     */
    public static class EscalonInfo {
        private final Integer cantidadRequerida;
        private final BigDecimal porcentajeDescuento;
        private final Integer cantidadFaltante;
        
        public EscalonInfo(Integer cantidadRequerida, BigDecimal porcentajeDescuento, Integer cantidadFaltante) {
            this.cantidadRequerida = cantidadRequerida;
            this.porcentajeDescuento = porcentajeDescuento;
            this.cantidadFaltante = cantidadFaltante;
        }
        
        public Integer getCantidadRequerida() { return cantidadRequerida; }
        public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
        public Integer getCantidadFaltante() { return cantidadFaltante; }
        
        @Override
        public String toString() {
            BigDecimal porcentaje = porcentajeDescuento.multiply(new BigDecimal("100"));
            String porcentajeStr = porcentaje.stripTrailingZeros().toPlainString();
            return String.format("EscalonInfo{cantidad=%d, descuento=%s%%, faltan=%d}", 
                               cantidadRequerida, 
                               porcentajeStr, 
                               cantidadFaltante);
        }
    }
}
