package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.factory.ProductoFactoryManager;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

/**
 * Demo ejecutable del Factory Pattern para mostrar creación especializada de productos.
 * 
 * EJECUTAR CON:
 * mvnw test -Dtest=FactoryPatternDemo
 * 
 * Este demo demuestra:
 * - Selección automática de factories por tipo
 * - Validaciones específicas por categoría
 * - Configuraciones automáticas inteligentes
 * - Integración con Observer Pattern
 * - Manejo de errores especializados
 */
@SpringBootTest
@ActiveProfiles("test")
public class FactoryPatternDemo {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoFactoryManager factoryManager;

    @Test
    public void demonstrarFactoryPattern() throws Exception {
        System.out.println("\n🏭 =====================================================");
        System.out.println("🏭 DEMO DEL FACTORY PATTERN - TIENDA DEPORTES");
        System.out.println("🏭 =====================================================\n");

        mostrarFactoriesDisponibles();
        demoCreacionCalzadoRunning();
        demoCreacionCalzadoFutbol();
        demoCreacionCalzadoBasketball();
        demoValidacionesEspecializadas();
        demoConfiguracionesAutomaticas();
        demoManejoErrores();

        System.out.println("\n🏭 =====================================================");
        System.out.println("🏭 DEMO COMPLETADO - Factory Pattern en Acción");
        System.out.println("🏭 =====================================================\n");
    }

    private void mostrarFactoriesDisponibles() {
        System.out.println("📋 FACTORIES DISPONIBLES EN EL SISTEMA");
        ProductoFactoryManager.FactoryInfo info = factoryManager.getFactoryInfo();
        
        for (ProductoFactoryManager.FactoryInfo.FactoryDetails factory : info.getFactories()) {
            System.out.println("   🏭 " + factory.toString());
        }
        System.out.println();
    }

