package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.factory.ProductoFactoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Demostración simple del Factory Pattern que se ejecuta al iniciar la aplicación en modo demo.
 */
@Component
@Profile("demo")
public class FactoryPatternRunner implements CommandLineRunner {

    @Autowired
    private ProductoFactoryManager factoryManager;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎬 DEMOSTRACIÓN DEL FACTORY PATTERN EN ACCIÓN");
        System.out.println("=".repeat(80));
        
        // Demo 1: Crear calzado de running
        System.out.println("\n📌 Demo 1: Creando Zapatillas de Running");
        System.out.println("-".repeat(50));
        
        ProductoCreationRequest requestRunning = ProductoCreationRequest.builder()
            .conNombre("Nike Air Zoom Pegasus")
            .conDescripcion("Zapatillas profesionales para running")
            .conPrecio(new BigDecimal("149.99"))
            .conCategoria("CALZADO")
            .conTipo("RUNNING")
            .conTalla("42")
            .conColor("Negro/Blanco")
            .conMaterial("Mesh transpirable")
            .build();
            
        try {
            var productoRunning = factoryManager.crearProducto(requestRunning);
            System.out.println("✅ Producto creado exitosamente:");
            System.out.println("   Nombre: " + productoRunning.getNombre());
            System.out.println("   Categoría: " + productoRunning.getCategoria());
            System.out.println("   Stock configurado automáticamente: " + productoRunning.getStockDisponible());
            System.out.println("   Precio: €" + productoRunning.getPrecio());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        
        // Demo 2: Crear calzado de fútbol
        System.out.println("\n📌 Demo 2: Creando Botas de Fútbol");
        System.out.println("-".repeat(50));
        
        ProductoCreationRequest requestFutbol = ProductoCreationRequest.builder()
            .conNombre("Adidas Predator Elite")
            .conDescripcion("Botas profesionales para fútbol")
            .conPrecio(new BigDecimal("299.99"))
            .conCategoria("CALZADO")
            .conTipo("FUTBOL")
            .conTalla("43")
            .conColor("Azul/Dorado")
            .conMaterial("Cuero sintético con tacos")
            .build();
            
        try {
            var productoFutbol = factoryManager.crearProducto(requestFutbol);
            System.out.println("✅ Producto creado exitosamente:");
            System.out.println("   Nombre: " + productoFutbol.getNombre());
            System.out.println("   Categoría: " + productoFutbol.getCategoria());
            System.out.println("   Stock configurado automáticamente: " + productoFutbol.getStockDisponible());
            System.out.println("   Precio: €" + productoFutbol.getPrecio());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        
        // Demo 3: Mostrar información de factories disponibles
        System.out.println("\n📌 Demo 3: Factories Disponibles");
        System.out.println("-".repeat(50));
        
        var factoryInfo = factoryManager.getFactoryInfo();
        factoryInfo.getFactories().forEach(factory -> {
            System.out.println("🏭 " + factory.toString());
        });
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎉 DEMOSTRACIÓN COMPLETADA - Factory Pattern funcionando correctamente!");
        System.out.println("=".repeat(80) + "\n");
    }
}
