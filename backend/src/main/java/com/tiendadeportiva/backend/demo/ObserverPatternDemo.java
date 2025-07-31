package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.event.ProductoEventPublisher;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Demo ejecutable del Observer Pattern para mostrar eventos en tiempo real.
 * 
 * Para ejecutar este demo, usar:
 * mvnw spring-boot:run -Dspring-boot.run.arguments="--demo.observer.enabled=true"
 * 
 * FUNCIONALIDADES DEMOSTRADAS:
 * - Emisión automática de eventos durante operaciones CRUD
 * - Procesamiento por múltiples observadores (StockObserver, PrecioObserver, LoggingObserver)
 * - Ejecución síncrona y asíncrona de observadores
 * - Filtrado de eventos por tipo e interés
 * - Manejo robusto de errores sin interrumpir el flujo
 */
@Component
@ConditionalOnProperty(value = "demo.observer.enabled", havingValue = "true")
public class ObserverPatternDemo implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ObserverPatternDemo.class);
    
    private final ProductoService productoService;
    private final ProductoEventPublisher eventPublisher;
    
    public ObserverPatternDemo(ProductoService productoService, ProductoEventPublisher eventPublisher) {
        this.productoService = productoService;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("🎬 =====================================================");
        logger.info("🎬 DEMO DEL OBSERVER PATTERN - TIENDA DEPORTES");
        logger.info("🎬 =====================================================");
        
        mostrarEstadisticasIniciales();
        demoCreacionProducto();
        demoCambiosDeStock();
        demoCambiosDePrecios();
        demoAplicacionDescuentos();
        
        logger.info("🎬 =====================================================");
        logger.info("🎬 DEMO COMPLETADO - Observer Pattern en Acción");
        logger.info("🎬 =====================================================");
    }
    
    private void mostrarEstadisticasIniciales() {
        logger.info("📊 ESTADÍSTICAS INICIALES DEL OBSERVER PATTERN");
        ProductoEventPublisher.ObserverStats stats = eventPublisher.getStats();
        logger.info("   🔔 Total observadores registrados: {}", stats.getTotal());
        logger.info("   ⚡ Observadores síncronos: {}", stats.getSincronos());
        logger.info("   🌪️ Observadores asíncronos: {}", stats.getAsincronos());
        logger.info("");
    }
    
    private void demoCreacionProducto() {
        logger.info("🏗️ DEMO 1: CREACIÓN DE PRODUCTO");
        logger.info("   📝 Creando producto 'Zapatillas Running Pro'...");
        
        Producto producto = new Producto(
            "Zapatillas Running Pro",
            "Zapatillas profesionales para running con tecnología avanzada",
            new BigDecimal("150.00"),
            "CALZADO",
            "NikePro",
            25
        );
        
        Producto productoCreado = productoService.crearProducto(producto);
        logger.info("   ✅ Producto creado con ID: {}", productoCreado.getId());
        logger.info("   🔔 Observa los eventos emitidos por los observadores...");
        logger.info("");
        
        // Pausa para que se vean los logs de los observadores asíncronos
        try { Thread.sleep(500); } catch (InterruptedException e) { /* ignore */ }
    }
    
    private void demoCambiosDeStock() throws Exception {
        logger.info("📦 DEMO 2: CAMBIOS DE STOCK Y ALERTAS");
        
        // Crear producto para demo de stock
        Producto producto = new Producto(
            "Balón de Fútbol",
            "Balón oficial de fútbol",
            new BigDecimal("25.00"),
            "DEPORTES",
            "Adidas",
            15
        );
        
        Producto productoCreado = productoService.crearProducto(producto);
        Thread.sleep(300);
        
        logger.info("   📉 Reduciendo stock a 4 unidades (debería activar alerta STOCK_BAJO)...");
        productoService.actualizarStock(productoCreado.getId(), 4);
        Thread.sleep(300);
        
        logger.info("   📉 Reduciendo stock a 1 unidad (debería activar alerta STOCK_CRÍTICO)...");
        productoService.actualizarStock(productoCreado.getId(), 1);
        Thread.sleep(300);
        
        logger.info("   📉 Agotando stock completamente (debería activar alerta STOCK_AGOTADO)...");
        productoService.actualizarStock(productoCreado.getId(), 0);
        Thread.sleep(300);
        
        logger.info("   🔔 Observa cómo StockObserver emite diferentes alertas según el nivel...");
        logger.info("");
    }
    
    private void demoCambiosDePrecios() throws Exception {
        logger.info("💰 DEMO 3: CAMBIOS DE PRECIOS Y ANÁLISIS");
        
        // Crear producto para demo de precios
        Producto producto = new Producto(
            "Raqueta de Tenis",
            "Raqueta profesional de tenis",
            new BigDecimal("80.00"),
            "DEPORTES",
            "Wilson",
            10
        );
        
        Producto productoCreado = productoService.crearProducto(producto);
        Thread.sleep(300);
        
        logger.info("   📈 Aumentando precio de $80.00 a $95.00 (aumento del 18.75%)...");
        productoService.actualizarPrecio(productoCreado.getId(), new BigDecimal("95.00"));
        Thread.sleep(300);
        
        logger.info("   📉 Bajando precio de $95.00 a $65.00 (disminución del 31.6%)...");
        productoService.actualizarPrecio(productoCreado.getId(), new BigDecimal("65.00"));
        Thread.sleep(300);
        
        logger.info("   🔔 Observa cómo PrecioObserver analiza y clasifica los cambios...");
        logger.info("");
    }
    
    private void demoAplicacionDescuentos() throws Exception {
        logger.info("🏷️ DEMO 4: APLICACIÓN DE DESCUENTOS");
        
        // Crear producto para demo de descuentos
        Producto producto = new Producto(
            "Conjunto Deportivo",
            "Conjunto completo para entrenamiento",
            new BigDecimal("120.00"),
            "ROPA",
            "Nike",
            20
        );
        
        Producto productoCreado = productoService.crearProducto(producto);
        Thread.sleep(300);
        
        logger.info("   💳 Calculando precio para usuario regular con 2 productos...");
        BigDecimal precio1 = productoService.calcularPrecioConDescuento(
            productoCreado.getId(), 2, false
        );
        logger.info("   💰 Precio calculado: ${}", precio1);
        Thread.sleep(300);
        
        logger.info("   👑 Calculando precio para usuario VIP con 6 productos...");
        BigDecimal precio2 = productoService.calcularPrecioConDescuento(
            productoCreado.getId(), 6, true
        );
        logger.info("   💰 Precio calculado: ${}", precio2);
        Thread.sleep(300);
        
        logger.info("   🔔 Observa los eventos de descuentos aplicados...");
        logger.info("");
    }
}
