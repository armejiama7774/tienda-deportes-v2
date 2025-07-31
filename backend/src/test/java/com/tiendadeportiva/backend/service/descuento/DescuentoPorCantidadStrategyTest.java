package com.tiendadeportiva.backend.service.descuento;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.descuento.DescuentoPorCantidadStrategy.EscalonInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para DescuentoPorCantidadStrategy.
 * 
 * OBJETIVO EDUCATIVO:
 * - Demostrar testing de algoritmos escalonados
 * - Mostrar testing de lógica de negocio compleja (usuarios VIP)
 * - Enseñar validación de casos edge con datos numéricos
 * - Probar funcionalidades de utilidad avanzadas
 * 
 * CASOS DE PRUEBA:
 * 1. Descuentos escalonados por cantidad
 * 2. Lógica especial para usuarios VIP
 * 3. Casos edge (nulls, cantidades inválidas)
 * 4. Validación de aplicabilidad
 * 5. Métodos de utilidad y configuración
 * 6. Información de próximos escalones
 * 
 * @author Equipo Desarrollo
 * @version 2.0
 * @since Fase 2 - Strategy Pattern Tests
 */
@DisplayName("DescuentoPorCantidadStrategy - Tests Unitarios")
class DescuentoPorCantidadStrategyTest {
    
    private DescuentoPorCantidadStrategy strategy;
    private Producto productoTest;
    
    @BeforeEach
    void setUp() {
        strategy = new DescuentoPorCantidadStrategy();
        productoTest = crearProducto("Producto Test", "Zapatos", new BigDecimal("100.00"));
    }
    
    @Nested
    @DisplayName("Descuentos Escalonados")
    class DescuentosEscalonados {
        
        @Test
        @DisplayName("Debe aplicar 5% descuento para 3-4 items")
        void debeAplicar5PorCientoParaTresItems() {
            // Arrange
            DescuentoContexto contexto = crearContexto(3, "NORMAL");
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(productoTest, contexto);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("5.00")); // 5% de 100
        }
        