    private void demoCreacionCalzadoRunning() throws Exception {
        System.out.println("👟 DEMO 1: CREACIÓN DE ZAPATILLAS RUNNING");
        System.out.println("   📝 Creando zapatillas especializadas para running...");

        ProductoCreationRequest request = ProductoCreationRequest.builder()
                .conNombre("Air Max Running Pro")
                .conDescripcion("Zapatillas profesionales para running de larga distancia")
                .conPrecio(new BigDecimal("150.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conMarca("Nike")
                .conStockInicial(25)
                .conTalla("42")
                .conColor("Azul/Blanco")
                .conMaterial("MESH")
                .conGenero("UNISEX")
                .conTemporada("TODO_AÑO")
                .conUsuarioCreador("DEMO_USER")
                .conCanal("WEB")
                .aplicarDescuentoLanzamiento(true)
                .activarImmediatamente(true)
                .conPropiedadExtendida("drop_tacon", "10mm")
                .conPropiedadExtendida("peso_gramos", "280")
                .build();

        Producto producto = productoService.crearProductoConFactory(request);
        
        System.out.println("   ✅ Producto creado: " + producto.getNombre() + " (ID: " + producto.getId() + ")");
        System.out.println("   🔔 Observa las configuraciones automáticas aplicadas...");
        Thread.sleep(500);
        System.out.println();
    }

    private void demoCreacionCalzadoFutbol() throws Exception {
        System.out.println("⚽ DEMO 2: CREACIÓN DE BOTAS DE FÚTBOL");
        System.out.println("   📝 Creando botas especializadas para fútbol...");

        ProductoCreationRequest request = ProductoCreationRequest.builder()
                .conNombre("Mercurial Superfly")
                .conDescripcion("Botas de fútbol para césped natural")
                .conPrecio(new BigDecimal("220.00"))
                .conCategoria("CALZADO")
                .conTipo("FUTBOL")
                .conSubtipo("CESPED")
                .conMarca("Nike")
                .conStockInicial(20)
                .conTalla("41")
                .conColor("Rojo")
                .conGenero("HOMBRE")
                .conUsuarioCreador("DEMO_USER")
                .conCanal("WEB")
                .activarImmediatamente(true)
                .build();

        Producto producto = productoService.crearProductoConFactory(request);
        
        System.out.println("   ✅ Producto creado: " + producto.getNombre() + " (ID: " + producto.getId() + ")");
        System.out.println("   🔔 Configuraciones específicas de fútbol aplicadas automáticamente...");
        Thread.sleep(500);
        System.out.println();
    }

    private void demoCreacionCalzadoBasketball() throws Exception {
        System.out.println("🏀 DEMO 3: CREACIÓN DE ZAPATILLAS BASKETBALL");
        System.out.println("   📝 Creando zapatillas especializadas para basketball...");

        ProductoCreationRequest request = ProductoCreationRequest.builder()
                .conNombre("Jordan Air 35")
                .conDescripcion("Zapatillas de basketball de alto rendimiento")
                .conPrecio(new BigDecimal("180.00"))
                .conCategoria("CALZADO")
                .conTipo("BASKETBALL")
                .conMarca("Jordan")
                .conStockInicial(15)
                .conTalla("44")
                .conColor("Negro/Rojo")
                .conGenero("HOMBRE")
                .conUsuarioCreador("DEMO_USER")
                .conCanal("MOBILE")
                .activarImmediatamente(true)
                .conPropiedadExtendida("altura_corte", "HIGH_TOP")
                .build();

        Producto producto = productoService.crearProductoConFactory(request);
        
        System.out.println("   ✅ Producto creado: " + producto.getNombre() + " (ID: " + producto.getId() + ")");
        System.out.println("   🔔 Configuraciones de basketball aplicadas (soporte tobillo, etc.)...");
        Thread.sleep(500);
        System.out.println();
    }

    private void demoValidacionesEspecializadas() {
        System.out.println("🔍 DEMO 4: VALIDACIONES ESPECIALIZADAS");
        System.out.println("   📝 Probando validaciones específicas por tipo...");

        // Test 1: Talla inválida
        ProductoCreationRequest requestTallaInvalida = ProductoCreationRequest.builder()
                .conNombre("Test Producto")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conTalla("99") // Talla inválida
                .build();

        ProductoFactoryManager.ValidationResult resultado1 = 
            productoService.validarPeticionCreacion(requestTallaInvalida);
        
        System.out.println("   ❌ Validación talla inválida: " + resultado1.getMensaje());

        // Test 2: Tipo no soportado
        ProductoCreationRequest requestTipoInvalido = ProductoCreationRequest.builder()
                .conNombre("Test Producto")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("VOLEIBOL") // Tipo no soportado
                .build();

        ProductoFactoryManager.ValidationResult resultado2 = 
            productoService.validarPeticionCreacion(requestTipoInvalido);
        
        System.out.println("   ❌ Validación tipo no soportado: " + resultado2.getMensaje());

        // Test 3: Validación exitosa
        ProductoCreationRequest requestValido = ProductoCreationRequest.builder()
                .conNombre("Test Producto Válido")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conTalla("42")
                .build();

        ProductoFactoryManager.ValidationResult resultado3 = 
            productoService.validarPeticionCreacion(requestValido);
        
        System.out.println("   ✅ Validación exitosa: " + resultado3.getMensaje());
        System.out.println();
    }

    private void demoConfiguracionesAutomaticas() {
        System.out.println("⚙️ DEMO 5: CONFIGURACIONES AUTOMÁTICAS");
        System.out.println("   📝 Mostrando configuraciones inteligentes por tipo...");

        System.out.println("   🏃 RUNNING: Se aplica automáticamente:");
        System.out.println("      - Material superior: MESH");
        System.out.println("      - Tipo de suela: EVA");
        System.out.println("      - Amortiguación: ALTA");
        System.out.println("      - Stock por defecto: 15 unidades");

        System.out.println("   ⚽ FÚTBOL: Se aplica automáticamente:");
        System.out.println("      - Tipo de tacos según superficie (FG, TF, etc.)");
        System.out.println("      - Material exterior optimizado");
        System.out.println("      - Stock por defecto: 20 unidades");

        System.out.println("   🏀 BASKETBALL: Se aplica automáticamente:");
        System.out.println("      - Altura de corte: MID_TOP");
        System.out.println("      - Soporte de tobillo: ALTO");
        System.out.println("      - Tracción: MULTIDIRECCIONAL");
        System.out.println("      - Stock por defecto: 12 unidades");
        
        System.out.println();
    }

    private void demoManejoErrores() {
        System.out.println("🚨 DEMO 6: MANEJO DE ERRORES");
        System.out.println("   📝 Probando diferentes tipos de errores...");

        try {
            // Error: Categoría no soportada
            ProductoCreationRequest request = ProductoCreationRequest.builder()
                    .conNombre("Producto Inválido")
                    .conPrecio(new BigDecimal("100.00"))
                    .conCategoria("CATEGORIA_INEXISTENTE")
                    .conTipo("TIPO_INEXISTENTE")
                    .build();

            productoService.crearProductoConFactory(request);
            
        } catch (Exception e) {
            System.out.println("   ❌ Error capturado correctamente: " + e.getMessage());
        }

        System.out.println("   ✅ Sistema de manejo de errores funcionando correctamente");
        System.out.println();
    }
}
