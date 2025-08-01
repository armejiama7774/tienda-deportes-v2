package com.tiendadeportiva.backend.demo;

import com.tiendadeportiva.backend.event.ProductoEventPublisher;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

/**
 * Demo ejecutable del Observer Pattern como test de Spring Boot.
 * 
 * EJECUTAR CON:
 * mvnw test -Dtest=ObserverPatternLiveDemo
 * 
 * Este test demuestra el Observer Pattern en acción con casos reales.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ObserverPatternLiveDemo {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoEventPublisher eventPublisher;

    @Test
    public void demonstrarObserverPattern() throws Exception {
        System.out.println("\n🎬 =====================================================");
        System.out.println("🎬 DEMO DEL OBSERVER PATTERN - TIENDA DEPORTES");
        System.out.println("🎬 =====================================================\n");

        mostrarEstadisticasIniciales();
        demoCreacionProducto();
        demoCambiosDeStock();
        demoCambiosDePrecios();
        demoAplicacionDescuentos();

        System.out.println("\n🎬 =====================================================");
        System.out.println("🎬 DEMO COMPLETADO - Observer Pattern en Acción");
        System.out.println("🎬 =====================================================\n");
    }

    private void mostrarEstadisticasIniciales() {
        System.out.println("📊 ESTADÍSTICAS INICIALES DEL OBSERVER PATTERN");
        ProductoEventPublisher.ObserverStats stats = eventPublisher.getStats();
        System.out.println("   🔔 Total observadores registrados: " + stats.getTotal());
        System.out.println("   ⚡ Observadores síncronos: " + stats.getSincronos());
        System.out.println("   🌪️ Observadores asíncronos: " + stats.getAsincronos());
        System.out.println();
    }

    private void demoCreacionProducto() throws Exception {
        System.out.println("🏗️ DEMO 1: CREACIÓN DE PRODUCTO");
        System.out.println("   📝 Creando producto 'Zapatillas Running Pro'...");

        Producto producto = new Producto(
            "Zapatillas Running Pro",
            "Zapatillas profesionales para running con tecnología avanzada",
            new BigDecimal("150.00"),
            "CALZADO",
            "NikePro",
            25
        );

        Producto productoCreado = productoService.crearProducto(producto);
        System.out.println("   ✅ Producto creado con ID: " + productoCreado.getId());
        System.out.println("   🔔 Observa los logs de los observadores arriba...");
        Thread.sleep(500);
        System.out.println();
    }

    private void demoCambiosDeStock() throws Exception {
        System.out.println("📦 DEMO 2: CAMBIOS DE STOCK");

        // Crear producto para demo de stock
        Producto producto = new Producto(
            "Camiseta Elite",
            "Camiseta de entrenamiento élite",
            new BigDecimal("45.00"),
            "ROPA",
            "Adidas",
            15
        );

        Producto productoCreado = productoService.crearProducto(producto);
        Thread.sleep(300);

        System.out.println("   📦 Reduciendo stock a 4 unidades (debería disparar STOCK_BAJO)...");
        productoService.actualizarStock(productoCreado.getId(), 4);
        Thread.sleep(300);

        System.out.println("   📦 Agotando stock (debería disparar STOCK_AGOTADO)...");
        productoService.actualizarStock(productoCreado.getId(), 0);
        Thread.sleep(300);

        System.out.println("   📦 Reponiendo stock a 10 unidades...");
        productoService.actualizarStock(productoCreado.getId(), 10);
        Thread.sleep(300);

        System.out.println("   🔔 Observa cómo reaccionan los observadores a cada cambio...");
        System.out.println();
    }

    private void demoCambiosDePrecios() throws Exception {
        System.out.println("💰 DEMO 3: CAMBIOS DE PRECIOS");

        // Crear producto para demo de precios
        Producto producto = new Producto(
            "Mochila Deportiva",
            "Mochila ergonómica para deportes",
            new BigDecimal("80.00"),
            "ACCESORIOS",
            "Under Armour",
            12
        );

        Producto productoCreado = productoService.crearProducto(producto);
        Thread.sleep(300);

        System.out.println("   💰 Cambiando precio de $80.00 a $95.00...");
        productoService.actualizarPrecio(productoCreado.getId(), new BigDecimal("95.00"));
        Thread.sleep(300);

        System.out.println("   💰 Aplicando descuento: cambiando precio a $65.00...");
        productoService.actualizarPrecio(productoCreado.getId(), new BigDecimal("65.00"));
        Thread.sleep(300);

        System.out.println("   🔔 Los observadores registran cada cambio de precio...");
        System.out.println();
    }

    private void demoAplicacionDescuentos() throws Exception {
        System.out.println("🏷️ DEMO 4: APLICACIÓN DE DESCUENTOS");

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

        System.out.println("   💳 Calculando precio para usuario regular con 2 productos...");
        BigDecimal precio1 = productoService.calcularPrecioConDescuento(
            productoCreado.getId(), 2, false
        );
        System.out.println("   💰 Precio calculado: $" + precio1);
        Thread.sleep(300);

        System.out.println("   👑 Calculando precio para usuario VIP con 6 productos...");
        BigDecimal precio2 = productoService.calcularPrecioConDescuento(
            productoCreado.getId(), 6, true
        );
        System.out.println("   💰 Precio calculado: $" + precio2);
        Thread.sleep(300);

        System.out.println("   🔔 Observa los eventos de descuentos aplicados...");
        System.out.println();
    }
}
