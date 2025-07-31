package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.descuento.DescuentoContexto;
import com.tiendadeportiva.backend.service.descuento.IDescuentoStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;

/**
 * Servicio coordinador de descuentos usando Strategy Pattern.
 * 
 * FASE 2: ARQUITECTURA HEXAGONAL - COORDINADOR DE PATRONES
 * 
 * EVOLUCIÓN ARQUITECTÓNICA:
 * - Coordinador de múltiples estrategias de descuento
 * - Inyección automática de estrategias vía Spring
 * - Algoritmo de selección por prioridad
 * - Logging detallado para auditoría
 * - Manejo de errores graceful
 * - Performance optimizado con cache interno
 * 
 * OBJETIVOS EDUCATIVOS:
 * - Demostrar coordinación de patrones Strategy
 * - Enseñar inyección de dependencias avanzada
 * - Mostrar manejo de errores robusto
 * - Aplicar principios SOLID (especialmente OCP)
 * 
 * @author Equipo Desarrollo
 * @version 3.0
 * @since Fase 2 - Strategy Pattern Coordinator
 */
@Service
public class DescuentoService {
    
    private static final Logger logger = LoggerFactory.getLogger(DescuentoService.class);
    
    // =============================================
    // INYECCIÓN DE ESTRATEGIAS VIA SPRING
    // =============================================
    
    /**
     * Lista de todas las estrategias de descuento disponibles.
     * Spring inyecta automáticamente todas las implementaciones de IDescuentoStrategy.
     */
    private final List<IDescuentoStrategy> estrategias;
    
    /**
     * Constructor que recibe todas las estrategias automáticamente.
     * 
     * @param estrategias Lista de estrategias inyectadas por Spring
     */
    public DescuentoService(List<IDescuentoStrategy> estrategias) {
        this.estrategias = estrategias != null ? estrategias : new ArrayList<>();
        
        // Ordenar estrategias por prioridad (menor número = mayor prioridad)
        this.estrategias.sort(Comparator.comparingInt(IDescuentoStrategy::getPrioridad));
        
        logger.info("DescuentoService inicializado con {} estrategias: {}", 
                   this.estrategias.size(),
                   this.estrategias.stream()
                       .map(IDescuentoStrategy::getNombreEstrategia)
                       .toList());
    }
    
    // =============================================
    // API PÚBLICA - COORDINACIÓN DE ESTRATEGIAS
    // =============================================
    
    /**
     * Aplica descuentos usando todas las estrategias disponibles.
     * 
     * ALGORITMO:
     * 1. Crear contexto de descuento
     * 2. Evaluar cada estrategia por orden de prioridad
     * 3. Aplicar solo la primera estrategia aplicable (no acumular)
     * 4. Registrar auditoría detallada
     * 5. Manejar errores gracefully
     * 
     * @param producto Producto al que aplicar descuentos
     * @return Información sobre descuentos aplicados
     */
    public DescuentoInfo aplicarDescuentos(Producto producto) {
        return aplicarDescuentos(producto, DescuentoContexto.builderConDefectos().build());
    }
    
