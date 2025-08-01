package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.factory.ProductoFactoryManager;
import com.tiendadeportiva.backend.model.Producto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Demo simple del Factory Pattern para ejecución con Spring Boot.
 * 
 * Para ejecutar:
 * mvnw spring-boot:run -Ddemo.factory.enabled=true
 */
@Component
@ConditionalOnProperty(value = "demo.factory.enabled", havingValue = "true")
public class SimpleFactoryDemo implements CommandLineRunner {

    private final ProductoFactoryManager factoryManager;

    public SimpleFactoryDemo(ProductoFactoryManager factoryManager) {
        this.factoryManager = factoryManager;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🏭 =====================================================");
        System.out.println("🏭 DEMO SIMPLE DEL FACTORY PATTERN");
        System.out.println("🏭 =====================================================\n");

        demoFactoriesDisponibles();
        demoCreacionCalzadoRunning();
        demoValidaciones();

        System.out.println("\n🏭 =====================================================");
        System.out.println("🏭 DEMO FACTORY PATTERN COMPLETADO");
        System.out.println("🏭 =====================================================\n");
    }

    private void demoFactoriesDisponibles() {
        System.out.println("📋 FACTORIES DISPONIBLES:");
        ProductoFactoryManager.FactoryInfo info = factoryManager.getFactoryInfo();
        
        for (ProductoFactoryManager.FactoryInfo.FactoryDetails factory : info.getFactories()) {
            System.out.println("   🏭 " + factory.toString());
        }
        System.out.println();
    }

    private void demoCreacionCalzadoRunning() {
        System.out.println("👟 CREANDO ZAPATILLAS RUNNING CON FACTORY:");
        
        ProductoCreationRequest request = ProductoCreationRequest.builder()
                .conNombre("Demo Running Shoes")
                .conDescripcion("Zapatillas de running profesionales")
                .conPrecio(new BigDecimal("120.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conMarca("Nike")
                .conStockInicial(20)
                .conTalla("42")
                .activarImmediatamente(true)
                .build();

        try {
            Producto producto = factoryManager.crearProducto(request);
            System.out.println("   ✅ Producto creado: " + producto.getNombre());
            System.out.println("   📦 Stock inicial: " + producto.getStockDisponible());
            System.out.println("   💰 Precio: $" + producto.getPrecio());
            System.out.println("   🔧 Configuraciones automáticas aplicadas");
        } catch (Exception e) {
            System.out.println("   ❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private void demoValidaciones() {
        System.out.println("🔍 DEMO DE VALIDACIONES:");
        
        // Validación exitosa
        ProductoCreationRequest requestValido = ProductoCreationRequest.builder()
                .conNombre("Producto Válido")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conTalla("42")
                .build();

        ProductoFactoryManager.ValidationResult resultado1 = 
            factoryManager.validarPeticion(requestValido);
        System.out.println("   ✅ Validación correcta: " + resultado1.getMensaje());

        // Validación con error
        ProductoCreationRequest requestInvalido = ProductoCreationRequest.builder()
                .conNombre("Producto Inválido")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conTalla("99") // Talla inválida
                .build();

        ProductoFactoryManager.ValidationResult resultado2 = 
            factoryManager.validarPeticion(requestInvalido);
        System.out.println("   ❌ Validación con error: " + resultado2.getMensaje());
        System.out.println();
    }
}
