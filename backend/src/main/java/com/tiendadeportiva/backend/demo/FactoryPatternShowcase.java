package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.factory.ProductoFactoryManager;
import com.tiendadeportiva.backend.model.Producto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Clase utilitaria para demostrar el Factory Pattern de forma programática.
 * Útil para llamar desde controllers o tests.
 */
@Component
public class FactoryPatternShowcase {

    private final ProductoFactoryManager factoryManager;

    public FactoryPatternShowcase(ProductoFactoryManager factoryManager) {
        this.factoryManager = factoryManager;
    }

    /**
     * Demostración completa del Factory Pattern con output formateado.
     */
    public String demonstrarFactoryPattern() {
        StringBuilder demo = new StringBuilder();
        
        demo.append("\n🏭 =====================================================\n");
        demo.append("🏭 DEMOSTRACIÓN DEL FACTORY PATTERN\n");
        demo.append("🏭 =====================================================\n\n");

        // 1. Mostrar factories disponibles
        demo.append(mostrarFactoriesDisponibles());
        
        // 2. Demo de creación de productos
        demo.append(demoCreacionZapatillasRunning());
        demo.append(demoCreacionBotasFutbol());
        demo.append(demoCreacionZapatillasBasketball());
        
        // 3. Demo de validaciones
        demo.append(demoValidaciones());
        
        demo.append("\n🏭 =====================================================\n");
        demo.append("🏭 DEMOSTRACIÓN COMPLETADA EXITOSAMENTE\n");
        demo.append("🏭 =====================================================\n");

        return demo.toString();
    }

    private String mostrarFactoriesDisponibles() {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 FACTORIES DISPONIBLES EN EL SISTEMA:\n");
        
        ProductoFactoryManager.FactoryInfo info = factoryManager.getFactoryInfo();
        for (ProductoFactoryManager.FactoryInfo.FactoryDetails factory : info.getFactories()) {
            sb.append("   🏭 ").append(factory.toString()).append("\n");
        }
        sb.append("\n");
        
        return sb.toString();
    }

    private String demoCreacionZapatillasRunning() {
        StringBuilder sb = new StringBuilder();
        sb.append("👟 DEMO 1: CREACIÓN DE ZAPATILLAS RUNNING\n");
        sb.append("   📝 Creando 'Air Max Running Pro' con configuraciones automáticas...\n");

        try {
            ProductoCreationRequest request = ProductoCreationRequest.builder()
                    .conNombre("Air Max Running Pro")
                    .conDescripcion("Zapatillas profesionales para running de larga distancia")
                    .conPrecio(new BigDecimal("150.00"))
                    .conCategoria("CALZADO")
                    .conTipo("RUNNING")
                    .conMarca("Nike")
                    .conStockInicial(null) // Para ver stock automático
                    .conTalla("42")
                    .conColor("Azul/Blanco")
                    .conMaterial("MESH")
                    .aplicarDescuentoLanzamiento(true)
                    .activarImmediatamente(true)
                    .build();

            Producto producto = factoryManager.crearProducto(request);
            
            sb.append("   ✅ Producto creado exitosamente:\n");
            sb.append("      📦 Nombre: ").append(producto.getNombre()).append("\n");
            sb.append("      💰 Precio: $").append(producto.getPrecio()).append("\n");
            sb.append("      📊 Stock: ").append(producto.getStockDisponible()).append(" unidades (configurado automáticamente)\n");
            sb.append("      ✅ Activo: ").append(producto.isActivo()).append("\n");
            sb.append("      🔧 Configuraciones automáticas de RUNNING aplicadas\n");
            
        } catch (Exception e) {
            sb.append("   ❌ Error: ").append(e.getMessage()).append("\n");
        }
        
        sb.append("\n");
        return sb.toString();
    }

    private String demoCreacionBotasFutbol() {
        StringBuilder sb = new StringBuilder();
        sb.append("⚽ DEMO 2: CREACIÓN DE BOTAS DE FÚTBOL\n");
        sb.append("   📝 Creando 'Mercurial Superfly' con configuraciones de fútbol...\n");

        try {
            ProductoCreationRequest request = ProductoCreationRequest.builder()
                    .conNombre("Mercurial Superfly")
                    .conDescripcion("Botas de fútbol para césped natural")
                    .conPrecio(new BigDecimal("220.00"))
                    .conCategoria("CALZADO")
                    .conTipo("FUTBOL")
                    .conSubtipo("CESPED")
                    .conMarca("Nike")
                    .conStockInicial(null) // Para ver stock automático
                    .conTalla("41")
                    .conColor("Rojo")
                    .activarImmediatamente(true)
                    .build();

            Producto producto = factoryManager.crearProducto(request);
            
            sb.append("   ✅ Producto creado exitosamente:\n");
            sb.append("      📦 Nombre: ").append(producto.getNombre()).append("\n");
            sb.append("      💰 Precio: $").append(producto.getPrecio()).append("\n");
            sb.append("      📊 Stock: ").append(producto.getStockDisponible()).append(" unidades (configurado para FUTBOL)\n");
            sb.append("      🔧 Configuraciones específicas de FÚTBOL aplicadas (tipo de tacos, etc.)\n");
            
        } catch (Exception e) {
            sb.append("   ❌ Error: ").append(e.getMessage()).append("\n");
        }
        
        sb.append("\n");
        return sb.toString();
    }

