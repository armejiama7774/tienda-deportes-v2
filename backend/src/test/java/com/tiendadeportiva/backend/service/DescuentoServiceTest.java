package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.DescuentoService.DescuentoInfo;
import com.tiendadeportiva.backend.service.DescuentoService.EstrategiaInfo;
import com.tiendadeportiva.backend.service.descuento.DescuentoContexto;
import com.tiendadeportiva.backend.service.descuento.DescuentoPorCantidadStrategy;
import com.tiendadeportiva.backend.service.descuento.DescuentoPorCategoriaStrategy;
import com.tiendadeportiva.backend.service.descuento.IDescuentoStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DescuentoService (Coordinador de Estrategias).
 * 
 * OBJETIVO EDUCATIVO:
 * - Demostrar testing de coordinador de patrones Strategy
 * - Mostrar uso de mocks para aislar lógica de coordinación
 * - Enseñar testing de algoritmos de prioridad
 * - Validar manejo de errores en coordinación
 * - Probar métricas de performance y diagnóstico
 * 
 * CASOS DE PRUEBA:
 * 1. Coordinación correcta de estrategias por prioridad
 * 2. Selección de primera estrategia aplicable
 * 3. Manejo de errores graceful
 * 4. Métricas de performance
 * 5. Métodos de diagnóstico y utilidad
 * 6. Compatibilidad con API anterior
 * 
 * @author Equipo Desarrollo
 * @version 3.0
 * @since Fase 2 - Strategy Pattern Coordinator Tests
 */
@DisplayName("DescuentoService - Tests del Coordinador")
class DescuentoServiceTest {
    
    @Mock
    private IDescuentoStrategy estrategiaMockAlta;    // Prioridad 1
    
    @Mock
    private IDescuentoStrategy estrategiaMockMedia;   // Prioridad 5
    
    @Mock
    private IDescuentoStrategy estrategiaMockBaja;    // Prioridad 10
    
    private DescuentoService service;
    private Producto productoTest;
    private DescuentoContexto contextoTest;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configurar mocks de estrategias con diferentes prioridades
        configurarEstrategiaMock(estrategiaMockAlta, "Estrategia Alta Prioridad", 1);
        configurarEstrategiaMock(estrategiaMockMedia, "Estrategia Media Prioridad", 5);
        configurarEstrategiaMock(estrategiaMockBaja, "Estrategia Baja Prioridad", 10);
        
        // Crear servicio con estrategias mock
        List<IDescuentoStrategy> estrategias = Arrays.asList(
            estrategiaMockBaja,    // Desordenadas intencionalmente
            estrategiaMockAlta,
            estrategiaMockMedia
        );
        service = new DescuentoService(estrategias);
        