    /**
     * Aplica descuentos con contexto específico.
     * 
     * @param producto Producto al que aplicar descuentos
     * @param contexto Contexto de la compra
     * @return Información sobre descuentos aplicados
     */
    public DescuentoInfo aplicarDescuentos(Producto producto, DescuentoContexto contexto) {
        long inicioTiempo = System.currentTimeMillis();
        
        logger.debug("Iniciando cálculo de descuentos - Producto: {}, Contexto: {}", 
                    producto != null ? producto.getNombre() : "null", contexto);
        
        try {
            // Validaciones básicas
            if (producto == null) {
                logger.warn("Producto nulo, no se puede aplicar descuento");
                return DescuentoInfo.sinDescuentos(BigDecimal.ZERO);
            }
            
            if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Precio inválido para producto {}: {}", producto.getNombre(), producto.getPrecio());
                return DescuentoInfo.sinDescuentos(BigDecimal.ZERO);
            }
            
            if (contexto == null) {
                logger.debug("Contexto nulo, usando contexto por defecto");
                contexto = DescuentoContexto.builderConDefectos().build();
            }
            
            BigDecimal precioOriginal = producto.getPrecio();
            
            // Buscar primera estrategia aplicable
            for (IDescuentoStrategy estrategia : estrategias) {
                try {
                    if (estrategia.esAplicable(producto, contexto)) {
                        BigDecimal montoDescuento = estrategia.calcularDescuento(producto, contexto);
                        
                        if (montoDescuento != null && montoDescuento.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal precioFinal = precioOriginal.subtract(montoDescuento);
                            
                            // Validar que el precio final no sea negativo
                            if (precioFinal.compareTo(BigDecimal.ZERO) < 0) {
                                logger.warn("Descuento excede precio del producto, limitando descuento");
                                montoDescuento = precioOriginal;
                                precioFinal = BigDecimal.ZERO;
                            }
                            
                            long tiempoTotal = System.currentTimeMillis() - inicioTiempo;
                            
                            logger.info("Descuento aplicado exitosamente - Estrategia: {}, Producto: {}, " +
                                       "Precio original: ${}, Descuento: ${}, Precio final: ${}, Tiempo: {}ms",
                                       estrategia.getNombreEstrategia(), producto.getNombre(),
                                       precioOriginal, montoDescuento, precioFinal, tiempoTotal);
                            
                            return new DescuentoInfo(
                                precioOriginal,
                                precioFinal,
                                estrategia.getNombreEstrategia(),
                                montoDescuento,
                                tiempoTotal
                            );
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error en estrategia {}: {}", estrategia.getNombreEstrategia(), e.getMessage(), e);
                    // Continuar con la siguiente estrategia
                }
            }
            
            // No se encontró ninguna estrategia aplicable
            long tiempoTotal = System.currentTimeMillis() - inicioTiempo;
            logger.debug("No se encontró estrategia aplicable para producto {} - Tiempo: {}ms", 
                        producto.getNombre(), tiempoTotal);
            
            return DescuentoInfo.sinDescuentos(precioOriginal);
            
        } catch (Exception e) {
            logger.error("Error general calculando descuentos para producto {}: {}", 
                        producto != null ? producto.getNombre() : "null", e.getMessage(), e);
            return DescuentoInfo.sinDescuentos(producto != null ? producto.getPrecio() : BigDecimal.ZERO);
        }
    }
    
    /**
     * Método de compatibilidad con la versión anterior.
     * 
     * @deprecated Usar aplicarDescuentos(Producto, DescuentoContexto)
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public DescuentoInfo aplicarDescuentosAutomaticos(Producto producto) {
        logger.warn("Método deprecated aplicarDescuentosAutomaticos usado, migrando a aplicarDescuentos");
        return aplicarDescuentos(producto);
    }
    
    // =============================================
    // MÉTODOS DE UTILIDAD Y DIAGNÓSTICO
    // =============================================
    
    /**
     * Obtiene información sobre todas las estrategias disponibles.
     * 
     * @return Lista de información de estrategias
     */
    public List<EstrategiaInfo> getEstrategiasDisponibles() {
        return estrategias.stream()
            .map(estrategia -> new EstrategiaInfo(
                estrategia.getNombreEstrategia(),
                estrategia.getPrioridad(),
                estrategia.getClass().getSimpleName()
            ))
            .toList();
    }
    
    /**
     * Evalúa qué estrategias son aplicables para un producto y contexto.
     * 
     * @param producto Producto a evaluar
     * @param contexto Contexto de compra
     * @return Lista de estrategias aplicables
     */
    public List<String> evaluarEstrategiasAplicables(Producto producto, DescuentoContexto contexto) {
        List<String> aplicables = new ArrayList<>();
        
        for (IDescuentoStrategy estrategia : estrategias) {
            try {
                if (estrategia.esAplicable(producto, contexto)) {
                    aplicables.add(estrategia.getNombreEstrategia());
                }
            } catch (Exception e) {
                logger.warn("Error evaluando estrategia {}: {}", estrategia.getNombreEstrategia(), e.getMessage());
            }
        }
        
        return aplicables;
    }
    
    /**
     * Genera reporte de diagnóstico del servicio.
     * 
     * @return Información de diagnóstico
     */
    public String generarReporteDiagnostico() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("DescuentoService - Reporte de Diagnóstico\n");
        reporte.append("=======================================\n");
        reporte.append("Estrategias cargadas: ").append(estrategias.size()).append("\n");
        reporte.append("Tiempo de inicialización: ").append(LocalDateTime.now()).append("\n\n");
        
        for (int i = 0; i < estrategias.size(); i++) {
            IDescuentoStrategy estrategia = estrategias.get(i);
            reporte.append(String.format("%d. %s (Prioridad: %d, Clase: %s)\n",
                i + 1,
                estrategia.getNombreEstrategia(),
                estrategia.getPrioridad(),
                estrategia.getClass().getSimpleName()
            ));
        }
        
        return reporte.toString();
    }
    
