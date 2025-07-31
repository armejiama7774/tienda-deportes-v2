package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.command.CommandExecutionException; // ✅ IMPORT AGREGADO
import com.tiendadeportiva.backend.command.CommandHandler;
import com.tiendadeportiva.backend.exception.ProductoException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import com.tiendadeportiva.backend.service.descuento.DescuentoContexto;
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
        productoValido.setActivo(Boolean.TRUE); // ✅ CORRECCIÓN: Boolean wrapper
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
        productoValido.setActivo(Boolean.TRUE); // ✅ CORRECCIÓN: Boolean wrapper
        
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
        productoEliminado.setActivo(Boolean.FALSE); // ✅ CORRECCIÓN: Boolean wrapper
        
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

    @Test
    @DisplayName("Debe actualizar producto correctamente usando Command Pattern")
    void debeActualizarProductoCorrectamente() {
        // Arrange
        Long productoId = 1L;
        Producto datosActualizacion = new Producto();
        datosActualizacion.setNombre("Nombre Actualizado");
        datosActualizacion.setPrecio(new BigDecimal("39.99"));
        
        try {
            when(commandHandler.handle(any())).thenReturn(productoValido);
        } catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }

        // Act
        Optional<Producto> resultado = productoService.actualizarProducto(productoId, datosActualizacion);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo(productoValido.getNombre());
        
        try {
            verify(commandHandler).handle(any());
        } catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Debe eliminar producto correctamente usando Command Pattern")
    void debeEliminarProductoCorrectamente() {
        // Arrange
        Long productoId = 1L;
        
        try {
            when(commandHandler.handle(any())).thenReturn(true);
        } catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }

        // Act
        boolean resultado = productoService.eliminarProducto(productoId);

        // Assert
        assertThat(resultado).isTrue();
        
        try {
            verify(commandHandler).handle(any());
        } catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    // =============================================
    // TESTS PARA NUEVA FUNCIONALIDAD: PRECIOS CON DESCUENTO
    // =============================================

    @Test
    @DisplayName("Debe calcular precio con descuento correctamente para usuario regular")
    void debeCalcularPrecioConDescuentoParaUsuarioRegular() {
        // Arrange
        Long productoId = 1L;
        Integer cantidadEnCarrito = 2;
        boolean esUsuarioVIP = false;
        BigDecimal precioEsperado = new BigDecimal("90.00");
        
        Producto producto = crearProductoParaTest(productoId, "Zapatos Nike", "Zapatos", new BigDecimal("100.00"));
        
        // Mock: El producto existe
        when(productoRepository.findById(productoId))
                .thenReturn(Optional.of(producto));
        
        // Mock: DescuentoService retorna información con descuento
        DescuentoService.DescuentoInfo descuentoInfo = new DescuentoService.DescuentoInfo(
                new BigDecimal("100.00"), // precio original
                precioEsperado,           // precio final
                "Descuento por Categoría", // estrategia aplicada
                new BigDecimal("10.00"),  // total descuento
                150L                      // tiempo cálculo ms
        );
        
        when(descuentoService.aplicarDescuentos(eq(producto), any(DescuentoContexto.class)))
                .thenReturn(descuentoInfo);

        // Act
        BigDecimal precioFinal = productoService.calcularPrecioConDescuento(productoId, cantidadEnCarrito, esUsuarioVIP);

        // Assert
        assertThat(precioFinal).isEqualTo(precioEsperado);
        
        // Verify: Se llamó al repositorio para obtener el producto
        verify(productoRepository).findById(productoId);
        
        // Verify: Se llamó al servicio de descuentos con el contexto correcto
        verify(descuentoService).aplicarDescuentos(eq(producto), any(DescuentoContexto.class));
        
        logger.info("✅ Test exitoso: Precio con descuento calculado correctamente para usuario regular");
    }

    @Test
    @DisplayName("Debe calcular precio con descuento correctamente para usuario VIP")
    void debeCalcularPrecioConDescuentoParaUsuarioVIP() {
        // Arrange
        Long productoId = 2L;
        Integer cantidadEnCarrito = 5; // Cantidad que califica para descuento por volumen
        boolean esUsuarioVIP = true;
        BigDecimal precioEsperado = new BigDecimal("75.00"); // Mayor descuento para VIP
        
        Producto producto = crearProductoParaTest(productoId, "Camiseta Adidas", "Camisetas", new BigDecimal("100.00"));
        
        // Mock: El producto existe
        when(productoRepository.findById(productoId))
                .thenReturn(Optional.of(producto));
        
        // Mock: DescuentoService retorna mayor descuento para VIP
        DescuentoService.DescuentoInfo descuentoInfo = new DescuentoService.DescuentoInfo(
                new BigDecimal("100.00"), // precio original
                precioEsperado,           // precio final con descuento VIP
                "Descuento por Cantidad", // estrategia aplicada
                new BigDecimal("25.00"),  // mayor descuento para VIP
                200L                      // tiempo cálculo ms
        );
        
        when(descuentoService.aplicarDescuentos(eq(producto), any(DescuentoContexto.class)))
                .thenReturn(descuentoInfo);

        // Act
        BigDecimal precioFinal = productoService.calcularPrecioConDescuento(productoId, cantidadEnCarrito, esUsuarioVIP);

        // Assert
        assertThat(precioFinal).isEqualTo(precioEsperado);
        
        // Verify: Se construyó contexto con tipo de usuario VIP
        verify(descuentoService).aplicarDescuentos(eq(producto), argThat(contexto -> 
            "VIP".equals(contexto.getTipoUsuario()) && 
            cantidadEnCarrito.equals(contexto.getCantidadEnCarrito())
        ));
        
        logger.info("✅ Test exitoso: Precio con descuento VIP calculado correctamente");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando producto no existe")
    void debeLanzarExcepcionCuandoProductoNoExiste() {
        // Arrange
        Long productoIdInexistente = 999L;
        Integer cantidadEnCarrito = 1;
        boolean esUsuarioVIP = false;
        
        // Mock: El producto no existe
        when(productoRepository.findById(productoIdInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert
        ProductoNoEncontradoException exception = assertThrows(
                ProductoNoEncontradoException.class,
                () -> productoService.calcularPrecioConDescuento(productoIdInexistente, cantidadEnCarrito, esUsuarioVIP)
        );
        
        assertThat(exception.getMessage()).contains("No se encontró producto con ID: " + productoIdInexistente);
        
        // Verify: No se llamó al servicio de descuentos
        verify(descuentoService, never()).aplicarDescuentos(any(), any());
        
        logger.info("✅ Test exitoso: Excepción lanzada correctamente para producto inexistente");
    }

    @Test
    @DisplayName("Debe manejar error del servicio de descuentos gracefully")
    void debeManejarErrorDelServicioDeDescuentosGracefully() {
        // Arrange
        Long productoId = 3L;
        Integer cantidadEnCarrito = 1;
        boolean esUsuarioVIP = false;
        
        Producto producto = crearProductoParaTest(productoId, "Pantalones Nike", "Pantalones", new BigDecimal("80.00"));
        
        // Mock: El producto existe
        when(productoRepository.findById(productoId))
                .thenReturn(Optional.of(producto));
        
        // Mock: DescuentoService lanza excepción
        when(descuentoService.aplicarDescuentos(eq(producto), any(DescuentoContexto.class)))
                .thenThrow(new RuntimeException("Error interno del servicio de descuentos"));

        // Act & Assert
        ProductoException exception = assertThrows(
                ProductoException.class,
                () -> productoService.calcularPrecioConDescuento(productoId, cantidadEnCarrito, esUsuarioVIP)
        );
        
        assertThat(exception.getMessage()).contains("Error calculando precio con descuento");
        
        logger.info("✅ Test exitoso: Error del servicio de descuentos manejado correctamente");
    }
    
    // =============================================
    // MÉTODOS HELPER PARA TESTS
    // =============================================
    
    /**
     * Crea un producto para tests con datos específicos.
     * EDUCATIVO: Método helper que evita duplicación de código en tests.
     */
    private Producto crearProductoParaTest(Long id, String nombre, String categoria, BigDecimal precio) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setDescripcion("Producto para test");
        producto.setMarca("Test Brand");
        producto.setStockDisponible(10);
        producto.setActivo(Boolean.TRUE);
        producto.setFechaCreacion(LocalDateTime.now());
        return producto;
    }
}
