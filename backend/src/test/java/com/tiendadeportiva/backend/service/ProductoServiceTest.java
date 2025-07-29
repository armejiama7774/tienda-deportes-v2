package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.exception.ProductoException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductoService.
 * 
 * Implementa las mejores prácticas de testing:
 * - Tests unitarios aislados usando mocks
 * - Naming descriptivo con @DisplayName
 * - AAA pattern (Arrange, Act, Assert)
 * - Testing de casos positivos y negativos
 * - Verificación de interacciones con mocks
 * - Uso de AssertJ para assertions más legibles
 * 
 * Estos tests sirven como:
 * - Documentación viva del comportamiento esperado
 * - Red de seguridad para refactorings
 * - Feedback rápido durante desarrollo
 * - Validación de principios SOLID
 * 
 * @author Equipo Desarrollo
 * @version 1.0
 * @since Fase 1 - Monolito Modular con SOLID
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService - Tests Unitarios")
class ProductoServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceTest.class);

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher; // ✅ AGREGAR MOCK

    @Mock
    private DescuentoService descuentoService; // ✅ AGREGAR MOCK

    @InjectMocks
    private ProductoService productoService;

    private Producto productoValido;

    @BeforeEach
    void setUp() {
        productoValido = new Producto();
        productoValido.setId(1L);
        productoValido.setNombre("Camiseta Deportiva");
        productoValido.setDescripcion("Camiseta de alta calidad");
        productoValido.setPrecio(new BigDecimal("29.99"));
        productoValido.setCategoria("Camisetas");
        productoValido.setMarca("Nike");
        productoValido.setStockDisponible(10);
        productoValido.setActivo(true);
        productoValido.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    @DisplayName("Debe obtener todos los productos activos correctamente")
    void debeObtenerTodosLosProductosActivos() {
        // Arrange
        List<Producto> productosEsperados = List.of(productoValido);
        when(productoRepository.findByActivoTrueOrderByFechaCreacionDesc())
                .thenReturn(productosEsperados);

        // Act
        List<Producto> productosObtenidos = productoService.obtenerTodosLosProductos();

        // Assert
        assertThat(productosObtenidos)
                .isNotNull()
                .hasSize(1)
                .containsExactly(productoValido);
        
        verify(productoRepository).findByActivoTrueOrderByFechaCreacionDesc();
    }

    @Test
    @DisplayName("Debe obtener un producto por ID cuando existe")
    void debeObtenerProductoPorIdCuandoExiste() {
        // Arrange
        Long productId = 1L;
        productoValido.setId(productId);
        productoValido.setActivo(true);
        
        when(productoRepository.findById(productId))
                .thenReturn(Optional.of(productoValido));

        // Act
        Optional<Producto> productoObtenido = productoService.obtenerProductoPorId(productId);

        // Assert
        assertThat(productoObtenido)
                .isPresent()
                .contains(productoValido);
        
        verify(productoRepository).findById(productId);
    }

    @Test
    @DisplayName("No debe obtener un producto eliminado (inactivo)")
    void noDebeObtenerProductoEliminado() {
        // Arrange
        Long productId = 1L;
        Producto productoEliminado = new Producto();
        productoEliminado.setId(productId);
        productoEliminado.setNombre("Producto Eliminado");
        productoEliminado.setActivo(false); // Producto eliminado (soft delete)
        
        when(productoRepository.findById(productId))
            .thenReturn(Optional.of(productoEliminado));
        
        // Act
        Optional<Producto> resultado = productoService.obtenerProductoPorId(productId);
        
        // Assert
        assertThat(resultado).isEmpty();
        verify(productoRepository).findById(productId);
    }

    @Test
    @DisplayName("Debe crear producto correctamente CON notificaciones")
    void debeCrearProductoConNotificaciones() {
        // Arrange
        productoValido.setId(null); // Para simular creación
        
        when(productoRepository.save(any(Producto.class)))
                .thenReturn(productoValido);
        when(productoRepository.existsByNombreAndMarcaAndActivoTrue(
                productoValido.getNombre(), productoValido.getMarca()))
                .thenReturn(false);
        
        // ✅ NUEVO: Mock del DescuentoService
        DescuentoService.DescuentoInfo descuentoInfo = DescuentoService.DescuentoInfo.sinDescuentos(productoValido.getPrecio());
        when(descuentoService.aplicarDescuentosAutomaticos(any(Producto.class)))
                .thenReturn(descuentoInfo);

        // Act
        Producto resultado = productoService.crearProducto(productoValido);

        // Assert
        assertThat(resultado).isNotNull();
        verify(productoRepository).save(productoValido);
        verify(productoRepository).existsByNombreAndMarcaAndActivoTrue(
                productoValido.getNombre(), productoValido.getMarca());
        verify(descuentoService).aplicarDescuentosAutomaticos(any(Producto.class));
        verify(applicationEventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando producto ya existe (por nombre y marca)")
    void debeLanzarExcepcionCuandoProductoYaExiste() {
        // Arrange
        String nombreExistente = "Camiseta Deportiva";
        String marcaExistente = "Nike";
        
        productoValido.setId(null); // Simular producto nuevo
        productoValido.setNombre(nombreExistente);
        productoValido.setMarca(marcaExistente);
        
        when(productoRepository.existsByNombreAndMarcaAndActivoTrue(nombreExistente, marcaExistente))
                .thenReturn(true);

        // Act & Assert
        ProductoException exception = assertThrows(ProductoException.class, 
                () -> productoService.crearProducto(productoValido));
        
        assertThat(exception.getCodigo()).isEqualTo("PRODUCTO_DUPLICADO");
        assertThat(exception.getMessage()).contains(nombreExistente);
        assertThat(exception.getMessage()).contains(marcaExistente);
        
        // Verificar que no se intentó guardar
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el producto no existe o está inactivo")
    void debeRetornarOptionalVacioCuandoProductoNoExisteOEstaInactivo() {
        // Arrange
        Long productId = 999L;
        when(productoRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act
        Optional<Producto> productoObtenido = productoService.obtenerProductoPorId(productId);

        // Assert
        assertThat(productoObtenido).isEmpty();
        verify(productoRepository).findById(productId);
    }

    @Test
    @DisplayName("Debe actualizar stock correctamente")
    void debeActualizarStockCorrectamente() {
        // Arrange
        Long productId = 1L;
        Integer nuevoStock = 25;
        
        when(productoRepository.findById(productId))
                .thenReturn(Optional.of(productoValido));
        when(productoRepository.save(any(Producto.class)))
                .thenReturn(productoValido);

        // Act
        boolean resultado = productoService.actualizarStock(productId, nuevoStock);

        // Assert
        assertThat(resultado).isTrue();
        assertThat(productoValido.getStockDisponible()).isEqualTo(nuevoStock);
        verify(productoRepository).findById(productId);
        verify(productoRepository).save(productoValido);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el stock es negativo")
    void debeLanzarExcepcionCuandoStockEsNegativo() {
        // Arrange
        Long productId = 1L;
        Integer stockNegativo = -5;

        // Act & Assert
        // ✅ CORRECCIÓN: Usar ProductoException en lugar de StockInvalidoException
        ProductoException exception = assertThrows(ProductoException.class,
                () -> productoService.actualizarStock(productId, stockNegativo));

        assertThat(exception.getCodigo()).isEqualTo("STOCK_INVALIDO");
        assertThat(exception.getMessage()).contains("negativo");
        
        // Verificar que no se accedió al repositorio
        verify(productoRepository, never()).findById(any());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando producto no existe para actualizar stock")
    void debeLanzarExcepcionCuandoProductoNoExisteParaActualizarStock() {
        // Arrange
        Long productIdInexistente = 999L;
        Integer nuevoStock = 10;
        
        when(productoRepository.findById(productIdInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert
        ProductoNoEncontradoException exception = assertThrows(ProductoNoEncontradoException.class,
                () -> productoService.actualizarStock(productIdInexistente, nuevoStock));

        verify(productoRepository).findById(productIdInexistente);
        verify(productoRepository, never()).save(any());
    }
}
