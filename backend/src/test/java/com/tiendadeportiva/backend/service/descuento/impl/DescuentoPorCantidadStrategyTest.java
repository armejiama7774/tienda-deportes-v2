package com.tiendadeportiva.backend.service.descuento.impl;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.descuento.DescuentoContexto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para DescuentoPorCantidadStrategy.
 * 
 * CORRECCIÓN APLICADA - Fase 2: Arquitectura Hexagonal:
 * - Tests actualizados para BigDecimal con precisión monetaria
 * - Validación de formato de porcentajes corregida
 * - Casos edge mejorados para valores monetarios
 * 
 * OBJETIVO DIDÁCTICO:
 * - Testing de lógica escalonada
 * - Validación de rangos y NavigableMap
 * - Casos edge de cantidad cero/nula
 * - Precisión monetaria en assertions
 * 
 * @author Equipo Desarrollo
 * @version 2.1.1
 */
@DisplayName("DescuentoPorCantidadStrategy - Strategy Pattern Tests")
class DescuentoPorCantidadStrategyTest {
    
    private static final Logger logger = LoggerFactory.getLogger(DescuentoPorCantidadStrategyTest.class);
    
    private DescuentoPorCantidadStrategy strategy;
    private Producto producto;
    
    @BeforeEach
    void setUp() {
        strategy = new DescuentoPorCantidadStrategy();
        
        producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setCategoria("Test");
    }
    
    @ParameterizedTest
    @CsvSource({
        "1, 0.00",    // Sin descuento
        "2, 0.00",    // Sin descuento
        "3, 5.00",    // 5% descuento
        "5, 5.00",    // 5% descuento
        "6, 10.00",   // 10% descuento
        "10, 10.00",  // 10% descuento
        "11, 15.00",  // 15% descuento
        "20, 15.00"   // 15% descuento
    })
    @DisplayName("Debe calcular descuentos escalonados correctamente")
    void debeCalcularDescuentosEscalonadosCorrectamente(
            Integer cantidad, String descuentoEsperadoStr) {
        
        // Arrange
        BigDecimal descuentoEsperado = new BigDecimal(descuentoEsperadoStr);
        DescuentoContexto contexto = DescuentoContexto.builder()
                .conCantidadEnCarrito(cantidad)
                .build();
        
        // Act
        BigDecimal descuento = strategy.calcularDescuento(producto, contexto);
        
        // Assert
        // ✅ CORRECCIÓN: Comparar BigDecimal correctamente
        assertThat(descuento).isEqualByComparingTo(descuentoEsperado);
        // ✅ CORRECCIÓN: Verificar que tiene precisión monetaria (2 decimales)
        assertThat(descuento.scale()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Debe manejar cantidad nula o cero")
    void debeManejarCantidadNulaOCero() {
        // Arrange
        DescuentoContexto contextoNulo = DescuentoContexto.builder().build();
        DescuentoContexto contextoCero = DescuentoContexto.builder()
                .conCantidadEnCarrito(0)
                .build();
        DescuentoContexto contextoNegativo = DescuentoContexto.builder()
                .conCantidadEnCarrito(-1)
                .build();
        
        BigDecimal ceroMonetario = new BigDecimal("0.00");
        
        // Act & Assert
        // ✅ CORRECCIÓN: Usar isEqualByComparingTo para BigDecimal
        assertThat(strategy.calcularDescuento(producto, contextoNulo))
                .isEqualByComparingTo(ceroMonetario);
        assertThat(strategy.esAplicable(producto, contextoNulo)).isFalse();
        
        assertThat(strategy.calcularDescuento(producto, contextoCero))
                .isEqualByComparingTo(ceroMonetario);
        assertThat(strategy.esAplicable(producto, contextoCero)).isFalse();
        
        assertThat(strategy.calcularDescuento(producto, contextoNegativo))
                .isEqualByComparingTo(ceroMonetario);
        assertThat(strategy.esAplicable(producto, contextoNegativo)).isFalse();
    }
    
    @Test
    @DisplayName("Debe generar información de rangos correctamente")
    void debeGenerarInformacionDeRangosCorrectamente() {
        // Act
        String info = strategy.obtenerInformacionRangos();
        
        // Assert
        // ✅ CORRECCIÓN: Actualizar expectativas al formato correcto
        assertThat(info).contains("1-2 productos: 0%");
        assertThat(info).contains("3-5 productos: 5%");
        assertThat(info).contains("6-10 productos: 10%");
        assertThat(info).contains("11+ productos: 15%");
        
        // ✅ VERIFICACIÓN ADICIONAL: No debe contener decimales innecesarios
        assertThat(info).doesNotContain("5.00%");
        assertThat(info).doesNotContain("10.00%");
        assertThat(info).doesNotContain("15.00%");
        
        logger.info("📊 Información de rangos generada:\n{}", info);
    }
    
    @Test
    @DisplayName("Debe tener configuración correcta de estrategia")
    void debeTenerConfiguracionCorrectaDeEstrategia() {
        // Assert
        assertThat(strategy.getNombreEstrategia()).isEqualTo("Descuento por Cantidad");
        assertThat(strategy.getPrioridad()).isEqualTo(7); // Prioridad alta
    }
    
    @Test
    @DisplayName("Debe aplicar estrategia solo cuando hay descuento")
    void debeAplicarEstrategiaSoloCuandoHayDescuento() {
        // Arrange
        DescuentoContexto contextoSinDescuento = DescuentoContexto.builder()
                .conCantidadEnCarrito(1) // No tiene descuento
                .build();
        
        DescuentoContexto contextoConDescuento = DescuentoContexto.builder()
                .conCantidadEnCarrito(3) // Tiene 5% descuento
                .build();
        
        // Act & Assert
        assertThat(strategy.esAplicable(producto, contextoSinDescuento)).isFalse();
        assertThat(strategy.esAplicable(producto, contextoConDescuento)).isTrue();
    }
    
    /**
     * Test adicional para verificar casos edge de redondeo.
     * 
     * OBJETIVO DIDÁCTICO: Mostrar importancia de precision monetaria
     */
    @Test
    @DisplayName("Debe manejar redondeo monetario correctamente")
    void debeManejarRedondeoMonetarioCorrectamente() {
        // Arrange - Precio que resultará en redondeo
        Producto productoConRedondeo = new Producto();
        productoConRedondeo.setNombre("Producto Redondeo");
        productoConRedondeo.setPrecio(new BigDecimal("33.33")); // 5% = 1.6665
        productoConRedondeo.setCategoria("Test");
        
        DescuentoContexto contexto = DescuentoContexto.builder()
                .conCantidadEnCarrito(3) // 5% descuento
                .build();
        
        // Act
        BigDecimal descuento = strategy.calcularDescuento(productoConRedondeo, contexto);
        
        // Assert - Debe redondear a 1.67 (HALF_UP)
        assertThat(descuento).isEqualByComparingTo(new BigDecimal("1.67"));
        assertThat(descuento.scale()).isEqualTo(2);
    }
}