        // Datos de prueba
        productoTest = crearProducto("Producto Test", "Zapatos", new BigDecimal("100.00"));
        contextoTest = DescuentoContexto.builder()
                .conCantidadEnCarrito(5)
                .conTipoUsuario("NORMAL")
                .conFechaCalculo(LocalDateTime.now())
                .build();
    }
    
    @Nested
    @DisplayName("Coordinación de Estrategias")
    class CoordinacionEstrategias {
        
        @Test
        @DisplayName("Debe aplicar primera estrategia aplicable según prioridad")
        void debeAplicarPrimeraEstrategiaAplicableSegunPrioridad() {
            // Arrange
            when(estrategiaMockAlta.esAplicable(eq(productoTest), any())).thenReturn(true);
            when(estrategiaMockAlta.calcularDescuento(eq(productoTest), any()))
                .thenReturn(new BigDecimal("15.00"));
            
            when(estrategiaMockMedia.esAplicable(eq(productoTest), any())).thenReturn(true);
            when(estrategiaMockMedia.calcularDescuento(eq(productoTest), any()))
                .thenReturn(new BigDecimal("10.00"));
            
            // Act
            DescuentoInfo resultado = service.aplicarDescuentos(productoTest, contextoTest);
            
            // Assert
            assertThat(resultado.getTotalDescuento()).isEqualTo(new BigDecimal("15.00"));
            assertThat(resultado.getEstrategiaAplicada()).isEqualTo("Estrategia Alta Prioridad");
            assertThat(resultado.getPrecioFinal()).isEqualTo(new BigDecimal("85.00"));
            
            // Verificar que solo se evaluó la estrategia de alta prioridad
            verify(estrategiaMockAlta).esAplicable(eq(productoTest), any());
            verify(estrategiaMockAlta).calcularDescuento(eq(productoTest), any());
            verify(estrategiaMockMedia, never()).calcularDescuento(any(), any());
            verify(estrategiaMockBaja, never()).calcularDescuento(any(), any());
        }
        
        @Test
        @DisplayName("Debe continuar con siguiente estrategia si primera no es aplicable")
        void debeContinuarConSiguienteEstrategiaSiPrimeraNoEsAplicable() {
            // Arrange
            when(estrategiaMockAlta.esAplicable(eq(productoTest), any())).thenReturn(false);
            
            when(estrategiaMockMedia.esAplicable(eq(productoTest), any())).thenReturn(true);
            when(estrategiaMockMedia.calcularDescuento(eq(productoTest), any()))
                .thenReturn(new BigDecimal("8.00"));
            
            // Act
            DescuentoInfo resultado = service.aplicarDescuentos(productoTest, contextoTest);
            
            // Assert
            assertThat(resultado.getTotalDescuento()).isEqualTo(new BigDecimal("8.00"));
            assertThat(resultado.getEstrategiaAplicada()).isEqualTo("Estrategia Media Prioridad");
            
            // Verificar orden de evaluación
            verify(estrategiaMockAlta).esAplicable(eq(productoTest), any());
            verify(estrategiaMockMedia).esAplicable(eq(productoTest), any());
            verify(estrategiaMockMedia).calcularDescuento(eq(productoTest), any());
        }
        
        @Test
        @DisplayName("Debe retornar sin descuento si ninguna estrategia es aplicable")
        void debeRetornarSinDescuentoSiNingunaEstrategiaEsAplicable() {
            // Arrange
            when(estrategiaMockAlta.esAplicable(any(), any())).thenReturn(false);
            when(estrategiaMockMedia.esAplicable(any(), any())).thenReturn(false);
            when(estrategiaMockBaja.esAplicable(any(), any())).thenReturn(false);
            
            // Act
            DescuentoInfo resultado = service.aplicarDescuentos(productoTest, contextoTest);
            
            // Assert
            assertThat(resultado.getTotalDescuento()).isEqualTo(BigDecimal.ZERO);
            assertThat(resultado.getPrecioFinal()).isEqualTo(new BigDecimal("100.00"));
            assertThat(resultado.tieneDescuentos()).isFalse();
            
            // Verificar que se evaluaron todas las estrategias
            verify(estrategiaMockAlta).esAplicable(any(), any());
            verify(estrategiaMockMedia).esAplicable(any(), any());
            verify(estrategiaMockBaja).esAplicable(any(), any());
        }
        
        @Test
        @DisplayName("Debe usar contexto por defecto si no se proporciona")
        void debeUsarContextoPorDefectoSiNoSeProvee() {
            // Arrange
            when(estrategiaMockAlta.esAplicable(eq(productoTest), any())).thenReturn(true);
            when(estrategiaMockAlta.calcularDescuento(eq(productoTest), any()))
                .thenReturn(new BigDecimal("10.00"));
            
            // Act
            DescuentoInfo resultado = service.aplicarDescuentos(productoTest);
            
            // Assert
            assertThat(resultado.getTotalDescuento()).isEqualTo(new BigDecimal("10.00"));
            
            // Verificar que se pasó un contexto (no null)
            verify(estrategiaMockAlta).esAplicable(eq(productoTest), notNull());
            verify(estrategiaMockAlta).calcularDescuento(eq(productoTest), notNull());
        }
    }
    
    @Nested
    @DisplayName("Manejo de Errores")
    class ManejoErrores {
        
        @Test
        @DisplayName("Debe manejar errores en estrategia y continuar con siguiente")
        void debeManejarErroresEnEstrategiaYContinuar() {
            // Arrange
            when(estrategiaMockAlta.esAplicable(any(), any())).thenReturn(true);
            when(estrategiaMockAlta.calcularDescuento(any(), any()))
                .thenThrow(new RuntimeException("Error en estrategia"));
            
            when(estrategiaMockMedia.esAplicable(any(), any())).thenReturn(true);
            when(estrategiaMockMedia.calcularDescuento(any(), any()))
                .thenReturn(new BigDecimal("5.00"));
            
            // Act
            DescuentoInfo resultado = service.aplicarDescuentos(productoTest, contextoTest);
            
            // Assert
            assertThat(resultado.getTotalDescuento()).isEqualTo(new BigDecimal("5.00"));
            assertThat(resultado.getEstrategiaAplicada()).isEqualTo("Estrategia Media Prioridad");
            
            // Verificar que se intentó la primera estrategia y luego la segunda
            verify(estrategiaMockAlta).calcularDescuento(any(), any());
            verify(estrategiaMockMedia).calcularDescuento(any(), any());
        }
        
        @Test
        @DisplayName("Debe retornar sin descuento cuando producto es null")
        void debeRetornarSinDescuentoCuandoProductoEsNull() {
            // Act
            DescuentoInfo resultado = service.aplicarDescuentos(null, contextoTest);
            
            // Assert
            assertThat(resultado.getTotalDescuento()).isEqualTo(BigDecimal.ZERO);
            assertThat(resultado.getPrecioOriginal()).isEqualTo(BigDecimal.ZERO);
            
            // Verificar que no se llamaron las estrategias
            verify(estrategiaMockAlta, never()).esAplicable(any(), any());
        }
        
        @Test
        @DisplayName("Debe retornar sin descuento cuando precio es inválido")
        void debeRetornarSinDescuentoCuandoPrecioEsInvalido() {
            // Arrange
            Producto productoSinPrecio = crearProducto("Test", "Zapatos", null);
            Producto productoPrecioNegativo = crearProducto("Test", "Zapatos", new BigDecimal("-10"));
            
            // Act
            DescuentoInfo resultado1 = service.aplicarDescuentos(productoSinPrecio, contextoTest);
            DescuentoInfo resultado2 = service.aplicarDescuentos(productoPrecioNegativo, contextoTest);
            
            // Assert
            assertThat(resultado1.getTotalDescuento()).isEqualTo(BigDecimal.ZERO);
            assertThat(resultado2.getTotalDescuento()).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Debe limitar descuento si excede precio del producto")
        void debeLimitarDescuentoSiExcedePrecio() {
            // Arrange
            when(estrategiaMockAlta.esAplicable(any(), any())).thenReturn(true);
            when(estrategiaMockAlta.calcularDescuento(any(), any()))
                .thenReturn(new BigDecimal("150.00")); // Más que el precio del producto (100)
            
            // Act
            DescuentoInfo resultado = service.aplicarDescuentos(productoTest, contextoTest);
            
            // Assert
            assertThat(resultado.getTotalDescuento()).isEqualTo(new BigDecimal("100.00")); // Limitado al precio
            assertThat(resultado.getPrecioFinal()).isEqualTo(BigDecimal.ZERO);
        }
    }
    
    @Nested
    @DisplayName("Métodos de Diagnóstico")
    class MetodosDiagnostico {
        
        @Test
        @DisplayName("Debe retornar información de estrategias disponibles")
        void debeRetornarInformacionEstrategiasDisponibles() {
            // Act
            List<EstrategiaInfo> estrategias = service.getEstrategiasDisponibles();
            
            // Assert
            assertThat(estrategias).hasSize(3);
            
            // Verificar orden por prioridad
            assertThat(estrategias.get(0).getNombre()).isEqualTo("Estrategia Alta Prioridad");
            assertThat(estrategias.get(0).getPrioridad()).isEqualTo(1);
            
            assertThat(estrategias.get(1).getNombre()).isEqualTo("Estrategia Media Prioridad");
            assertThat(estrategias.get(1).getPrioridad()).isEqualTo(5);
            
            assertThat(estrategias.get(2).getNombre()).isEqualTo("Estrategia Baja Prioridad");
            assertThat(estrategias.get(2).getPrioridad()).isEqualTo(10);
        }
        
        @Test
        @DisplayName("Debe evaluar correctamente estrategias aplicables")
        void debeEvaluarCorrectamenteEstrategiasAplicables() {
            // Arrange
            when(estrategiaMockAlta.esAplicable(any(), any())).thenReturn(true);
            when(estrategiaMockMedia.esAplicable(any(), any())).thenReturn(false);
            when(estrategiaMockBaja.esAplicable(any(), any())).thenReturn(true);
            
            // Act
            List<String> aplicables = service.evaluarEstrategiasAplicables(productoTest, contextoTest);
            
            // Assert
            assertThat(aplicables).containsExactly(
                "Estrategia Alta Prioridad",
                "Estrategia Baja Prioridad"
            );
        }
        
        @Test
        @DisplayName("Debe generar reporte de diagnóstico completo")
        void debeGenerarReporteDiagnosticoCompleto() {
            // Act
            String reporte = service.generarReporteDiagnostico();
            
            // Assert
            assertThat(reporte)
                .contains("DescuentoService - Reporte de Diagnóstico")
                .contains("Estrategias cargadas: 3")
                .contains("1. Estrategia Alta Prioridad (Prioridad: 1")
                .contains("2. Estrategia Media Prioridad (Prioridad: 5")
                .contains("3. Estrategia Baja Prioridad (Prioridad: 10");
        }
    }
    
    @Nested
    @DisplayName("Compatibilidad con API Anterior")
    class CompatibilidadAPI {
        
        @Test
        @DisplayName("Método deprecated debe funcionar correctamente")
        void metodoDeprecatedDebeFuncionarCorrectamente() {
            // Arrange
            when(estrategiaMockAlta.esAplicable(any(), any())).thenReturn(true);
            when(estrategiaMockAlta.calcularDescuento(any(), any()))
                .thenReturn(new BigDecimal("12.00"));
            
            // Act
            @SuppressWarnings("deprecation")
            DescuentoInfo resultado = service.aplicarDescuentosAutomaticos(productoTest);
            
            // Assert
            assertThat(resultado.getTotalDescuento()).isEqualTo(new BigDecimal("12.00"));
            assertThat(resultado.getEstrategiaAplicada()).isEqualTo("Estrategia Alta Prioridad");
        }
    }
    
    @Nested
    @DisplayName("Clase DescuentoInfo")
    class ClaseDescuentoInfo {
        
        @Test
        @DisplayName("DescuentoInfo debe calcular porcentaje correctamente")
        void descuentoInfoDebeCalcularPorcentajeCorrectamente() {
            // Arrange
            DescuentoInfo info = new DescuentoInfo(
                new BigDecimal("100.00"),
                new BigDecimal("85.00"),
                "Test Strategy",
                new BigDecimal("15.00"),
                50L
            );
            
            // Act & Assert
            assertThat(info.getPorcentajeDescuento()).isEqualTo(new BigDecimal("15.0000"));
            assertThat(info.tieneDescuentos()).isTrue();
            assertThat(info.getTiempoCalculoMs()).isEqualTo(50L);
        }
        
        @Test
        @DisplayName("DescuentoInfo sin descuentos debe funcionar correctamente")
        void descuentoInfoSinDescuentosDebeFuncionarCorrectamente() {
            // Act
            DescuentoInfo info = DescuentoInfo.sinDescuentos(new BigDecimal("100.00"));
            
            // Assert
            assertThat(info.getPrecioOriginal()).isEqualTo(new BigDecimal("100.00"));
            assertThat(info.getPrecioFinal()).isEqualTo(new BigDecimal("100.00"));
            assertThat(info.getTotalDescuento()).isEqualTo(BigDecimal.ZERO);
            assertThat(info.tieneDescuentos()).isFalse();
            assertThat(info.getEstrategiaAplicada()).isEqualTo("Sin descuento aplicable");
        }
        
        @Test
        @DisplayName("DescuentoInfo toString debe ser descriptivo")
        void descuentoInfoToStringDebeSerDescriptivo() {
            // Arrange
            DescuentoInfo info = new DescuentoInfo(
                new BigDecimal("100.00"),
                new BigDecimal("90.00"),
                "Test Strategy",
                new BigDecimal("10.00"),
                25L
            );
            
            // Act
            String descripcion = info.toString();
            
            // Assert
            assertThat(descripcion)
                .contains("original=$100.00")
                .contains("final=$90.00")
                .contains("descuento=$10.00")
                .contains("estrategia='Test Strategy'")
                .contains("tiempo=25ms");
        }
    }
    
    @Nested
    @DisplayName("Tests de Integración con Estrategias Reales")
    class IntegracionEstrategiasReales {
        
        private DescuentoService serviceReal;
        
        @BeforeEach
        void setUpReal() {
            // Crear servicio con estrategias reales
            List<IDescuentoStrategy> estrategiasReales = Arrays.asList(
                new DescuentoPorCantidadStrategy(),    // Prioridad 5
                new DescuentoPorCategoriaStrategy()    // Prioridad 10
            );
            serviceReal = new DescuentoService(estrategiasReales);
        }
        
        @Test
        @DisplayName("Debe priorizar descuento por cantidad sobre categoría")
        void debePriorizarDescuentoPorCantidadSobreCategoria() {
            // Arrange - Producto que califica para ambas estrategias
            Producto producto = crearProducto("Zapatos Nike", "Zapatos", new BigDecimal("100.00"));
            DescuentoContexto contexto = DescuentoContexto.builder()
                    .conCantidadEnCarrito(5)  // Califica para descuento por cantidad (10%)
                    .conTipoUsuario("NORMAL")
                    .build();
            
            // Act
            DescuentoInfo resultado = serviceReal.aplicarDescuentos(producto, contexto);
            
            // Assert
            // Debe aplicar descuento por cantidad (prioridad 5) en lugar de categoría (prioridad 10)
            assertThat(resultado.getEstrategiaAplicada()).isEqualTo("Descuento por Cantidad");
            assertThat(resultado.getTotalDescuento()).isEqualTo(new BigDecimal("10.00")); // 10% de 100
        }
        
        @Test
        @DisplayName("Debe aplicar descuento por categoría cuando cantidad no califica")
        void debeAplicarDescuentoPorCategoriacuandoCantidadNoCalifica() {
            // Arrange - Producto que solo califica para categoría
            Producto producto = crearProducto("Zapatos Nike", "Zapatos", new BigDecimal("100.00"));
            DescuentoContexto contexto = DescuentoContexto.builder()
                    .conCantidadEnCarrito(1)  // No califica para descuento por cantidad
                    .conTipoUsuario("NORMAL")
                    .build();
            
            // Act
            DescuentoInfo resultado = serviceReal.aplicarDescuentos(producto, contexto);
            
            // Assert
            assertThat(resultado.getEstrategiaAplicada()).isEqualTo("Descuento por Categoría");
            assertThat(resultado.getTotalDescuento()).isEqualTo(new BigDecimal("10.00")); // 10% de 100
        }
    }
    
    // =============================================
    // MÉTODOS DE UTILIDAD PARA TESTS
    // =============================================
    
    private void configurarEstrategiaMock(IDescuentoStrategy mock, String nombre, int prioridad) {
        when(mock.getNombreEstrategia()).thenReturn(nombre);
        when(mock.getPrioridad()).thenReturn(prioridad);
    }
    
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
