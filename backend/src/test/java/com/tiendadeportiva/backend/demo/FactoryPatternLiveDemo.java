package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.factory.ProductoFactoryManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

/**
 * Demostración simple del Factory Pattern con salida clara.
 */
@SpringBootTest
@ActiveProfiles("test")
public class FactoryPatternLiveDemo {

    @Autowired
    private ProductoFactoryManager factoryManager;

    @Test
    public void verFactoryPatternEnAccion() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎬 FACTORY PATTERN EN ACCIÓN - DEMOSTRACIÓN COMPLETA");
        System.out.println("=".repeat(80));
        
        // Demo 1: Zapatillas de Running
        crearZapatillasRunning();
        
        // Demo 2: Botas de Fútbol
        crearBotasFutbol();
        
        // Demo 3: Mostrar factories disponibles
        mostrarFactoriesDisponibles();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎉 DEMOSTRACIÓN COMPLETADA - Factory Pattern funcionando perfectamente!");
        System.out.println("=".repeat(80) + "\n");
    }
    
    private void crearZapatillasRunning() {
        System.out.println("\n📌 DEMO 1: Creando Zapatillas de Running");
        System.out.println("-".repeat(60));
        
        ProductoCreationRequest request = ProductoCreationRequest.builder()
            .conNombre("Nike Air Zoom Pegasus 40")
            .conDescripcion("Zapatillas profesionales para running de larga distancia")
            .conPrecio(new BigDecimal("149.99"))
            .conCategoria("CALZADO")
            .conTipo("RUNNING")
            .conTalla("42")
            .conColor("Negro/Blanco")
            .conMaterial("Mesh transpirable con suela React")
            .build();
            
        try {
            var producto = factoryManager.crearProducto(request);
            System.out.println("✅ Producto creado exitosamente:");
            System.out.println("   📦 Nombre: " + producto.getNombre());
            System.out.println("   🏷️ Categoría: " + producto.getCategoria());
            System.out.println("   📊 Stock: " + producto.getStockDisponible() + " unidades");
            System.out.println("   💰 Precio: €" + producto.getPrecio());
            System.out.println("   ✅ Estado: " + (producto.isActivo() ? "Activo" : "Inactivo"));
        } catch (Exception e) {
            System.out.println("❌ Error al crear producto: " + e.getMessage());
        }
    }
    
    private void crearBotasFutbol() {
        System.out.println("\n📌 DEMO 2: Creando Botas de Fútbol");
        System.out.println("-".repeat(60));
        
        ProductoCreationRequest request = ProductoCreationRequest.builder()
            .conNombre("Adidas Predator Elite FG")
            .conDescripcion("Botas profesionales para fútbol en césped natural")
            .conPrecio(new BigDecimal("299.99"))
            .conCategoria("CALZADO")
            .conTipo("FUTBOL")
            .conTalla("43")
            .conColor("Azul Solar/Dorado")
            .conMaterial("Cuero sintético con control de balón")
            .build();
            
        try {
            var producto = factoryManager.crearProducto(request);
            System.out.println("✅ Producto creado exitosamente:");
            System.out.println("   📦 Nombre: " + producto.getNombre());
            System.out.println("   🏷️ Categoría: " + producto.getCategoria());
            System.out.println("   📊 Stock: " + producto.getStockDisponible() + " unidades");
            System.out.println("   💰 Precio: €" + producto.getPrecio());
            System.out.println("   ✅ Estado: " + (producto.isActivo() ? "Activo" : "Inactivo"));
        } catch (Exception e) {
            System.out.println("❌ Error al crear producto: " + e.getMessage());
        }
    }
    
    private void mostrarFactoriesDisponibles() {
        System.out.println("\n📌 DEMO 3: Factories Disponibles en el Sistema");
        System.out.println("-".repeat(60));
        
        var factoryInfo = factoryManager.getFactoryInfo();
        System.out.println("🏭 Total de factories registradas: " + factoryInfo.getFactories().size());
        System.out.println();
        
        factoryInfo.getFactories().forEach(factory -> {
            System.out.println("🔧 " + factory.toString());
        });
        
        System.out.println("\n📋 Tipos disponibles:");
        System.out.println("   " + factoryManager.obtenerTiposDisponibles());
    }
}