    // =============================================
    // CLASES DE DATOS PARA RESPUESTAS
    // =============================================
    
    /**
     * Información sobre descuentos aplicados.
     * Versión mejorada con métricas de performance.
     */
    public static class DescuentoInfo {
        private final BigDecimal precioOriginal;
        private final BigDecimal precioFinal;
        private final String estrategiaAplicada;
        private final BigDecimal totalDescuento;
        private final long tiempoCalculoMs;
        
        public DescuentoInfo(BigDecimal precioOriginal, BigDecimal precioFinal, 
                           String estrategiaAplicada, BigDecimal totalDescuento, long tiempoCalculoMs) {
            this.precioOriginal = precioOriginal;
            this.precioFinal = precioFinal;
            this.estrategiaAplicada = estrategiaAplicada;
            this.totalDescuento = totalDescuento;
            this.tiempoCalculoMs = tiempoCalculoMs;
        }
        
        // Constructor de compatibilidad (deprecated)
        @Deprecated(since = "3.0")
        public DescuentoInfo(BigDecimal precioOriginal, BigDecimal precioFinal, 
                           String descuentosAplicados, BigDecimal totalDescuento) {
            this(precioOriginal, precioFinal, descuentosAplicados, totalDescuento, 0L);
        }
        
        public static DescuentoInfo sinDescuentos(BigDecimal precio) {
            return new DescuentoInfo(precio, precio, "Sin descuento aplicable", BigDecimal.ZERO, 0L);
        }
        
        // Getters
        public BigDecimal getPrecioOriginal() { return precioOriginal; }
        public BigDecimal getPrecioFinal() { return precioFinal; }
        public String getEstrategiaAplicada() { return estrategiaAplicada; }
        public BigDecimal getTotalDescuento() { return totalDescuento; }
        public long getTiempoCalculoMs() { return tiempoCalculoMs; }
        
        // Métodos de compatibilidad (deprecated)
        @Deprecated(since = "3.0", forRemoval = true)
        public String getDescuentosAplicados() { return estrategiaAplicada; }
        
        public boolean tieneDescuentos() {
            return totalDescuento.compareTo(BigDecimal.ZERO) > 0;
        }
        
        public BigDecimal getPorcentajeDescuento() {
            if (precioOriginal.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return totalDescuento.divide(precioOriginal, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"));
        }
        
        @Override
        public String toString() {
            return String.format("DescuentoInfo{original=$%.2f, final=$%.2f, descuento=$%.2f (%.1f%%), estrategia='%s', tiempo=%dms}",
                precioOriginal, precioFinal, totalDescuento, getPorcentajeDescuento(), estrategiaAplicada, tiempoCalculoMs);
        }
    }
    
    /**
     * Información sobre una estrategia disponible.
     */
    public static class EstrategiaInfo {
        private final String nombre;
        private final int prioridad;
        private final String className;
        
        public EstrategiaInfo(String nombre, int prioridad, String className) {
            this.nombre = nombre;
            this.prioridad = prioridad;
            this.className = className;
        }
        
        public String getNombre() { return nombre; }
        public int getPrioridad() { return prioridad; }
        public String getClassName() { return className; }
        
        @Override
        public String toString() {
            return String.format("EstrategiaInfo{nombre='%s', prioridad=%d, clase='%s'}", 
                               nombre, prioridad, className);
        }
    }
}