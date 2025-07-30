package com.tiendadeportiva.backend.service.descuento.impl;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.descuento.DescuentoContexto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para DescuentoPorTemporadaStrategy.
 * 
 * OBJETIVO DIDÁCTICO:
 * - Mostrar testing de Strategy Pattern
 * - Validar lógica de negocio compleja
 * - Probar casos edge y configuraciones
 * - Usar tests parametrizados para eficiencia
 * 
 * @author Equipo Desarrollo
 * @version 2.1
 */
@DisplayName("DescuentoPorTemporadaStrategy - Strategy Pattern Tests")
class DescuentoPorTemporadaStrategyTest {
    
    private DescuentoPorTemporadaStrategy strategy;
    private Producto camiseta;
    private Producto abrigo;
    private Producto zapatillas;
    
    @BeforeEach
    void setUp() {
        strategy = new DescuentoPorTemporadaStrategy();
        
        // Productos de test para diferentes categorías
        camiseta = new Producto();
        camiseta.setNombre("Camiseta Nike");
        camiseta.setPrecio(new BigDecimal("50.00"));
        camiseta.setCategoria("Camisetas");
        
        abrigo = new Producto();
        abrigo.setNombre("Abrigo Adidas");
        abrigo.setPrecio(new BigDecimal("100.00"));
        abrigo.setCategoria("Abrigos");
        
        zapatillas = new Producto();
        zapatillas.setNombre("Zapatillas Puma");
        zapatillas.setPrecio(new BigDecimal("80.00"));
        zapatillas.setCategoria("Zapatillas");
    }
    
    @ParameterizedTest
    @CsvSource({
        "VERANO, Camisetas, 50.00, 7.50",     // 15% de descuento
        "INVIERNO, Abrigos, 100.00, 20.00",   // 20% de descuento
        "OTOÑO, Camisetas, 50.00, 5.00",      // 10% descuento (todas las categorías)
        "PRIMAVERA, Zapatillas, 80.00, 4.00"  // 5% de descuento
    })
    @DisplayName("Debe calcular descuentos correctos por temporada y categoría")
    void debeCalcularDescuentosCorrectosPorTemporadaYCategoria(
            String temporada, String categoria, BigDecimal precio, BigDecimal descuentoEsperado) {
        
        // Arrange
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        
        DescuentoContexto contexto = DescuentoContexto.builder()
                .conTemporada(temporada)
                .build();
        
        // Act
        BigDecimal descuento = strategy.calcularDescuento(producto, contexto);
        
        // Assert
        assertThat(descuento).isEqualTo(descuentoEsperado);
    }
    
    @Test
    @DisplayName("Debe retornar cero cuando categoría no aplica para temporada")
    void debeRetornarCeroCuandoCategoriaNoAplicaParaTemporada() {
        // Arrange - Camiseta en INVIERNO (no aplica)
        DescuentoContexto contexto = DescuentoContexto.builder()
                .conTemporada("INVIERNO")
                .build();
        
        // Act
        BigDecimal descuento = strategy.calcularDescuento(camiseta, contexto);
        
        // Assert
        assertThat(descuento).isEqualTo(BigDecimal.ZERO);
        assertThat(strategy.esAplicable(camiseta, contexto)).isFalse();
    }
    
    @Test
    @DisplayName("Debe calcular temporada automáticamente basada en fecha")
    void debeCalcularTemporadaAutomaticamenteBasadaEnFecha() {
        // Arrange - Fecha de julio (VERANO)
        LocalDateTime fechaVerano = LocalDateTime.of(2025, 7, 15, 10, 0);
        DescuentoContexto contexto = DescuentoContexto.builder()
                .conFechaCalculo(fechaVerano)
                .build();
        
        // Act
        BigDecimal descuento = strategy.calcularDescuento(camiseta, contexto);
        
        // Assert - Debe aplicar descuento de verano (15%)
        assertThat(descuento).isEqualTo(new BigDecimal("7.50")); // 15% de 50.00
        assertThat(strategy.esAplicable(camiseta, contexto)).isTrue();
    }
    
    @Test
    @DisplayName("Debe manejar temporada nula o inválida")
    void debeManejarTemporadaNulaOInvalida() {
        // Arrange
        DescuentoContexto contextoSinTemporada = DescuentoContexto.builder().build();
        DescuentoContexto contextoTemporadaInvalida = DescuentoContexto.builder()
                .conTemporada("TEMPORADA_INEXISTENTE")
                .build();
        
        // Act & Assert - Sin temporada (debe calcular automáticamente)
        assertThat(strategy.esAplicable(camiseta, contextoSinTemporada)).isTrue();
        
        // Act & Assert - Temporada inválida
        assertThat(strategy.calcularDescuento(camiseta, contextoTemporadaInvalida))
                .isEqualTo(BigDecimal.ZERO);
        assertThat(strategy.esAplicable(camiseta, contextoTemporadaInvalida)).isFalse();
    }
    
    @Test
    @DisplayName("Debe tener información correcta de estrategia")
    void debeTenerInformacionCorrectaDeEstrategia() {
        // Assert
        assertThat(strategy.getNombreEstrategia()).isEqualTo("Descuento por Temporada");
        assertThat(strategy.getPrioridad()).isEqualTo(5);
    }
}