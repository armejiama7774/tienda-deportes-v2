package com.tiendadeportiva.usuario.service;

import com.tiendadeportiva.usuario.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio del dominio Usuario.
 * 
 * BOUNDED CONTEXT: Usuario
 * - Responsabilidad: Gestión de usuarios, autenticación, perfiles
 * - Independiente del dominio Producto
 * 
 * PREPARACIÓN PARA MICROSERVICIOS:
 * - Servicio autocontenido sin dependencias externas a otros dominios
 * - Listo para ser extraído a un microservicio independiente
 * - Mantiene patrones consistentes con ProductoService
 * 
 * FASE ACTUAL: Implementación básica
 * - Operaciones CRUD básicas
 * - Validaciones de negocio
 * - Logging consistente
 * 
 * FUTURO: Cuando se convierta en microservicio
 * - Tendrá su propia base de datos
 * - Comunicará con otros servicios vía eventos
 * - Mantendra APIs REST independientes
 */
@Service
@Transactional
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    // TODO: En fases futuras se agregará:
    // - UsuarioRepository (persistencia)
    // - PasswordEncoder (seguridad)
    // - EventPublisher (comunicación con otros dominios)
    // - ValidationService (validaciones complejas)

    // =============================================
    // OPERACIONES BÁSICAS CRUD
    // =============================================

    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * @param usuario Usuario a crear
     * @return Usuario creado con ID asignado
     */
    public Usuario crearUsuario(Usuario usuario) {
        // TODO: Validaciones de negocio
        validarUsuarioParaCreacion(usuario);
        
        logger.info("👤 Creando nuevo usuario: {} ({})", 
                   usuario.getNombreCompleto(), usuario.getEmail());
        
        // TODO: Hashear contraseña
        // usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // TODO: Persistir en base de datos
        // Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Por ahora, simular ID asignado
        usuario.setId(System.currentTimeMillis());
        usuario.setFechaCreacion(LocalDateTime.now());
        
        logger.info("✅ Usuario creado exitosamente con ID: {}", usuario.getId());
        
        // TODO: Emitir evento UsuarioCreado para otros dominios
        // eventPublisher.publishEvent(new UsuarioCreado(usuario));
        
        return usuario;
    }

    /**
     * Obtiene un usuario por su ID.
     */
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        logger.debug("🔍 Buscando usuario por ID: {}", id);
        
        // TODO: Buscar en base de datos
        // return usuarioRepository.findByIdAndActivoTrue(id);
        
        // Por ahora, simular búsqueda
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        
        // Crear usuario simulado para demostración
        Usuario usuario = new Usuario("Juan", "Pérez", "juan@email.com", "password123");
        usuario.setId(id);
        
        return Optional.of(usuario);
    }

    /**
     * Obtiene un usuario por su email.
     */
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        logger.debug("🔍 Buscando usuario por email: {}", email);
        
        // TODO: Buscar en base de datos
        // return usuarioRepository.findByEmailAndActivoTrue(email);
        
        // Por ahora, simular búsqueda
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.empty(); // Simular no encontrado
    }

    /**
     * Actualiza los datos de un usuario.
     */
    public Optional<Usuario> actualizarUsuario(Long id, Usuario datosActualizados) {
        logger.info("📝 Actualizando usuario ID: {}", id);
        
        Optional<Usuario> usuarioOpt = obtenerUsuarioPorId(id);
        if (usuarioOpt.isEmpty()) {
            logger.warn("❌ Usuario con ID {} no encontrado para actualización", id);
            return Optional.empty();
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Actualizar solo campos no nulos
        if (datosActualizados.getNombre() != null) {
            usuario.setNombre(datosActualizados.getNombre());
        }
        if (datosActualizados.getApellido() != null) {
            usuario.setApellido(datosActualizados.getApellido());
        }
        if (datosActualizados.getEmail() != null) {
            usuario.setEmail(datosActualizados.getEmail());
        }
        if (datosActualizados.getTipo() != null) {
            usuario.setTipo(datosActualizados.getTipo());
        }
        
        usuario.setFechaModificacion(LocalDateTime.now());
        
        // TODO: Persistir cambios
        // Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        logger.info("✅ Usuario actualizado: {}", usuario.getNombreCompleto());
        
        // TODO: Emitir evento UsuarioActualizado
        // eventPublisher.publishEvent(new UsuarioActualizado(usuario));
        
        return Optional.of(usuario);
    }

    /**
     * Desactiva un usuario (soft delete).
     */
    public boolean desactivarUsuario(Long id) {
        logger.info("🗑️ Desactivando usuario ID: {}", id);
        
        Optional<Usuario> usuarioOpt = obtenerUsuarioPorId(id);
        if (usuarioOpt.isEmpty()) {
            logger.warn("❌ Usuario con ID {} no encontrado para desactivación", id);
            return false;
        }
        
        Usuario usuario = usuarioOpt.get();
        usuario.desactivar();
        
        // TODO: Persistir cambios
        // usuarioRepository.save(usuario);
        
        logger.info("✅ Usuario desactivado: {}", usuario.getNombreCompleto());
        
        // TODO: Emitir evento UsuarioDesactivado
        // eventPublisher.publishEvent(new UsuarioDesactivado(usuario));
        
        return true;
    }

    // =============================================
    // OPERACIONES DE NEGOCIO
    // =============================================

    /**
     * Verifica si un usuario es VIP.
     * Esta información es usada por el dominio Producto para aplicar descuentos.
     */
    public boolean esUsuarioVIP(Long usuarioId) {
        logger.debug("🔍 Verificando si usuario {} es VIP", usuarioId);
        
        Optional<Usuario> usuarioOpt = obtenerUsuarioPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            logger.warn("❌ Usuario con ID {} no encontrado para verificación VIP", usuarioId);
            return false;
        }
        
        boolean esVip = usuarioOpt.get().esVIP();
        logger.debug("👑 Usuario {} {} VIP", usuarioId, esVip ? "ES" : "NO ES");
        
        return esVip;
    }

    /**
     * Registra el acceso de un usuario al sistema.
     */
    public void registrarAcceso(Long usuarioId) {
        logger.debug("🔐 Registrando acceso del usuario: {}", usuarioId);
        
        Optional<Usuario> usuarioOpt = obtenerUsuarioPorId(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.registrarAcceso();
            
            // TODO: Persistir cambios
            // usuarioRepository.save(usuario);
            
            logger.debug("✅ Acceso registrado para: {}", usuario.getNombreCompleto());
        }
    }

    /**
     * Obtiene información básica del usuario para otros dominios.
     * Retorna solo datos públicos, no información sensible.
     */
    public UsuarioInfo obtenerInfoBasica(Long usuarioId) {
        logger.debug("ℹ️ Obteniendo información básica del usuario: {}", usuarioId);
        
        Optional<Usuario> usuarioOpt = obtenerUsuarioPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return null;
        }
        
        Usuario usuario = usuarioOpt.get();
        return new UsuarioInfo(
            usuario.getId(),
            usuario.getNombreCompleto(),
            usuario.getEmail(),
            usuario.getTipo(),
            usuario.esVIP()
        );
    }

    // =============================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // =============================================

    private void validarUsuarioParaCreacion(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        
        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        
        // TODO: Validar que el email no esté ya registrado
        // if (usuarioRepository.existsByEmail(usuario.getEmail())) {
        //     throw new IllegalArgumentException("Ya existe un usuario con este email");
        // }
    }

    // =============================================
    // DTO PARA COMUNICACIÓN ENTRE DOMINIOS
    // =============================================

    /**
     * DTO para compartir información básica del usuario con otros dominios.
     * No incluye información sensible como contraseñas.
     */
    public static class UsuarioInfo {
        private final Long id;
        private final String nombreCompleto;
        private final String email;
        private final Usuario.TipoUsuario tipo;
        private final boolean esVIP;

        public UsuarioInfo(Long id, String nombreCompleto, String email, 
                          Usuario.TipoUsuario tipo, boolean esVIP) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
            this.email = email;
            this.tipo = tipo;
            this.esVIP = esVIP;
        }

        public Long getId() { return id; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getEmail() { return email; }
        public Usuario.TipoUsuario getTipo() { return tipo; }
        public boolean isEsVIP() { return esVIP; }

        @Override
        public String toString() {
            return String.format("UsuarioInfo{id=%d, nombre='%s', tipo=%s, esVIP=%s}", 
                               id, nombreCompleto, tipo, esVIP);
        }
    }
}
