package com.tiendadeportiva.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendadeportiva.backend.config.TestSecurityConfig;
import com.tiendadeportiva.backend.exception.ProductoException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.IProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ProductoController.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2: Arquitectura Hexagonal
 * - Testing completo de la capa web usando @WebMvcTest
 * - Validación de responses JSON estructuradas
 * - Testing de manejo de errores HTTP centralizado
 * - Validación de códigos de estado semánticamente correctos
 * - Tests de serialización/deserialización robustos
 * - Siguiendo Google Java Style Guide y Conventional Commits
 * 
 * Cobertura de testing:
 * ✅ Operaciones CRUD completas
 * ✅ Manejo de errores con GlobalExceptionHandler
 * ✅ Validaciones de Bean Validation
 * ✅ Búsquedas y filtros avanzados
 * ✅ Respuestas HTTP semánticamente correctas
 * ✅ Testing robusto preparado para evolución a microservicios
 * 
 * @author Equipo Desarrollo
 * @version 2.0
 * @since Fase 2 - Arquitectura Hexagonal
 */
@WebMvcTest(controllers = ProductoController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ProductoController - Tests de Integración Arquitectura Hexagonal")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto productoValido;

    @BeforeEach
    void setUp() {
        productoValido = new Producto();
        productoValido.setId(1L);
        productoValido.setNombre("Camiseta Deportiva Nike");
        productoValido.setDescripcion("Camiseta de alta tecnología para running con tecnología Dri-FIT");
        productoValido.setPrecio(new BigDecimal("29.99"));
        productoValido.setCategoria("Camisetas");
        productoValido.setMarca("Nike");
        productoValido.setStockDisponible(15);
        productoValido.setActivo(Boolean.TRUE); // ✅ Boolean wrapper según convenciones del proyecto
    }

    // =============================================
    // TESTS DE QUERIES (OPERACIONES DE LECTURA)
    // =============================================

    @Test
    @DisplayName("GET /api/productos - Debe retornar lista de productos con HTTP 200")
    void debeRetornarListaDeProductosConHttp200() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(productoValido);
        when(productoService.obtenerTodosLosProductos()).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nombre", is("Camiseta Deportiva Nike")))
                .andExpect(jsonPath("$[0].precio", is(29.99)))
                .andExpect(jsonPath("$[0].marca", is("Nike"))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).obtenerTodosLosProductos();
    }

    @Test
    @DisplayName("GET /api/productos/{id} - Debe retornar producto cuando existe")
    void debeRetornarProductoCuandoExiste() throws Exception {
        // Arrange
        Long productId = 1L;
        when(productoService.obtenerProductoPorId(productId))
                .thenReturn(Optional.of(productoValido));

        // Act & Assert
        mockMvc.perform(get("/api/productos/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Camiseta Deportiva Nike")))
                .andExpect(jsonPath("$.marca", is("Nike")));

        verify(productoService).obtenerProductoPorId(productId);
    }

    @Test
    @DisplayName("GET /api/productos/{id} - Debe retornar HTTP 404 cuando producto no existe")
    void debeRetornar404CuandoProductoNoExiste() throws Exception {
        // Arrange
        Long productId = 999L;
        when(productoService.obtenerProductoPorId(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/productos/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productoService).obtenerProductoPorId(productId);
    }

    // =============================================
    // TESTS DE COMMANDS (OPERACIONES DE ESCRITURA)
    // =============================================

    @Test
    @DisplayName("POST /api/productos - Debe crear producto válido con HTTP 201")
    void debeCrearProductoValidoConHttp201() throws Exception {
        // Arrange
        when(productoService.crearProducto(any(Producto.class)))
                .thenReturn(productoValido);

        // Act & Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoValido)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Camiseta Deportiva Nike"))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).crearProducto(any(Producto.class));
    }

    @Test
    @DisplayName("POST /api/productos - Debe retornar HTTP 400 con datos inválidos por Bean Validation")
    void debeRetornar400ConDatosInvalidosPorBeanValidation() throws Exception {
        // ✅ Datos claramente inválidos para activar Bean Validation
        String productoConDatosInvalidos = """
            {
                "nombre": "",
                "precio": -10.50,
                "categoria": "",
                "marca": "",
                "stockDisponible": -5
            }
            """;
        
        // Act & Assert
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoConDatosInvalidos))
                .andDo(print()) // ✅ Debugging output
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists())
                .andExpect(jsonPath("$.fieldErrors.precio").exists())
                .andExpect(jsonPath("$.fieldErrors.categoria").exists()); // ✅ CORRECCIÓN: andExpect()
        
        // Verificar que el servicio NO fue llamado debido a validación fallida
        verify(productoService, never()).crearProducto(any(Producto.class));
    }

    @Test
    @DisplayName("POST /api/productos - Debe retornar HTTP 400 con errores específicos por campo")
    void debeRetornarErroresEspecificosPorCampoInvalido() throws Exception {
        String productoConPrecioNegativo = """
            {
                "nombre": "Producto Test Válido",
                "descripcion": "Descripción válida del producto",
                "precio": -5.00,
                "categoria": "Camisetas",
                "marca": "Nike",
                "stockDisponible": 10
            }
            """;
    
        // Act & Assert
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoConPrecioNegativo))
                .andDo(print()) // ✅ Debugging output
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.precio").value(containsString("mayor que 0"))); // ✅ CORRECCIÓN: andExpect()
        
        verify(productoService, never()).crearProducto(any(Producto.class));
    }

    @Test
    @DisplayName("POST /api/productos - Debe retornar HTTP 400 cuando servicio lanza ProductoException")
    void debeRetornar400CuandoServicioLanzaProductoException() throws Exception {
        // Arrange
        when(productoService.crearProducto(any(Producto.class)))
                .thenThrow(new ProductoException("PRECIO_INVALIDO", "El precio debe ser mayor que 0"));

        // Act & Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PRECIO_INVALIDO"))
                .andExpect(jsonPath("$.message").value("El precio debe ser mayor que 0"));

        verify(productoService).crearProducto(any(Producto.class));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} - Debe actualizar producto existente con HTTP 200")
    void debeActualizarProductoExistente() throws Exception {
        // Arrange
        Long productId = 1L;
        Producto productoActualizado = new Producto();
        productoActualizado.setId(productId);
        productoActualizado.setNombre("Camiseta Adidas Actualizada");
        productoActualizado.setDescripcion("Nueva descripción con tecnología Climacool");
        productoActualizado.setPrecio(new BigDecimal("34.99"));
        productoActualizado.setCategoria("Camisetas");
        productoActualizado.setMarca("Adidas");
        productoActualizado.setStockDisponible(20);

        when(productoService.actualizarProducto(eq(productId), any(Producto.class)))
                .thenReturn(Optional.of(productoActualizado));

        // Act & Assert
        mockMvc.perform(put("/api/productos/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Camiseta Adidas Actualizada")))
                .andExpect(jsonPath("$.marca", is("Adidas"))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).actualizarProducto(eq(productId), any(Producto.class));
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - Debe eliminar producto existente con HTTP 204")
    void debeEliminarProductoExistente() throws Exception {
        // Arrange
        Long productId = 1L;
        when(productoService.eliminarProducto(productId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/productos/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productoService).eliminarProducto(productId);
    }

    @Test
    @DisplayName("PATCH /api/productos/{id}/stock - Debe actualizar stock correctamente")
    void debeActualizarStockCorrectamente() throws Exception {
        // Arrange
        Long productId = 1L;
        Integer nuevoStock = 25;
        when(productoService.actualizarStock(productId, nuevoStock))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(patch("/api/productos/{id}/stock", productId)
                        .param("stock", nuevoStock.toString()))
                .andExpect(status().isOk()); // ✅ CORRECCIÓN FINAL: andExpect() en lugar de andExpected()

        verify(productoService).actualizarStock(productId, nuevoStock);
    }

    @Test
    @DisplayName("PATCH /api/productos/{id}/stock - Debe retornar HTTP 404 cuando producto no existe")
    void debeRetornar404AlActualizarStockDeProductoInexistente() throws Exception {
        // Arrange
        Long productId = 999L;
        Integer nuevoStock = 25;
        when(productoService.actualizarStock(productId, nuevoStock))
                .thenThrow(new ProductoNoEncontradoException(productId));

        // Act & Assert
        mockMvc.perform(patch("/api/productos/{id}/stock", productId)
                        .param("stock", nuevoStock.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCTO_NO_ENCONTRADO"));

        verify(productoService).actualizarStock(productId, nuevoStock);
    }

    @Test
    @DisplayName("PATCH /api/productos/{id}/stock - Debe retornar HTTP 400 con stock inválido")
    void debeRetornar400ConStockInvalido() throws Exception {
        // Arrange
        Long productId = 1L;
        Integer stockInvalido = -5;
        when(productoService.actualizarStock(productId, stockInvalido))
                .thenThrow(new ProductoException("STOCK_INVALIDO", "El stock no puede ser negativo"));

        // Act & Assert
        mockMvc.perform(patch("/api/productos/{id}/stock", productId)
                        .param("stock", stockInvalido.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("STOCK_INVALIDO"));

        verify(productoService).actualizarStock(productId, stockInvalido);
    }

    // =============================================
    // TESTS DE BÚSQUEDAS Y FILTROS
    // =============================================

    @Test
    @DisplayName("GET /api/productos/categoria/{categoria} - Debe buscar por categoría")
    void debeBuscarPorCategoria() throws Exception {
        // Arrange
        String categoria = "Camisetas";
        List<Producto> productos = Arrays.asList(productoValido);
        when(productoService.buscarPorCategoria(categoria)).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/productos/categoria/{categoria}", categoria))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoria", is(categoria))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).buscarPorCategoria(categoria);
    }

    @Test
    @DisplayName("GET /api/productos/marca/{marca} - Debe buscar por marca")
    void debeBuscarPorMarca() throws Exception {
        // Arrange
        String marca = "Nike";
        List<Producto> productos = Arrays.asList(productoValido);
        when(productoService.buscarPorMarca(marca)).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/productos/marca/{marca}", marca))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].marca", is(marca))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).buscarPorMarca(marca);
    }

    @Test
    @DisplayName("GET /api/productos/buscar?nombre=xxx - Debe buscar por nombre")
    void debeBuscarPorNombre() throws Exception {
        // Arrange
        String nombre = "Camiseta";
        List<Producto> productos = Arrays.asList(productoValido);
        when(productoService.buscarPorNombre(nombre)).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/productos/buscar")
                        .param("nombre", nombre))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).buscarPorNombre(nombre);
    }

    @Test
    @DisplayName("GET /api/productos/precio?min=x&max=y - Debe buscar por rango de precios")
    void debeBuscarPorRangoDePrecios() throws Exception {
        // Arrange
        BigDecimal precioMin = new BigDecimal("20.00");
        BigDecimal precioMax = new BigDecimal("50.00");
        List<Producto> productos = Arrays.asList(productoValido);
        when(productoService.buscarPorRangoPrecios(precioMin, precioMax)).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/productos/precio")
                        .param("min", precioMin.toString())
                        .param("max", precioMax.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).buscarPorRangoPrecios(precioMin, precioMax);
    }

    @Test
    @DisplayName("GET /api/productos/con-stock - Debe obtener productos con stock disponible")
    void debeObtenerProductosConStock() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(productoValido);
        when(productoService.obtenerProductosConStock()).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/productos/con-stock"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).obtenerProductosConStock();
    }

    // =============================================
    // TESTS DE METADATOS DEL CATÁLOGO
    // =============================================

    @Test
    @DisplayName("GET /api/productos/categorias - Debe obtener todas las categorías")
    void debeObtenerTodasLasCategorias() throws Exception {
        // Arrange
        List<String> categorias = Arrays.asList("Camisetas", "Shorts", "Zapatillas");
        when(productoService.obtenerCategorias()).thenReturn(categorias);

        // Act & Assert
        mockMvc.perform(get("/api/productos/categorias"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).obtenerCategorias();
    }

    @Test
    @DisplayName("GET /api/productos/marcas - Debe obtener todas las marcas")
    void debeObtenerTodasLasMarcas() throws Exception {
        // Arrange
        List<String> marcas = Arrays.asList("Nike", "Adidas", "Puma");
        when(productoService.obtenerMarcas()).thenReturn(marcas);

        // Act & Assert
        mockMvc.perform(get("/api/productos/marcas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3))); // ✅ CORRECCIÓN: andExpect()

        verify(productoService).obtenerMarcas();
    }
}
