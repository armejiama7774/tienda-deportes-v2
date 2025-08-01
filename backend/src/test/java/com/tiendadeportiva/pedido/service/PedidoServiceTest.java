package com.tiendadeportiva.pedido.service;

import com.tiendadeportiva.pedido.model.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PedidoService.
 * 
 * BOUNDED CONTEXT: Pedido
 * - Verifica funcionalidad básica del dominio Pedido
 * - Tests unitarios sin dependencias externas
 * - Preparación para testing de microservicios
 */
@DisplayName("🧪 Pedido Service - Tests Unitarios")
class PedidoServiceTest {

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService();
    }

    @Test
    @DisplayName("✅ Debe crear pedido correctamente")
    void debeCrearPedidoCorrectamente() {
        // Given
        Long usuarioId = 1L;
        
        // When
        Pedido pedido = pedidoService.crearPedido(usuarioId);
        
        // Then
        assertNotNull(pedido);
        assertNotNull(pedido.getId());
        assertEquals(usuarioId, pedido.getUsuarioId());
        assertEquals(Pedido.EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(BigDecimal.ZERO, pedido.getTotal());
        assertEquals(BigDecimal.ZERO, pedido.getDescuentoAplicado());
        assertTrue(pedido.getItems().isEmpty());
        assertNotNull(pedido.getFechaCreacion());
    }

    @Test
    @DisplayName("✅ Debe obtener pedido por ID")
    void debeObtenerPedidoPorId() {
        // Given - Primero crear un pedido
        Long usuarioId = 1L;
        Pedido pedidoCreado = pedidoService.crearPedido(usuarioId);
        Long pedidoId = pedidoCreado.getId();
        
        // When
        Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(pedidoId);
        
        // Then
        assertTrue(pedidoOpt.isPresent());
        Pedido pedido = pedidoOpt.get();
        assertEquals(pedidoId, pedido.getId());
        assertEquals(usuarioId, pedido.getUsuarioId());
        assertNotNull(pedido.getUsuarioId());
    }

    @Test
    @DisplayName("✅ Debe agregar producto al carrito")
    void debeAgregarProductoAlCarrito() {
        // Given
        Long usuarioId = 1L;
        Long productoId = 100L;
        Integer cantidad = 2;
        
        // When
        Pedido carrito = pedidoService.agregarProductoAlCarrito(usuarioId, productoId, cantidad);
        
        // Then
        assertNotNull(carrito);
        assertEquals(1, carrito.getItems().size());
        assertEquals(cantidad, carrito.getCantidadTotalItems());
        assertTrue(carrito.getTotal().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(Pedido.EstadoPedido.PENDIENTE, carrito.getEstado());
    }

    @Test
    @DisplayName("✅ Debe actualizar cantidad en carrito")
    void debeActualizarCantidadEnCarrito() {
        // Given
        Long usuarioId = 1L;
        Long productoId = 100L;
        
        // Primero agregar producto
        Pedido carrito = pedidoService.agregarProductoAlCarrito(usuarioId, productoId, 1);
        BigDecimal totalInicial = carrito.getTotal();
        
        // When - Actualizar cantidad
        Pedido carritoActualizado = pedidoService.actualizarCantidadEnCarrito(usuarioId, productoId, 3);
        
        // Then
        assertNotNull(carritoActualizado);
        assertEquals(3, carritoActualizado.getCantidadTotalItems());
        assertTrue(carritoActualizado.getTotal().compareTo(totalInicial) > 0);
    }

    @Test
    @DisplayName("✅ Debe eliminar producto del carrito")
    void debeEliminarProductoDelCarrito() {
        // Given
        Long usuarioId = 1L;
        Long productoId = 100L;
        
        // Primero agregar producto
        pedidoService.agregarProductoAlCarrito(usuarioId, productoId, 2);
        
        // When - Eliminar producto
        Pedido carritoActualizado = pedidoService.eliminarProductoDelCarrito(usuarioId, productoId);
        
        // Then
        assertNotNull(carritoActualizado);
        assertEquals(0, carritoActualizado.getItems().size());
        assertEquals(0, carritoActualizado.getCantidadTotalItems());
    }

    @Test
    @DisplayName("✅ Debe confirmar pedido correctamente")
    void debeConfirmarPedido() {
        // Given
        Long usuarioId = 1L;
        Pedido pedido = pedidoService.crearPedido(usuarioId);
        Long pedidoId = pedido.getId();
        
        // When
        Pedido pedidoConfirmado = pedidoService.confirmarPedido(pedidoId);
        
        // Then
        assertNotNull(pedidoConfirmado);
        assertEquals(Pedido.EstadoPedido.CONFIRMADO, pedidoConfirmado.getEstado());
        assertNotNull(pedidoConfirmado.getFechaModificacion());
    }

    @Test
    @DisplayName("✅ Debe procesar pedido confirmado")
    void debeProcesarPedidoConfirmado() {
        // Given
        Long usuarioId = 1L;
        Pedido pedido = pedidoService.crearPedido(usuarioId);
        pedido = pedidoService.confirmarPedido(pedido.getId());
        
        // When
        Pedido pedidoProcesado = pedidoService.procesarPedido(pedido.getId());
        
        // Then
        assertNotNull(pedidoProcesado);
        assertEquals(Pedido.EstadoPedido.PROCESANDO, pedidoProcesado.getEstado());
    }

    @Test
    @DisplayName("✅ Debe completar pedido en procesamiento")
    void debeCompletarPedidoEnProcesamiento() {
        // Given
        Long usuarioId = 1L;
        Pedido pedido = pedidoService.crearPedido(usuarioId);
        pedido = pedidoService.confirmarPedido(pedido.getId());
        pedido = pedidoService.procesarPedido(pedido.getId());
        
        // When
        Pedido pedidoCompletado = pedidoService.completarPedido(pedido.getId());
        
        // Then
        assertNotNull(pedidoCompletado);
        assertEquals(Pedido.EstadoPedido.COMPLETADO, pedidoCompletado.getEstado());
        assertNotNull(pedidoCompletado.getFechaFinalizacion());
    }

    @Test
    @DisplayName("✅ Debe cancelar pedido con motivo")
    void debeCancelarPedidoConMotivo() {
        // Given
        Long usuarioId = 1L;
        Pedido pedido = pedidoService.crearPedido(usuarioId);
        String motivo = "Cambio de decisión del cliente";
        
        // When
        Pedido pedidoCancelado = pedidoService.cancelarPedido(pedido.getId(), motivo);
        
        // Then
        assertNotNull(pedidoCancelado);
        assertEquals(Pedido.EstadoPedido.CANCELADO, pedidoCancelado.getEstado());
        assertEquals(motivo, pedidoCancelado.getObservaciones());
    }

    @Test
    @DisplayName("❌ Debe fallar al procesar pedido en estado incorrecto")
    void debeFallarAlProcesarPedidoEnEstadoIncorrecto() {
        // Given
        Long usuarioId = 1L;
        Pedido pedido = pedidoService.crearPedido(usuarioId);
        // Pedido está PENDIENTE, no CONFIRMADO
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            pedidoService.procesarPedido(pedido.getId());
        });
    }
}
