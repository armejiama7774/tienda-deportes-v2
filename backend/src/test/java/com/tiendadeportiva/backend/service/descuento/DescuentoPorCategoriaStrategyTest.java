package com.tiendadeportiva.backend.service.descuento;

import com.tiendadeportiva.backend.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para DescuentoPorCategoriaStrategy.
 * 
 * OBJETIVO EDUCATIVO:
 * - Demostrar testing de Strategy Pattern
 * - Mostrar casos de uso reales de descuentos
 * - Enseñar testing de casos edge y errores
 * - Validar logging y comportamiento robusto
 * 
 * CASOS DE PRUEBA:
 * 1. Descuentos por categorías válidas
 * 2. Categorías sin descuento configurado
 * 3. Casos edge (nulls, precios inválidos)
 * 4. Validación de aplicabilidad
 * 5. Métodos de utilidad
 * 
 * @author Equipo Desarrollo
 * @version 2.0
 * @since Fase 2 - Strategy Pattern Tests
 */
@DisplayName("DescuentoPorCategoriaStrategy - Tests Unitarios")
class DescuentoPorCategoriaStrategyTest {
    
    private DescuentoPorCategoriaStrategy strategy;
    private DescuentoContexto contextoBasico;
    
    @BeforeEach
    void setUp() {
        strategy = new DescuentoPorCategoriaStrategy();
        
        // Contexto básico para pruebas
        contextoBasico = DescuentoContexto.builder()
                .conTipoUsuario("NORMAL")
                .conCantidadEnCarrito(1)
                .conFechaCalculo(LocalDateTime.now())
                .build();
    }
    
    @Nested
    @DisplayName("Cálculo de Descuentos")
    class CalculoDescuentos {
        
        @Test
        @DisplayName("Debe aplicar 10% descuento para categoría Zapatos")
        void debeAplicarDescuentoZapatos() {
            // Arrange
            Producto zapato = crearProducto("Zapatos Nike", "Zapatos", new BigDecimal("100.00"));
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(zapato, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("10.00"));
        }
        
        @Test
        @DisplayName("Debe aplicar 5% descuento para categoría Camisetas")
        void debeAplicarDescuentoCamisetas() {
            // Arrange
            Producto camiseta = crearProducto("Camiseta Adidas", "Camisetas", new BigDecimal("50.00"));
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(camiseta, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("2.50"));
        }
        
        @Test
        @DisplayName("Debe aplicar 8% descuento para categoría Pantalones")
        void debeAplicarDescuentoPantalones() {
            // Arrange
            Producto pantalon = crearProducto("Pantalón Deportivo", "Pantalones", new BigDecimal("80.00"));
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(pantalon, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("6.40"));
        }
        
        @Test
        @DisplayName("Debe aplicar 15% descuento para categoría Accesorios")
        void debeAplicarDescuentoAccesorios() {
            // Arrange
            Producto accesorio = crearProducto("Gorra Nike", "Accesorios", new BigDecimal("25.00"));
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(accesorio, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("3.75"));
        }
        
        @Test
        @DisplayName("Debe aplicar 12% descuento para categoría Equipamiento")
        void debeAplicarDescuentoEquipamiento() {
            // Arrange
            Producto equipo = crearProducto("Balón de Fútbol", "Equipamiento", new BigDecimal("45.00"));
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(equipo, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("5.40"));
        }
        
        @Test
        @DisplayName("No debe aplicar descuento para categoría no configurada")
        void noDebeAplicarDescuentoCategoriaNoConfigurada() {
            // Arrange
            Producto producto = crearProducto("Producto Especial", "Categoria_No_Existente", new BigDecimal("100.00"));
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(producto, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(BigDecimal.ZERO);
        }
    }
    
    @Nested
    @DisplayName("Casos Edge y Validaciones")
    class CasosEdge {
        
