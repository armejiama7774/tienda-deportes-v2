package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.factory.ProductoFactory;
import com.tiendadeportiva.backend.factory.ProductoFactoryManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * Diagnóstico detallado del Factory Pattern.
 */
@SpringBootTest
@ActiveProfiles("test")
public class FactoryPatternDiagnosticTest {

    @Autowired(required = false)
    private ProductoFactoryManager factoryManager;
    
    @Autowired(required = false)
    private List<ProductoFactory> factories;

    @Test
    public void diagnosticarFactory() {
        System.out.println("\n🔍 DIAGNÓSTICO DEL FACTORY PATTERN");
        System.out.println("=" .repeat(50));
        
        // Verificar FactoryManager
        if (factoryManager != null) {
            System.out.println("✅ ProductoFactoryManager: ENCONTRADO");
            System.out.println("   Clase: " + factoryManager.getClass().getName());
        } else {
            System.out.println("❌ ProductoFactoryManager: NO ENCONTRADO");
        }
        
        // Verificar Factories individuales
        if (factories != null && !factories.isEmpty()) {
            System.out.println("✅ Factories individuales: " + factories.size() + " encontradas");
            for (ProductoFactory factory : factories) {
                System.out.println("   - " + factory.getClass().getSimpleName());
            }
        } else {
            System.out.println("❌ Factories individuales: NO ENCONTRADAS");
        }
        
        // Test básico si todo está disponible
        if (factoryManager != null) {
            try {
                var info = factoryManager.getFactoryInfo();
                System.out.println("✅ getFactoryInfo(): FUNCIONA");
                System.out.println("   Factories registradas: " + info.getFactories().size());
            } catch (Exception e) {
                System.out.println("❌ getFactoryInfo(): ERROR - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