        @Test
        @DisplayName("Debe aplicar 10% descuento para 5-9 items")
        void debeAplicar10PorCientoParaCincoItems() {
            // Arrange
            DescuentoContexto contexto = crearContexto(5, "NORMAL");
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(productoTest, contexto);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("10.00")); // 10% de 100
        }
        
        @Test
        @DisplayName("Debe aplicar 15% descuento para 10-19 items")
        void debeAplicar15PorCientoParaDiezItems() {
            // Arrange
            DescuentoContexto contexto = crearContexto(10, "NORMAL");
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(productoTest, contexto);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("15.00")); // 15% de 100
        }
        
        @Test
        @DisplayName("Debe aplicar 20% descuento para 20-49 items")
        void debeAplicar20PorCientoParaVeinteItems() {
            // Arrange
            DescuentoContexto contexto = crearContexto(20, "NORMAL");
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(productoTest, contexto);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("20.00")); // 20% de 100
        }
        
        @Test
        @DisplayName("Debe aplicar 25% descuento para 50+ items")
        void debeAplicar25PorCientoParaCincuentaItems() {
            // Arrange
            DescuentoContexto contexto = crearContexto(50, "NORMAL");
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(productoTest, contexto);
            
            // Assert
            assertThat(descuento).isEqualTo(new BigDecimal("25.00")); // 25% de 100
        }
        
        @Test
        @DisplayName("No debe aplicar descuento para menos de 3 items")
        void noDebeAplicarDescuentoParaMenosDeTresItems() {
            // Arrange
            DescuentoContexto contexto1 = crearContexto(1, "NORMAL");
            DescuentoContexto contexto2 = crearContexto(2, "NORMAL");
            
            // Act
            BigDecimal descuento1 = strategy.calcularDescuento(productoTest, contexto1);
            BigDecimal descuento2 = strategy.calcularDescuento(productoTest, contexto2);
            
            // Assert
            assertThat(descuento1).isEqualTo(BigDecimal.ZERO);
            assertThat(descuento2).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe mantener el descuento del escalón más alto aplicable")
        void debeMantenerDescuentoEscalonMasAlto() {
            // Arrange - Probar valores en los límites
            DescuentoContexto contexto4 = crearContexto(4, "NORMAL");  // Escalón 3+
            DescuentoContexto contexto9 = crearContexto(9, "NORMAL");  // Escalón 5+
            DescuentoContexto contexto19 = crearContexto(19, "NORMAL"); // Escalón 10+
            DescuentoContexto contexto100 = crearContexto(100, "NORMAL"); // Escalón 50+
            
            // Act
            BigDecimal descuento4 = strategy.calcularDescuento(productoTest, contexto4);
            BigDecimal descuento9 = strategy.calcularDescuento(productoTest, contexto9);
            BigDecimal descuento19 = strategy.calcularDescuento(productoTest, contexto19);
            BigDecimal descuento100 = strategy.calcularDescuento(productoTest, contexto100);
            
            // Assert
            assertThat(descuento4).isEqualTo(new BigDecimal("5.00"));   // 5%
            assertThat(descuento9).isEqualTo(new BigDecimal("10.00"));  // 10%
            assertThat(descuento19).isEqualTo(new BigDecimal("15.00")); // 15%
            assertThat(descuento100).isEqualTo(new BigDecimal("25.00")); // 25%
        }
    }
    
    @Nested
    @DisplayName("Descuentos VIP")
    class DescuentosVip {
        
        @Test
        @DisplayName("Usuario VIP debe obtener 20% adicional sobre descuento base")
        void usuarioVipDebeObtenerDescuentoAdicional() {
            // Arrange
            DescuentoContexto contextoVip = crearContexto(5, "VIP");
            DescuentoContexto contextoNormal = crearContexto(5, "NORMAL");
            
            // Act
            BigDecimal descuentoVip = strategy.calcularDescuento(productoTest, contextoVip);
            BigDecimal descuentoNormal = strategy.calcularDescuento(productoTest, contextoNormal);
            
            // Assert
            // Normal: 10%, VIP: 10% + 20% de 10% = 12%
            assertThat(descuentoNormal).isEqualTo(new BigDecimal("10.00"));
            assertThat(descuentoVip).isEqualTo(new BigDecimal("12.00"));
        }
        
        @Test
        @DisplayName("Descuento VIP no debe exceder 30% máximo")
        void descuentoVipNoDebeExcederMaximo() {
            // Arrange - 50+ items da 25% base, VIP sería 30%, no debe exceder
            DescuentoContexto contextoVip = crearContexto(50, "VIP");
            
            // Act
            BigDecimal descuentoVip = strategy.calcularDescuento(productoTest, contextoVip);
            
            // Assert
            // 25% base + 20% de 25% = 30% (límite máximo)
            assertThat(descuentoVip).isEqualTo(new BigDecimal("30.00"));
        }
        
        @Test
        @DisplayName("Usuario VIP con pocas cantidades también obtiene bono")
        void usuarioVipConPocasCantidadesTambienObtieneBonus() {
            // Arrange - 3 items da 5% base
            DescuentoContexto contextoVip = crearContexto(3, "VIP");
            
            // Act
            BigDecimal descuentoVip = strategy.calcularDescuento(productoTest, contextoVip);
            
            // Assert
            // 5% base + 20% de 5% = 6%
            assertThat(descuentoVip).isEqualTo(new BigDecimal("6.00"));
        }
    }
    
    @Nested
    @DisplayName("Casos Edge y Validaciones")
    class CasosEdge {
        
        @Test
        @DisplayName("Debe retornar cero cuando producto es null")
        void debeRetornarCeroCuandoProductoEsNull() {
            // Arrange
            DescuentoContexto contexto = crearContexto(5, "NORMAL");
            
            // Act
            BigDecimal descuento = strategy.calcularDescuento(null, contexto);
            
            // Assert
            assertThat(descuento).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe retornar cero cuando contexto es null")
        void debeRetornarCeroCuandoContextoEsNull() {
            // Act
            BigDecimal descuento = strategy.calcularDescuento(productoTest, null);
            
            // Assert
            assertThat(descuento).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe retornar cero cuando precio es null o inválido")
        void debeRetornarCeroCuandoPrecioEsInvalido() {
            // Arrange
            Producto productoSinPrecio = crearProducto("Test", "Zapatos", null);
            Producto productoPrecioCero = crearProducto("Test", "Zapatos", BigDecimal.ZERO);
            Producto productoPrecioNegativo = crearProducto("Test", "Zapatos", new BigDecimal("-10"));
            DescuentoContexto contexto = crearContexto(5, "NORMAL");
            
            // Act
            BigDecimal descuento1 = strategy.calcularDescuento(productoSinPrecio, contexto);
            BigDecimal descuento2 = strategy.calcularDescuento(productoPrecioCero, contexto);
            BigDecimal descuento3 = strategy.calcularDescuento(productoPrecioNegativo, contexto);
            
            // Assert
            assertThat(descuento1).isEqualTo(BigDecimal.ZERO);
            assertThat(descuento2).isEqualTo(BigDecimal.ZERO);
            assertThat(descuento3).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe retornar cero cuando cantidad en carrito es null o inválida")
        void debeRetornarCeroCuandoCantidadEsInvalida() {
            // Arrange
            DescuentoContexto contextoSinCantidad = DescuentoContexto.builder()
                    .conTipoUsuario("NORMAL")
                    .build();
            DescuentoContexto contextoCantidadCero = crearContexto(0, "NORMAL");
            DescuentoContexto contextoCantidadNegativa = crearContexto(-5, "NORMAL");
            
            // Act
            BigDecimal descuento1 = strategy.calcularDescuento(productoTest, contextoSinCantidad);
            BigDecimal descuento2 = strategy.calcularDescuento(productoTest, contextoCantidadCero);
            BigDecimal descuento3 = strategy.calcularDescuento(productoTest, contextoCantidadNegativa);
            
            // Assert
            assertThat(descuento1).isEqualTo(BigDecimal.ZERO);
            assertThat(descuento2).isEqualTo(BigDecimal.ZERO);
            assertThat(descuento3).isEqualTo(BigDecimal.ZERO);
        }
    }
    
    @Nested
    @DisplayName("Aplicabilidad de Estrategia")
    class AplicabilidadEstrategia {
        
        @Test
        @DisplayName("Debe ser aplicable para cantidad mayor o igual al umbral mínimo")
        void debeSerAplicableParaCantidadMayorIgualUmbral() {
            // Arrange
            DescuentoContexto contexto3 = crearContexto(3, "NORMAL");  // Umbral mínimo
            DescuentoContexto contexto10 = crearContexto(10, "NORMAL"); // Más del mínimo
            
            // Act & Assert
            assertThat(strategy.esAplicable(productoTest, contexto3)).isTrue();
            assertThat(strategy.esAplicable(productoTest, contexto10)).isTrue();
        }
        
        @Test
        @DisplayName("No debe ser aplicable para cantidad menor al umbral mínimo")
        void noDebeSerAplicableParaCantidadMenorUmbral() {
            // Arrange
            DescuentoContexto contexto1 = crearContexto(1, "NORMAL");
            DescuentoContexto contexto2 = crearContexto(2, "NORMAL");
            
            // Act & Assert
            assertThat(strategy.esAplicable(productoTest, contexto1)).isFalse();
            assertThat(strategy.esAplicable(productoTest, contexto2)).isFalse();
        }
        
        @Test
        @DisplayName("No debe ser aplicable cuando hay datos null")
        void noDebeSerAplicableCuandoHayDatosNull() {
            // Arrange
            DescuentoContexto contextoValido = crearContexto(5, "NORMAL");
            DescuentoContexto contextoSinCantidad = DescuentoContexto.builder()
                    .conTipoUsuario("NORMAL")
                    .build();
            
            // Act & Assert
            assertThat(strategy.esAplicable(null, contextoValido)).isFalse();
            assertThat(strategy.esAplicable(productoTest, null)).isFalse();
            assertThat(strategy.esAplicable(productoTest, contextoSinCantidad)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Métodos de Utilidad")
    class MetodosUtilidad {
        
        @Test
        @DisplayName("Debe retornar información correcta de la estrategia")
        void debeRetornarInformacionCorrectaEstrategia() {
            // Act & Assert
            assertThat(strategy.getNombreEstrategia()).isEqualTo("Descuento por Cantidad");
            assertThat(strategy.getPrioridad()).isEqualTo(5);
        }
        
        @Test
        @DisplayName("Debe retornar umbrales correctos")
        void debeRetornarUmbralesCorrectos() {
            // Act & Assert
            assertThat(strategy.getUmbralMinimo()).isEqualTo(3);
            assertThat(strategy.getUmbralMaximo()).isEqualTo(50);
            assertThat(strategy.getDescuentoMaximo()).isEqualTo(new BigDecimal("0.25"));
        }
        
        @Test
        @DisplayName("Debe verificar correctamente si cantidad califica")
        void debeVerificarCorrectamenteSiCantidadCalifica() {
            // Act & Assert
            assertThat(strategy.calificaParaDescuento(2)).isFalse();
            assertThat(strategy.calificaParaDescuento(3)).isTrue();
            assertThat(strategy.calificaParaDescuento(100)).isTrue();
            assertThat(strategy.calificaParaDescuento(null)).isFalse();
        }
        
        @Test
        @DisplayName("Debe retornar información correcta del próximo escalón")
        void debeRetornarInformacionCorrectaProximoEscalon() {
            // Act
            EscalonInfo proximo4 = strategy.getProximoEscalon(4);   // Siguiente: 5
            EscalonInfo proximo15 = strategy.getProximoEscalon(15); // Siguiente: 20
            EscalonInfo proximo100 = strategy.getProximoEscalon(100); // No hay siguiente
            
            // Assert
            assertThat(proximo4).isNotNull();
            assertThat(proximo4.getCantidadRequerida()).isEqualTo(5);
            assertThat(proximo4.getPorcentajeDescuento()).isEqualTo(new BigDecimal("0.10"));
            assertThat(proximo4.getCantidadFaltante()).isEqualTo(1);
            
            assertThat(proximo15).isNotNull();
            assertThat(proximo15.getCantidadRequerida()).isEqualTo(20);
            assertThat(proximo15.getCantidadFaltante()).isEqualTo(5);
            
            assertThat(proximo100).isNull(); // Ya está en el máximo
        }
        
        @Test
        @DisplayName("Debe generar información detallada de estrategia")
        void debeGenerarInformacionDetalladaEstrategia() {
            // Act
            String info = strategy.generarInformacionEstrategia();
            
            // Assert
            assertThat(info)
                .contains("DescuentoPorCantidadStrategy")
                .contains("Descuento por Cantidad")
                .contains("Prioridad: 5")
                .contains("Umbral mínimo: 3")
                .contains("Descuento máximo: 25%")
                .contains("3+ items: 5%")
                .contains("50+ items: 25%");
        }
    }
    
    @Nested
    @DisplayName("Clase EscalonInfo")
    class ClaseEscalonInfo {
        
        @Test
        @DisplayName("EscalonInfo debe contener información correcta")
        void escalonInfoDebeContenerInformacionCorrecta() {
            // Arrange
            EscalonInfo escalon = new EscalonInfo(10, new BigDecimal("0.15"), 3);
            
            // Act & Assert
            assertThat(escalon.getCantidadRequerida()).isEqualTo(10);
            assertThat(escalon.getPorcentajeDescuento()).isEqualTo(new BigDecimal("0.15"));
            assertThat(escalon.getCantidadFaltante()).isEqualTo(3);
        }
        
        @Test
        @DisplayName("EscalonInfo toString debe ser descriptivo")
        void escalonInfoToStringDebeSerDescriptivo() {
            // Arrange
            EscalonInfo escalon = new EscalonInfo(10, new BigDecimal("0.15"), 3);
            
            // Act
            String descripcion = escalon.toString();
            
            // Assert
            assertThat(descripcion)
                .contains("cantidad=10")
                .contains("descuento=15%")
                .contains("faltan=3");
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
    
    private DescuentoContexto crearContexto(Integer cantidad, String tipoUsuario) {
        return DescuentoContexto.builder()
                .conCantidadEnCarrito(cantidad)
                .conTipoUsuario(tipoUsuario)
                .conFechaCalculo(LocalDateTime.now())
                .build();
    }
}
