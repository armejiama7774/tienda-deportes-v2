package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.factory.ProductoFactoryManager;
import com.tiendadeportiva.backend.model.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple para verificar que el Factory Pattern funciona correctamente.
 */
@SpringBootTest
@ActiveProfiles("test")
public class FactoryPatternBasicTest {

    @Autowired
    private ProductoFactoryManager factoryManager;

    @Test
    public void testFactoryManagerDisponible() {
        assertNotNull(factoryManager, "El ProductoFactoryManager debe estar disponible");
        System.out.println("✅ ProductoFactoryManager disponible");
    }

    @Test
    public void testCrearProductoBasico() {
        System.out.println("\n🧪 Test: Crear producto básico con Factory Pattern");
        
        ProductoCreationRequest request = ProductoCreationRequest.builder()
            .conNombre("Test Product")
            .conDescripcion("Producto de prueba")
            .conPrecio(new BigDecimal("99.99"))
            .conCategoria("CALZADO")
            .conTipo("RUNNING")
            .build();
            
        try {
            Producto producto = factoryManager.crearProducto(request);
            
            assertNotNull(producto, "El producto no debe ser null");
            assertEquals("Test Product", producto.getNombre());
            assertEquals(new BigDecimal("99.99"), producto.getPrecio());
            assertEquals("CALZADO", producto.getCategoria());
            
            System.out.println("✅ Producto creado exitosamente: " + producto.getNombre());
            System.out.println("📊 Stock inicial: " + producto.getStockDisponible());
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            fail("No debería haber error al crear producto: " + e.getMessage());
        }
    }

    @Test
    public void testFactoriesDisponibles() {
        System.out.println("\n🧪 Test: Verificar factories disponibles");
        
        var factoryInfo = factoryManager.getFactoryInfo();
        assertNotNull(factoryInfo, "La información de factories no debe ser null");
        
        System.out.println("🏭 Factories encontradas: " + factoryInfo.getFactories().size());
        
        factoryInfo.getFactories().forEach(factory -> {
            System.out.println("   - " + factory.toString());
        });
        
        assertTrue(factoryInfo.getFactories().size() > 0, "Debe haber al menos una factory disponible");
    }
}