    private String demoCreacionZapatillasBasketball() {
        StringBuilder sb = new StringBuilder();
        sb.append("🏀 DEMO 3: CREACIÓN DE ZAPATILLAS BASKETBALL\n");
        sb.append("   📝 Creando 'Jordan Air 35' con configuraciones de basketball...\n");

        try {
            ProductoCreationRequest request = ProductoCreationRequest.builder()
                    .conNombre("Jordan Air 35")
                    .conDescripcion("Zapatillas de basketball de alto rendimiento")
                    .conPrecio(new BigDecimal("180.00"))
                    .conCategoria("CALZADO")
                    .conTipo("BASKETBALL")
                    .conMarca("Jordan")
                    .conStockInicial(null) // Para ver stock automático
                    .conTalla("44")
                    .conColor("Negro/Rojo")
                    .activarImmediatamente(true)
                    .build();

            Producto producto = factoryManager.crearProducto(request);
            
            sb.append("   ✅ Producto creado exitosamente:\n");
            sb.append("      📦 Nombre: ").append(producto.getNombre()).append("\n");
            sb.append("      💰 Precio: $").append(producto.getPrecio()).append("\n");
            sb.append("      📊 Stock: ").append(producto.getStockDisponible()).append(" unidades (configurado para BASKETBALL)\n");
            sb.append("      🔧 Configuraciones de BASKETBALL aplicadas (soporte tobillo, etc.)\n");
            
        } catch (Exception e) {
            sb.append("   ❌ Error: ").append(e.getMessage()).append("\n");
        }
        
        sb.append("\n");
        return sb.toString();
    }

    private String demoValidaciones() {
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 DEMO 4: VALIDACIONES ESPECIALIZADAS\n");

        // Test 1: Validación exitosa
        ProductoCreationRequest requestValido = ProductoCreationRequest.builder()
                .conNombre("Producto Válido")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conTalla("42")
                .build();

        ProductoFactoryManager.ValidationResult resultado1 = 
            factoryManager.validarPeticion(requestValido);
        
        sb.append("   ✅ Validación exitosa: ").append(resultado1.getMensaje()).append("\n");

        // Test 2: Talla inválida
        ProductoCreationRequest requestTallaInvalida = ProductoCreationRequest.builder()
                .conNombre("Test Talla Inválida")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("RUNNING")
                .conTalla("99") // Talla inválida
                .build();

        ProductoFactoryManager.ValidationResult resultado2 = 
            factoryManager.validarPeticion(requestTallaInvalida);
        
        sb.append("   ❌ Error talla inválida: ").append(resultado2.getMensaje()).append("\n");

        // Test 3: Tipo no soportado
        ProductoCreationRequest requestTipoInvalido = ProductoCreationRequest.builder()
                .conNombre("Test Tipo Inválido")
                .conPrecio(new BigDecimal("100.00"))
                .conCategoria("CALZADO")
                .conTipo("VOLEIBOL") // Tipo no soportado
                .build();

        ProductoFactoryManager.ValidationResult resultado3 = 
            factoryManager.validarPeticion(requestTipoInvalido);
        
        sb.append("   ❌ Error tipo no soportado: ").append(resultado3.getMensaje()).append("\n");

        sb.append("   📊 Sistema de validaciones funcionando correctamente\n\n");
        
        return sb.toString();
    }

    /**
     * Demo específico para mostrar las diferencias de configuración por tipo.
     */
    public String compararConfiguracionesPorTipo() {
        StringBuilder sb = new StringBuilder();
        sb.append("⚙️ COMPARACIÓN DE CONFIGURACIONES POR TIPO:\n\n");

        // Crear productos de diferentes tipos para comparar
        String[] tipos = {"RUNNING", "FUTBOL", "BASKETBALL", "HIKING"};
        
        for (String tipo : tipos) {
            try {
                ProductoCreationRequest request = ProductoCreationRequest.builder()
                        .conNombre("Test " + tipo)
                        .conPrecio(new BigDecimal("100.00"))
                        .conCategoria("CALZADO")
                        .conTipo(tipo)
                        .conMarca("TestMarca")
                        .build();

                Producto producto = factoryManager.crearProducto(request);
                
                sb.append("🔸 ").append(tipo).append(":\n");
                sb.append("   📊 Stock automático: ").append(producto.getStockDisponible()).append(" unidades\n");
                sb.append("   📝 Descripción: ").append(producto.getDescripcion()).append("\n");
                sb.append("\n");
                
            } catch (Exception e) {
                sb.append("🔸 ").append(tipo).append(": ❌ Error - ").append(e.getMessage()).append("\n\n");
            }
        }
        
        return sb.toString();
    }
}
