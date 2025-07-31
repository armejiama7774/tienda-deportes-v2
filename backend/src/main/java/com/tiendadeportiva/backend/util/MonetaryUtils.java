package com.tiendadeportiva.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidades para operaciones monetarias.
 * 
 * OBJETIVO DIDÁCTICO:
 * - Centralizar lógica de precisión monetaria
 * - Buenas prácticas financieras
 * - Consistencia en toda la aplicación
 * 
 * PATRÓN APLICADO: Utility Pattern
 * - Métodos estáticos para operaciones comunes
 * - Sin estado (stateless)
 * - Reutilizable en toda la aplicación
 * 
 * @author Equipo Desarrollo
 * @version 2.1.1
 * @since Fase 2 - Mejoras con Patrones de Diseño
 */
public final class MonetaryUtils {
    
    /**
     * CONSTANTES DE PRECISIÓN MONETARIA
     * 
     * ESTÁNDAR PROFESIONAL:
     * - 2 decimales para monedas estándar
     * - HALF_UP es el redondeo bancario recomendado
     */
    public static final int ESCALA_MONETARIA = 2;
    public static final RoundingMode MODO_REDONDEO_MONETARIO = RoundingMode.HALF_UP;
    
    // Constructor privado para evitar instanciación
    private MonetaryUtils() {
        throw new UnsupportedOperationException("Utility class - no debe ser instanciada");
    }
    
    /**
     * Normaliza un BigDecimal a precisión monetaria estándar.
     * 
     * BENEFICIO PARA JUNIORS:
     * - Un solo lugar para cambiar precisión monetaria
     * - Garantiza consistencia en toda la aplicación
     * - Ejemplo de principio DRY (Don't Repeat Yourself)
     * 
     * @param valor El valor a normalizar
     * @return Valor con precisión monetaria estándar (2 decimales)
     */
    public static BigDecimal normalizarPrecisionMonetaria(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        
        return valor.setScale(ESCALA_MONETARIA, MODO_REDONDEO_MONETARIO);
    }
    
    /**
     * Multiplica dos valores y normaliza el resultado a precisión monetaria.
     * 
     * CASO DE USO: Cálculo de descuentos, impuestos, etc.
     * 
     * @param base Valor base
     * @param multiplicador Multiplicador (ejemplo: porcentaje de descuento)
     * @return Resultado con precisión monetaria normalizada
     */
    public static BigDecimal multiplicarYNormalizar(BigDecimal base, BigDecimal multiplicador) {
        if (base == null || multiplicador == null) {
            return BigDecimal.ZERO;
        }
        
        return base.multiply(multiplicador).setScale(ESCALA_MONETARIA, MODO_REDONDEO_MONETARIO);
    }
    
    /**
     * Suma valores monetarios y normaliza el resultado.
     * 
     * @param valores Valores a sumar
     * @return Suma normalizada a precisión monetaria
     */
    public static BigDecimal sumarYNormalizar(BigDecimal... valores) {
        BigDecimal suma = BigDecimal.ZERO;
        
        for (BigDecimal valor : valores) {
            if (valor != null) {
                suma = suma.add(valor);
            }
        }
        
        return normalizarPrecisionMonetaria(suma);
    }
    
    /**
     * Verifica si un valor monetario es positivo.
     * 
     * @param valor Valor a verificar
     * @return true si es mayor que cero, false en caso contrario
     */
    public static boolean esPositivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Verifica si un valor monetario es cero.
     * 
     * @param valor Valor a verificar
     * @return true si es igual a cero, false en caso contrario
     */
    public static boolean esCero(BigDecimal valor) {
        return valor == null || valor.compareTo(BigDecimal.ZERO) == 0;
    }
}