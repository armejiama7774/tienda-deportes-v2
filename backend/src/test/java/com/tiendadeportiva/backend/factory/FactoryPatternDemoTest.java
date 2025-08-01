package com.tiendadeportiva.backend.factory;

import com.tiendadeportiva.backend.factory.impl.CalzadoFactory;
import com.tiendadeportiva.backend.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test unitario del Factory Pattern para demostrar funcionamiento.
 */
@DisplayName("Factory Pattern - Demo Tests")
class FactoryPatternDemoTest {

    private CalzadoFactory calzadoFactory;

    @BeforeEach
    void setUp() {
        calzadoFactory = new CalzadoFactory();
    }

    @Test
    @DisplayName("🏭 Demo: Creación de zapatillas running con configuraciones automáticas")
    void debeCrearZapatillasRunningConConfiguracionesAutomaticas() {
        System.out.println("\n👟 DEMO: Creando zapatillas running...");
        
        // ARRANGE
        ProductoCreationRequest request = ProductoCreationRequest.builder()
                .conNombre("Air Max Running Pro")
                .conDescripcion("Zapatillas profesionales para running")
                .conPrecio(new BigDecimal("150.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conMarca("Nike")
                .conStockInicial(25)
                .conTalla("42")
                .conColor("Azul/Blanco")
                .conMaterial("MESH")
                .activarImmediatamente(true)
                .build();

        System.out.println("📝 Request creado: " + request.toString());

        // ACT
        Producto producto = calzadoFactory.crearProducto(request);

        // ASSERT & DEMO
        assertThat(producto).isNotNull();
        assertThat(producto.getNombre()).isEqualTo("Air Max Running Pro");
        assertThat(producto.getCategoria()).isEqualTo("CALZADO");
        assertThat(producto.getPrecio()).isEqualTo(new BigDecimal("150.00"));
        
        System.out.println("✅ Producto creado exitosamente:");
        System.out.println("   📦 Nombre: " + producto.getNombre());
        System.out.println("   💰 Precio: $" + producto.getPrecio());
        System.out.println("   📊 Stock: " + producto.getStockDisponible() + " unidades");
        System.out.println("   ✅ Activo: " + producto.isActivo());
        System.out.println("   🔧 Configuraciones automáticas aplicadas por CalzadoFactory");
    }

    @Test
    @DisplayName("🏭 Demo: Validaciones específicas de calzado")
    void debeValidarDatosEspecificosDeCalzado() {
        System.out.println("\n🔍 DEMO: Validaciones específicas...");

        // Test 1: Talla válida
        ProductoCreationRequest requestValido = ProductoCreationRequest.builder()
                .conNombre("Test Válido")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conTalla("42") // Talla válida
                .build();

        System.out.println("✅ Validación exitosa para talla 42");
        
        // No debe lanzar excepción
        calzadoFactory.validarDatos(requestValido);

        // Test 2: Talla inválida
        ProductoCreationRequest requestInvalido = ProductoCreationRequest.builder()
                .conNombre("Test Inválido")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conTalla("99") // Talla inválida
                .build();

        System.out.println("❌ Probando validación con talla inválida (99)...");
        
        // Debe lanzar excepción
        ProductoCreationException exception = assertThrows(
            ProductoCreationException.class,
            () -> calzadoFactory.validarDatos(requestInvalido)
        );

        System.out.println("🚨 Error capturado: " + exception.getMessage());
        assertThat(exception.getErrorCode()).isEqualTo("INVALID_SIZE");
    }

    @Test
    @DisplayName("🏭 Demo: Factory puede crear diferentes tipos de calzado")
    void debePoderCrearDiferentesTiposDeCalzado() {
        System.out.println("\n🎯 DEMO: Verificando tipos soportados...");

        String[] tiposEsperados = {"RUNNING", "CASUAL", "FUTBOL", "BASKETBALL", "TRAINING", "HIKING"};
        
        System.out.println("📋 Tipos soportados por CalzadoFactory:");
        for (String tipo : tiposEsperados) {
            boolean puedeCrear = calzadoFactory.puedeCrear("CALZADO", tipo);
            System.out.println("   " + (puedeCrear ? "✅" : "❌") + " " + tipo);
            assertThat(puedeCrear).isTrue();
        }

        // Verificar tipo no soportado
        boolean puedeCrearVoleibol = calzadoFactory.puedeCrear("CALZADO", "VOLEIBOL");
        System.out.println("   ❌ VOLEIBOL (no soportado)");
        assertThat(puedeCrearVoleibol).isFalse();
    }

    @Test
    @DisplayName("🏭 Demo: Configuraciones específicas por tipo de calzado")
    void debeAplicarConfiguracionesEspecificasPorTipo() {
        System.out.println("\n⚙️ DEMO: Configuraciones automáticas por tipo...");

        // RUNNING
        System.out.println("🏃 Creando zapatillas RUNNING...");
        ProductoCreationRequest requestRunning = ProductoCreationRequest.builder()
                .conNombre("Running Shoes")
                .conPrecio(new BigDecimal("120.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conMarca("Nike")
                .build();

        Producto productoRunning = calzadoFactory.crearProducto(requestRunning);
        System.out.println("   ✅ Stock automático: " + productoRunning.getStockDisponible() + " unidades");

        // FÚTBOL
        System.out.println("⚽ Creando botas de FÚTBOL...");
        ProductoCreationRequest requestFutbol = ProductoCreationRequest.builder()
                .conNombre("Football Boots")
                .conPrecio(new BigDecimal("180.00"))
                .conCategoria("CALZADO")
                .conTipo("FUTBOL")
                .conSubtipo("CESPED")
                .conMarca("Adidas")
                .build();

        Producto productoFutbol = calzadoFactory.crearProducto(requestFutbol);
        System.out.println("   ✅ Stock automático: " + productoFutbol.getStockDisponible() + " unidades");

        // Verificar que los stocks son diferentes según el tipo
        assertThat(productoRunning.getStockDisponible()).isNotEqualTo(productoFutbol.getStockDisponible());
        System.out.println("🔧 Configuraciones automáticas aplicadas correctamente según el tipo");
    }
}
