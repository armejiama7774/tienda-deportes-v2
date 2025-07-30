package com.tiendadeportiva.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para MonetaryUtils.
 * 
 * OBJETIVO DIDÁCTICO:
 * - Mostrar testing de utility classes
 * - Validar precisión monetaria
 * - Casos edge con nulls y valores especiales
 * 
 * @author Equipo Desarrollo
 * @version 2.1.1
 */
@DisplayName("MonetaryUtils - Utility Pattern Tests")
class MonetaryUtilsTest {
    
    @ParameterizedTest
    @CsvSource({
        "100.0000, 100.00",
        "50.1234, 50.12",
        "25.567, 25.57",    // Redondeo hacia arriba
        "25.564, 25.56",    // Redondeo hacia abajo
        "0.001, 0.00",
        "0.005, 0.01"       // Redondeo de 0.5
    })
    @DisplayName("Debe normalizar precisión monetaria correctamente")
    void debeNormalizarPrecisionMonetariaCorrectamente(String entrada, String esperado) {
        // Arrange
        BigDecimal valor = new BigDecimal(entrada);
        BigDecimal valorEsperado = new BigDecimal(esperado);
        
        // Act
        BigDecimal resultado = MonetaryUtils.normalizarPrecisionMonetaria(valor);
        
        // Assert
        assertThat(resultado).isEqualTo(valorEsperado);
        assertThat(resultado.scale()).isEqualTo(2); // Verificar escala
    }
    
    @Test
    @DisplayName("Debe manejar valores nulos en normalización")
    void debeManejarValoresNulosEnNormalizacion() {
        // Act
        BigDecimal resultado = MonetaryUtils.normalizarPrecisionMonetaria(null);
        
        // Assert
        assertThat(resultado).isEqualTo(BigDecimal.ZERO);
    }
    
    @ParameterizedTest
    @CsvSource({
        "100.00, 0.15, 15.00",  // 15% de descuento
        "50.00, 0.10, 5.00",    // 10% de descuento
        "80.00, 0.05, 4.00",    // 5% de descuento
        "33.33, 0.20, 6.67"     // Redondeo en multiplicación
    })
    @DisplayName("Debe multiplicar y normalizar correctamente")
    void debeMultiplicarYNormalizarCorrectamente(String base, String multiplicador, String esperado) {
        // Arrange
        BigDecimal valorBase = new BigDecimal(base);
        BigDecimal valorMultiplicador = new BigDecimal(multiplicador);
        BigDecimal valorEsperado = new BigDecimal(esperado);
        
        // Act
        BigDecimal resultado = MonetaryUtils.multiplicarYNormalizar(valorBase, valorMultiplicador);
        
        // Assert
        assertThat(resultado).isEqualTo(valorEsperado);
    }
    
    @Test
    @DisplayName("Debe sumar valores y normalizar resultado")
    void debeSumarValoresYNormalizarResultado() {
        // Arrange
        BigDecimal valor1 = new BigDecimal("10.123");
        BigDecimal valor2 = new BigDecimal("20.456");
        BigDecimal valor3 = new BigDecimal("5.789");
        BigDecimal esperado = new BigDecimal("36.37"); // Suma redondeada
        
        // Act
        BigDecimal resultado = MonetaryUtils.sumarYNormalizar(valor1, valor2, valor3);
        
        // Assert
        assertThat(resultado).isEqualTo(esperado);
    }
    
    @Test
    @DisplayName("Debe verificar valores positivos correctamente")
    void debeVerificarValoresPositivosCorrectamente() {
        // Assert
        assertThat(MonetaryUtils.esPositivo(new BigDecimal("10.00"))).isTrue();
        assertThat(MonetaryUtils.esPositivo(new BigDecimal("0.01"))).isTrue();
        assertThat(MonetaryUtils.esPositivo(BigDecimal.ZERO)).isFalse();
        assertThat(MonetaryUtils.esPositivo(new BigDecimal("-5.00"))).isFalse();
        assertThat(MonetaryUtils.esPositivo(null)).isFalse();
    }
}