package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.command.CommandExecutionException; // ✅ IMPORT AGREGADO
import com.tiendadeportiva.backend.command.CommandHandler;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService - Tests Unitarios")
class ProductoServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceTest.class);

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private DescuentoService descuentoService;

    @Mock
    private CommandHandler commandHandler;

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
        productoEliminado.setActivo(false);
        
        when(productoRepository.findById(productId))
            .thenReturn(Optional.of(productoEliminado));
        
        // Act
        Optional<Producto> resultado = productoService.obtenerProductoPorId(productId);
        
        // Assert
        assertThat(resultado).isEmpty();
        verify(productoRepository).findById(productId);
    }

    @Test
    @DisplayName("Debe crear producto correctamente usando Command Pattern")
    void debeCrearProductoConNotificaciones() {
        // Arrange
        productoValido.setId(null);
        
        // ✅ CORRECCIÓN: Mock sin excepción checked
        try {
            when(commandHandler.handle(any())).thenReturn(productoValido);
        } catch (CommandExecutionException e) {
            // Este catch nunca se ejecutará en mocks, pero satisface al compilador
            throw new RuntimeException(e);
        }

        // Act
        Producto resultado = productoService.crearProducto(productoValido);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo(productoValido.getNombre());
        
        try {
            verify(commandHandler).handle(any());
        } catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }
        
        logger.info("✅ Test actualizado: ProductoService usa Command Pattern correctamente");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando comando falla")
    void debeLanzarExcepcionCuandoComandoFalla() {
        // Arrange
        productoValido.setId(null);
        
        // ✅ CORRECCIÓN: Mock para lanzar excepción
        try {
            when(commandHandler.handle(any()))
                    .thenThrow(new CommandExecutionException(
                        "CrearProductoCommand", 
                        "PRODUCTO_DUPLICADO", 
                        "Producto ya existe"
                    ));
        } catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        ProductoException exception = assertThrows(ProductoException.class, 
                () -> productoService.crearProducto(productoValido));
        
        assertThat(exception.getCodigo()).isEqualTo("PRODUCTO_DUPLICADO");
        assertThat(exception.getMessage()).contains("Producto ya existe");
        
        try {
            verify(commandHandler).handle(any());
        } catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }
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
        ProductoException exception = assertThrows(ProductoException.class,
                () -> productoService.actualizarStock(productId, stockNegativo));

        assertThat(exception.getCodigo()).isEqualTo("STOCK_INVALIDO");
        assertThat(exception.getMessage()).contains("negativo");
        
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

    @Test
    @DisplayName("Debe buscar productos por categoría correctamente")
    void debeBuscarProductosPorCategoria() {
        // Arrange
        String categoria = "Camisetas";
        List<Producto> productosEsperados = List.of(productoValido);
        when(productoRepository.findByCategoriaAndActivoTrue(categoria))
                .thenReturn(productosEsperados);

        // Act
        List<Producto> productosObtenidos = productoService.buscarPorCategoria(categoria);

        // Assert
        assertThat(productosObtenidos)
                .isNotNull()
                .hasSize(1)
                .containsExactly(productoValido);
        
        verify(productoRepository).findByCategoriaAndActivoTrue(categoria);
    }

    @Test
    @DisplayName("Debe buscar productos por marca correctamente")
    void debeBuscarProductosPorMarca() {
        // Arrange
        String marca = "Nike";
        List<Producto> productosEsperados = List.of(productoValido);
        when(productoRepository.findByMarcaAndActivoTrue(marca))
                .thenReturn(productosEsperados);

        // Act
        List<Producto> productosObtenidos = productoService.buscarPorMarca(marca);

        // Assert
        assertThat(productosObtenidos)
                .isNotNull()
                .hasSize(1)
                .containsExactly(productoValido);
        
        verify(productoRepository).findByMarcaAndActivoTrue(marca);
    }

    @Test
    @DisplayName("Debe buscar productos por nombre correctamente")
    void debeBuscarProductosPorNombre() {
        // Arrange
        String nombre = "Camiseta";
        List<Producto> productosEsperados = List.of(productoValido);
        when(productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre))
                .thenReturn(productosEsperados);

        // Act
        List<Producto> productosObtenidos = productoService.buscarPorNombre(nombre);

        // Assert
        assertThat(productosObtenidos)
                .isNotNull()
                .hasSize(1)
                .containsExactly(productoValido);
        
        verify(productoRepository).findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }
}
