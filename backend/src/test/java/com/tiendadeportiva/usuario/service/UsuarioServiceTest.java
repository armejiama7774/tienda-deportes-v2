package com.tiendadeportiva.usuario.service;

import com.tiendadeportiva.usuario.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para UsuarioService.
 * 
 * BOUNDED CONTEXT: Usuario
 * - Verifica funcionalidad básica del dominio Usuario
 * - Tests unitarios sin dependencias externas
 * - Preparación para testing de microservicios
 */
@DisplayName("🧪 Usuario Service - Tests Unitarios")
class UsuarioServiceTest {

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService();
    }

    @Test
    @DisplayName("✅ Debe crear usuario correctamente")
    void debeCrearUsuarioCorrectamente() {
        // Given
        Usuario usuario = new Usuario("Juan", "Pérez", "juan@email.com", "password123");
        
        // When
        Usuario usuarioCreado = usuarioService.crearUsuario(usuario);
        
        // Then
        assertNotNull(usuarioCreado);
        assertNotNull(usuarioCreado.getId());
        assertEquals("Juan", usuarioCreado.getNombre());
        assertEquals("Pérez", usuarioCreado.getApellido());
        assertEquals("juan@email.com", usuarioCreado.getEmail());
        assertEquals(Usuario.TipoUsuario.REGULAR, usuarioCreado.getTipo());
        assertTrue(usuarioCreado.getActivo());
        assertNotNull(usuarioCreado.getFechaCreacion());
    }

    @Test
    @DisplayName("✅ Debe obtener usuario por ID")
    void debeObtenerUsuarioPorId() {
        // Given
        Long usuarioId = 1L;
        
        // When
        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorId(usuarioId);
        
        // Then
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        assertEquals(usuarioId, usuario.getId());
        assertNotNull(usuario.getNombre());
        assertNotNull(usuario.getEmail());
    }

    @Test
    @DisplayName("✅ Debe verificar usuario VIP correctamente")
    void debeVerificarUsuarioVIP() {
        // Given
        Usuario usuarioRegular = new Usuario("Ana", "García", "ana@email.com", "password123");
        usuarioRegular.setTipo(Usuario.TipoUsuario.REGULAR);
        
        Usuario usuarioVIP = new Usuario("Carlos", "López", "carlos@email.com", "password123");
        usuarioVIP.setTipo(Usuario.TipoUsuario.VIP);
        
        // When & Then
        assertFalse(usuarioRegular.esVIP());
        assertTrue(usuarioVIP.esVIP());
    }

    @Test
    @DisplayName("✅ Debe obtener nombre completo correctamente")
    void debeObtenerNombreCompleto() {
        // Given
        Usuario usuario = new Usuario("María", "Rodríguez", "maria@email.com", "password123");
        
        // When
        String nombreCompleto = usuario.getNombreCompleto();
        
        // Then
        assertEquals("María Rodríguez", nombreCompleto);
    }

    @Test
    @DisplayName("✅ Debe desactivar usuario correctamente")
    void debeDesactivarUsuario() {
        // Given
        Usuario usuario = new Usuario("Pedro", "Martínez", "pedro@email.com", "password123");
        assertTrue(usuario.getActivo());
        
        // When
        usuario.desactivar();
        
        // Then
        assertFalse(usuario.getActivo());
        assertNotNull(usuario.getFechaModificacion());
    }

    @Test
    @DisplayName("✅ Debe registrar acceso del usuario")
    void debeRegistrarAcceso() {
        // Given
        Usuario usuario = new Usuario("Sofia", "González", "sofia@email.com", "password123");
        assertNull(usuario.getUltimoAcceso());
        
        // When
        usuario.registrarAcceso();
        
        // Then
        assertNotNull(usuario.getUltimoAcceso());
    }

    @Test
    @DisplayName("✅ Debe obtener información básica del usuario")
    void debeObtenerInfoBasica() {
        // Given
        Long usuarioId = 1L;
        
        // When
        UsuarioService.UsuarioInfo info = usuarioService.obtenerInfoBasica(usuarioId);
        
        // Then
        assertNotNull(info);
        assertEquals(usuarioId, info.getId());
        assertNotNull(info.getNombreCompleto());
        assertNotNull(info.getEmail());
        assertNotNull(info.getTipo());
        assertFalse(info.isEsVIP()); // Usuario simulado es Regular
    }

    @Test
    @DisplayName("❌ Debe fallar con datos inválidos")
    void debeFallarConDatosInvalidos() {
        // Test usuario nulo
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(null);
        });

        // Test nombre vacío
        Usuario usuarioSinNombre = new Usuario();
        usuarioSinNombre.setApellido("Apellido");
        usuarioSinNombre.setEmail("email@test.com");
        usuarioSinNombre.setPassword("password123");
        
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(usuarioSinNombre);
        });
    }
}
