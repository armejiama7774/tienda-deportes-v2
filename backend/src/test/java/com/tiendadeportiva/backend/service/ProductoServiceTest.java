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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoValido;
    private Producto productoInvalido;

    @BeforeEach
    void setUp() {
        // Arrange - Preparar datos de prueba
        productoValido = new Producto();
        productoValido.setId(null); // Para creación, el ID debe ser null
        productoValido.setNombre("Camiseta Deportiva");
        productoValido.setDescripcion("Camiseta para running");
        productoValido.setPrecio(new BigDecimal("25.99"));
        productoValido.setCategoria("Camisetas");
        productoValido.setMarca("Nike");
        productoValido.setStockDisponible(10);
        productoValido.setActivo(true);

        productoInvalido = new Producto();
        productoInvalido.setNombre("Producto Inválido");
        productoInvalido.setPrecio(new BigDecimal("-10.00")); // Precio negativo
        productoInvalido.setStockDisponible(-5); // Stock negativo
        productoInvalido.setCategoria("CategoriaInvalida");
    }

    @Test
    @DisplayName("Debe obtener todos los productos activos correctamente")
    void debeObtenerTodosLosProductosActivos() {
        // Arrange
        List<Producto> productosEsperados = Arrays.asList(productoValido);
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
        productoValido.setId(productId);  // ← ESTO faltaba
        productoValido.setActivo(true);   // ← Y esto también por seguridad
        
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
    @DisplayName("Debe retornar Optional vacío cuando el producto no existe")
    void debeRetornarOptionalVacioCuandoProductoNoExiste() {
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
    @DisplayName("Debe crear un producto válido correctamente")
    void debeCrearProductoValidoCorrectamente() {
        // Arrange
        when(productoRepository.existsByNombreAndMarcaAndActivoTrue(
                productoValido.getNombre(), productoValido.getMarca()))
                .thenReturn(false);
        
        // Simular que el repositorio asigna un ID al guardar
        Producto productoConId = new Producto();
        productoConId.setId(1L);
        productoConId.setNombre(productoValido.getNombre());
        productoConId.setDescripcion(productoValido.getDescripcion());
        productoConId.setPrecio(productoValido.getPrecio());
        productoConId.setCategoria(productoValido.getCategoria());
        productoConId.setMarca(productoValido.getMarca());
        productoConId.setStockDisponible(productoValido.getStockDisponible());
        productoConId.setActivo(productoValido.getActivo());
        
        when(productoRepository.save(productoValido))
                .thenReturn(productoConId);

        // Act
        Producto productoCreado = productoService.crearProducto(productoValido);

        // Assert
        assertThat(productoCreado)
                .isNotNull()
                .satisfies(p -> {
                    assertThat(p.getId()).isEqualTo(1L);
                    assertThat(p.getNombre()).isEqualTo("Camiseta Deportiva");
                    assertThat(p.getMarca()).isEqualTo("Nike");
                });
        
        verify(productoRepository).existsByNombreAndMarcaAndActivoTrue(
                productoValido.getNombre(), productoValido.getMarca());
        verify(productoRepository).save(productoValido);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el precio es inválido")
    void debeLanzarExcepcionCuandoPrecioEsInvalido() {
        // Arrange
        productoInvalido.setPrecio(new BigDecimal("-10.00"));

        // Act & Assert
        assertThatThrownBy(() -> productoService.crearProducto(productoInvalido))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("El precio debe ser mayor que 0")
                .satisfies(ex -> {
                    ProductoException productEx = (ProductoException) ex;
                    assertThat(productEx.getCodigo()).isEqualTo("PRECIO_INVALIDO");
                });

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el stock es negativo")
    void debeLanzarExcepcionCuandoStockEsNegativo() {
        // Arrange
        productoInvalido.setPrecio(new BigDecimal("25.99")); // Precio válido
        productoInvalido.setStockDisponible(-5); // Stock inválido

        // Act & Assert
        assertThatThrownBy(() -> productoService.crearProducto(productoInvalido))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("El stock no puede ser negativo")
                .satisfies(ex -> {
                    ProductoException productEx = (ProductoException) ex;
                    assertThat(productEx.getCodigo()).isEqualTo("STOCK_INVALIDO");
                });

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando ya existe un producto con mismo nombre y marca")
    void debeLanzarExcepcionCuandoProductoDuplicado() {
        // Arrange
        when(productoRepository.existsByNombreAndMarcaAndActivoTrue(
                productoValido.getNombre(), productoValido.getMarca()))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productoService.crearProducto(productoValido))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("Ya existe un producto activo")
                .satisfies(ex -> {
                    ProductoException productEx = (ProductoException) ex;
                    assertThat(productEx.getCodigo()).isEqualTo("PRODUCTO_DUPLICADO");
                });

        verify(productoRepository).existsByNombreAndMarcaAndActivoTrue(
                productoValido.getNombre(), productoValido.getMarca());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar stock correctamente cuando el producto existe")
    void debeActualizarStockCorrectamenteCuandoProductoExiste() {
        // Arrange
        Long productId = 1L;
        Integer nuevoStock = 15;
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
    @DisplayName("Debe lanzar excepción cuando se intenta actualizar stock de producto inexistente")
    void debeLanzarExcepcionCuandoActualizaStockDeProductoInexistente() {
        // Arrange
        Long productId = 999L;
        Integer nuevoStock = 15;
        when(productoRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productoService.actualizarStock(productId, nuevoStock))
                .isInstanceOf(ProductoNoEncontradoException.class)
                .hasMessageContaining("No se encontró el producto con ID: 999");

        verify(productoRepository).findById(productId);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando se intenta establecer stock negativo")
    void debeLanzarExcepcionCuandoStockNegativoEnActualizacion() {
        // Arrange
        Long productId = 1L;
        Integer stockNegativo = -5;

        // Act & Assert
        assertThatThrownBy(() -> productoService.actualizarStock(productId, stockNegativo))
                .isInstanceOf(ProductoException.class)
                .hasMessageContaining("El stock no puede ser negativo")
                .satisfies(ex -> {
                    ProductoException productEx = (ProductoException) ex;
                    assertThat(productEx.getCodigo()).isEqualTo("STOCK_INVALIDO");
                });

        verify(productoRepository, never()).findById(any());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("No debe obtener un producto eliminado (inactivo)")
    void noDebeObtenerProductoEliminado() {
        // Arrange
        Long productId = 1L;
        Producto productoEliminado = new Producto();
        productoEliminado.setId(productId);
        productoEliminado.setNombre("Producto Eliminado");
        productoEliminado.setActivo(false); // Producto eliminado
        
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
        when(productoRepository.existsByNombreAndActivoTrue(productoValido.getNombre()))
                .thenReturn(false);

        // Act
        Producto resultado = productoService.crearProducto(productoValido);

        // Assert
        assertThat(resultado).isNotNull();
        verify(productoRepository).save(productoValido);
        
        // 🚨 PROBLEMA: ¿Cómo verificamos que las notificaciones se enviaron?
        // No podemos hacer verify() porque son métodos privados
        // No podemos mockear servicios externos porque están hardcodeados
        // Este test pasa, pero ¿realmente funcionan las notificaciones?
        
        // TODO: Necesitamos refactorizar para poder testear notificaciones por separado
    }
}
