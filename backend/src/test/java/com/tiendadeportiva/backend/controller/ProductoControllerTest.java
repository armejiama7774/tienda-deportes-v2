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
import org.springframework.security.test.context.support.WithMockUser;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ProductoController.
 * 
 * Implementa testing de la capa web usando @WebMvcTest:
 * - Tests del comportamiento HTTP completo
 * - Validación de responses JSON
 * - Testing de manejo de errores HTTP
 * - Validación de códigos de estado
 * - Tests de serialización/deserialización
 * 
 * Estos tests validan:
 * - Integración correcta entre controller y service
 * - Manejo adecuado de excepciones
 * - Formateo correcto de respuestas HTTP
 * - Aplicación correcta de validaciones
 * 
 * @author Equipo Desarrollo
 * @version 1.0
 * @since Fase 1 - Monolito Modular con SOLID
 */
@WebMvcTest(controllers = ProductoController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ProductoController - Tests de Integración")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto productoValido;
    private Producto productoInvalido;

    @BeforeEach
    void setUp() {
        productoValido = new Producto();
        productoValido.setId(1L);
        productoValido.setNombre("Camiseta Deportiva");
        productoValido.setDescripcion("Camiseta para running");
        productoValido.setPrecio(new BigDecimal("25.99"));
        productoValido.setCategoria("Camisetas");
        productoValido.setMarca("Nike");
        productoValido.setStockDisponible(10);
        productoValido.setActivo(true);

        productoInvalido = new Producto();
        // Nombre vacío para disparar validación
        productoInvalido.setNombre("");
        productoInvalido.setDescripcion("Descripción");
        productoInvalido.setPrecio(new BigDecimal("25.99"));
        productoInvalido.setCategoria("Camisetas");
        productoInvalido.setMarca("Nike");
        productoInvalido.setStockDisponible(10);
    }

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
                .andExpect(jsonPath("$[0].nombre", is("Camiseta Deportiva")))
                .andExpect(jsonPath("$[0].precio", is(25.99)))
                .andExpect(jsonPath("$[0].marca", is("Nike")));

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
                .andExpect(jsonPath("$.nombre", is("Camiseta Deportiva")))
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
                .andExpect(jsonPath("$.nombre", is("Camiseta Deportiva")));

        verify(productoService).crearProducto(any(Producto.class));
    }

    @Test
    @DisplayName("POST /api/productos - Debe retornar HTTP 400 con datos inválidos")
    void debeRetornar400ConDatosInvalidos() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoInvalido)))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).crearProducto(any());
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
                .andExpect(status().isBadRequest());

        verify(productoService).crearProducto(any(Producto.class));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} - Debe actualizar producto existente")
    void debeActualizarProductoExistente() throws Exception {
        // Arrange
        Long productId = 1L;
        Producto productoActualizado = new Producto();
        productoActualizado.setId(productId);
        productoActualizado.setNombre("Camiseta Actualizada");
        productoActualizado.setDescripcion("Nueva descripción");
        productoActualizado.setPrecio(new BigDecimal("29.99"));
        productoActualizado.setCategoria("Camisetas");
        productoActualizado.setMarca("Adidas");
        productoActualizado.setStockDisponible(15);

        when(productoService.actualizarProducto(eq(productId), any(Producto.class)))
                .thenReturn(Optional.of(productoActualizado));

        // Act & Assert
        mockMvc.perform(put("/api/productos/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Camiseta Actualizada")))
                .andExpect(jsonPath("$.marca", is("Adidas")));

        verify(productoService).actualizarProducto(eq(productId), any(Producto.class));
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
                .andExpect(status().isOk());

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
                .andExpect(status().isNotFound());

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
                .andExpect(status().isBadRequest());

        verify(productoService).actualizarStock(productId, stockInvalido);
    }

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
                .andExpect(jsonPath("$[0].categoria", is(categoria)));

        verify(productoService).buscarPorCategoria(categoria);
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
                .andExpect(jsonPath("$", hasSize(1)));

        verify(productoService).buscarPorNombre(nombre);
    }
}