        @Test
        @DisplayName("Debe retornar cero cuando producto es null")
        void debeRetornarCeroCuandoProductoEsNull() {
            // Act
            BigDecimal descuento = strategy.calcularDescuento(null, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe retornar cero cuando categoría es null")
        void debeRetornarCeroCuandoCategoriaEsNull() {
            // Arrange
            Producto producto = crearProducto("Producto Test", null, new BigDecimal("100.00"));
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(producto, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe retornar cero cuando precio es null")
        void debeRetornarCeroCuandoPrecioEsNull() {
            // Arrange
            Producto producto = crearProducto("Producto Test", "Zapatos", null);
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(producto, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe retornar cero cuando precio es cero o negativo")
        void debeRetornarCeroCuandoPrecioEsCeroONegativo() {
            // Arrange
            Producto productoCero = crearProducto("Producto Gratis", "Zapatos", BigDecimal.ZERO);
            Producto productoNegativo = crearProducto("Producto Negativo", "Zapatos", new BigDecimal("-10.00"));
            
            // Act
            BigDecimal descuentoCero = strategy.calcularDescuento(productoCero, contextoBasico);
            BigDecimal descuentoNegativo = strategy.calcularDescuento(productoNegativo, contextoBasico);
            
            // Assert
            assertThat(descuentoCero).isEqualTo(BigDecimal.ZERO);
            assertThat(descuentoNegativo).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe manejar categorías con espacios en blanco")
        void debeManejarCategoriasConEspacios() {
            // Arrange
            Producto producto = crearProducto("Producto Test", "  Zapatos  ", new BigDecimal("100.00"));
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(producto, contextoBasico);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("10.00"));
        }
        
        @Test
        @DisplayName("Debe manejar contexto null gracefully")
        void debeManejarContextoNull() {
            // Arrange
            Producto producto = crearProducto("Producto Test", "Zapatos", new BigDecimal("100.00"));
            
            // Act & Assert - No debe lanzar excepción
            assertThatNoException().isThrownBy(() -> {
                BigDecimal descuento = strategy.calcularDescuento(producto, null);
                assertThat(descuento).isEqualTo(new BigDecimal("10.00"));
            });
        }
    }
    
    @Nested
    @DisplayName("Aplicabilidad de Estrategia")
    class AplicabilidadEstrategia {
        
        @Test
        @DisplayName("Debe ser aplicable para categoría con descuento configurado")
        void debeSerAplicableParaCategoriaConfigurada() {
            // Arrange
            Producto producto = crearProducto("Producto Test", "Zapatos", new BigDecimal("100.00"));
            
            // Act
            boolean aplicable = strategy.esAplicable(producto, contextoBasico);
            
            // Assert
            assertThat(aplicable).isTrue();
        }
        
        @Test
        @DisplayName("No debe ser aplicable para categoría sin descuento configurado")
        void noDebeSerAplicableParaCategoriaNoConfigurada() {
            // Arrange
            Producto producto = crearProducto("Producto Test", "Categoria_Inexistente", new BigDecimal("100.00"));
            
            // Act
            boolean aplicable = strategy.esAplicable(producto, contextoBasico);
            
            // Assert
            assertThat(aplicable).isFalse();
        }
        
        @Test
        @DisplayName("No debe ser aplicable cuando producto es null")
        void noDebeSerAplicableCuandoProductoEsNull() {
            // Act
            boolean aplicable = strategy.esAplicable(null, contextoBasico);
            
            // Assert
            assertThat(aplicable).isFalse();
        }
        
        @Test
        @DisplayName("No debe ser aplicable cuando categoría es null o vacía")
        void noDebeSerAplicableCuandoCategoriaEsNullOVacia() {
            // Arrange
            Producto productoSinCategoria = crearProducto("Producto Test", null, new BigDecimal("100.00"));
            Producto productoCategoriaVacia = crearProducto("Producto Test", "", new BigDecimal("100.00"));
            Producto productoCategoriaEspacios = crearProducto("Producto Test", "   ", new BigDecimal("100.00"));
            
            // Act & Assert
            assertThat(strategy.esAplicable(productoSinCategoria, contextoBasico)).isFalse();
            assertThat(strategy.esAplicable(productoCategoriaVacia, contextoBasico)).isFalse();
            assertThat(strategy.esAplicable(productoCategoriaEspacios, contextoBasico)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Métodos de Utilidad")
    class MetodosUtilidad {
        
        @Test
        @DisplayName("Debe retornar nombre correcto de estrategia")
        void debeRetornarNombreCorrectoEstrategia() {
            // Act & Assert
            assertThat(strategy.getNombreEstrategia()).isEqualTo("Descuento por Categoría");
        }
        
        @Test
        @DisplayName("Debe retornar prioridad correcta")
        void debeRetornarPrioridadCorrecta() {
            // Act & Assert
            assertThat(strategy.getPrioridad()).isEqualTo(10);
        }
        
        @Test
        @DisplayName("Debe retornar porcentaje correcto para categoría")
        void debeRetornarPorcentajeCorrectoPorCategoria() {
            // Act & Assert
            assertThat(strategy.getPorcentajeDescuentoPorCategoria("Zapatos"))
                .isEqualTo(new BigDecimal("0.10"));
            assertThat(strategy.getPorcentajeDescuentoPorCategoria("Camisetas"))
                .isEqualTo(new BigDecimal("0.05"));
            assertThat(strategy.getPorcentajeDescuentoPorCategoria("Categoria_Inexistente"))
                .isNull();
        }
        
        @Test
        @DisplayName("Debe verificar correctamente si categoría tiene descuento configurado")
        void debeVerificarCorrectamenteSiCategoriaConfigureada() {
            // Act & Assert
            assertThat(strategy.tieneDescuentoConfigurdo("Zapatos")).isTrue();
            assertThat(strategy.tieneDescuentoConfigurdo("Categoria_Inexistente")).isFalse();
            assertThat(strategy.tieneDescuentoConfigurdo(null)).isFalse();
        }
        
        @Test
        @DisplayName("Debe retornar todas las categorías con descuento")
        void debeRetornarTodasLasCategoriasConDescuento() {
            // Act
            var categorias = strategy.getCategoriasConDescuento();
            
            // Assert
            assertThat(categorias).containsExactlyInAnyOrder(
                "Zapatos", "Camisetas", "Pantalones", "Accesorios", "Equipamiento"
            );
        }
        
        @Test
        @DisplayName("Debe generar información detallada de estrategia")
        void debeGenerarInformacionDetalladaEstrategia() {
            // Act
            String info = strategy.generarInformacionEstrategia();
            
            // Assert
            assertThat(info)
                .contains("DescuentoPorCategoriaStrategy")
                .contains("Descuento por Categoría")
                .contains("Prioridad: 10")
                .contains("Zapatos: 10%")
                .contains("Camisetas: 5%");
        }
    }
    
    // =============================================
    // MÉTODOS DE UTILIDAD PARA TESTS
    // =============================================
    
    private Producto crearProducto(String nombre, String categoria, BigDecimal precio) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setStockDisponible(10);
        producto.setActivo(true);
        return producto;
    }
}
