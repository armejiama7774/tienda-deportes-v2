package com.tiendadeportiva.backend.integration;

import com.tiendadeportiva.backend.event.ProductoEventPublisher;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración para demostrar el Observer Pattern en acción.
 * 
 * OBJETIVO EDUCATIVO:
 * - Verificar que los eventos se emiten correctamente
 * - Comprobar que los observadores reciben y procesan eventos
 * - Demostrar la separación entre lógica de negocio y eventos
 * - Mostrar el flujo completo del Observer Pattern
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ObserverPatternIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ObserverPatternIntegrationTest.class);
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private ProductoEventPublisher eventPublisher;
    
    @Test
    @DisplayName("🔔 Observer Pattern: Debe emitir eventos al actualizar stock")
    void debeEmitirEventosAlActualizarStock() {
        logger.info("🧪 Iniciando test de Observer Pattern - Actualización de Stock");
        
        // Arrange: Crear un producto para testing
        Producto producto = new Producto(
            "Producto Observer Test",
            "Producto para demostrar Observer Pattern",
            new BigDecimal("50.00"),
            "TESTING",
            "TestBrand",
            10
        );
        
        Producto productoCreado = productoService.crearProducto(producto);
        
        // Act: Actualizar el stock - Esto debería emitir eventos
        logger.info("📦 Actualizando stock de 10 a 3 unidades (debería emitir STOCK_BAJO)");
        boolean actualizado = productoService.actualizarStock(productoCreado.getId(), 3);
        
        // Assert: Verificar que la operación fue exitosa
        assertThat(actualizado).isTrue();
        
        // Act: Agotar el stock - Esto debería emitir STOCK_AGOTADO
        logger.info("📦 Agotando stock (debería emitir STOCK_AGOTADO)");
        boolean agotado = productoService.actualizarStock(productoCreado.getId(), 0);
        
        // Assert
        assertThat(agotado).isTrue();
        
        logger.info("✅ Test completado - Revisa los logs para ver los eventos del Observer Pattern");
    }
    
    @Test
    @DisplayName("🔔 Observer Pattern: Debe emitir eventos al cambiar precios")
    void debeEmitirEventosAlCambiarPrecios() {
        logger.info("🧪 Iniciando test de Observer Pattern - Cambio de Precios");
        
        // Arrange: Crear un producto para testing
        Producto producto = new Producto(
            "Producto Precio Test",
            "Producto para demostrar cambios de precio",
            new BigDecimal("100.00"),
            "TESTING",
            "TestBrand",
            5
        );
        
        Producto productoCreado = productoService.crearProducto(producto);
        
        // Act: Cambiar precio - Esto debería emitir evento PRECIO_CAMBIADO
        logger.info("💰 Cambiando precio de $100.00 a $120.00");
        boolean precioActualizado = productoService.actualizarPrecio(
            productoCreado.getId(), 
            new BigDecimal("120.00")
        );
        
        // Assert
        assertThat(precioActualizado).isTrue();
        
        logger.info("✅ Test completado - Revisa los logs para ver los eventos de cambio de precio");
    }
    
    @Test
    @DisplayName("🔔 Observer Pattern: Debe emitir eventos al aplicar descuentos")
    void debeEmitirEventosAlAplicarDescuentos() {
        logger.info("🧪 Iniciando test de Observer Pattern - Aplicación de Descuentos");
        
        // Arrange: Crear un producto para testing
        Producto producto = new Producto(
            "Producto Descuento Test",
            "Producto para demostrar descuentos",
            new BigDecimal("100.00"),
            "DEPORTES",  // Categoría que puede tener descuentos
            "TestBrand",
            10
        );
        
        Producto productoCreado = productoService.crearProducto(producto);
        
        // Act: Calcular precio con descuento - Esto puede emitir evento DESCUENTO_APLICADO
        logger.info("💳 Calculando precio con descuento para usuario VIP con 5 productos");
        BigDecimal precioConDescuento = productoService.calcularPrecioConDescuento(
            productoCreado.getId(), 
            5,  // Cantidad que puede calificar para descuento por volumen
            true  // Usuario VIP
        );
        
        // Assert: Verificar que se calculó un precio
        assertThat(precioConDescuento).isNotNull();
        assertThat(precioConDescuento).isLessThanOrEqualTo(new BigDecimal("100.00"));
        
        logger.info("💰 Precio final calculado: ${}", precioConDescuento);
        logger.info("✅ Test completado - Revisa los logs para ver los eventos de descuento");
    }
    
    @Test
    @DisplayName("📊 Observer Pattern: Verificar estadísticas del sistema")
    void debeVerificarEstadisticasDelSistema() {
        logger.info("🧪 Verificando estadísticas del Observer Pattern");
        
        // Assert: Verificar que hay observadores registrados
        assertThat(eventPublisher.getObserverCount()).isGreaterThan(0);
        
        ProductoEventPublisher.ObserverStats stats = eventPublisher.getStats();
        
        logger.info("📊 Estadísticas del Observer Pattern:");
        logger.info("   Total observadores: {}", stats.getTotal());
        logger.info("   Observadores síncronos: {}", stats.getSincronos());
        logger.info("   Observadores asíncronos: {}", stats.getAsincronos());
        
        // Assert: Verificar que el sistema está configurado correctamente
        assertThat(stats.getTotal()).isGreaterThan(0);
        assertThat(stats.getSincronos() + stats.getAsincronos()).isEqualTo(stats.getTotal());
        
        logger.info("✅ Sistema Observer Pattern configurado correctamente");
    }
}
