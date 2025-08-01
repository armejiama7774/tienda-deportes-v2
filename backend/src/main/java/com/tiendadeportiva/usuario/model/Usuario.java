package com.tiendadeportiva.usuario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entidad Usuario para el dominio de gestión de usuarios.
 * 
 * BOUNDED CONTEXT: Usuario
 * - Responsabilidad: Gestión de usuarios, autenticación, perfiles
 * - Separado del dominio Producto para preparar microservicios
 * 
 * PREPARACIÓN PARA MICROSERVICIOS:
 * - Entidad independiente con su propia tabla
 * - Sin dependencias directas a otros dominios
 * - Preparada para base de datos separada en el futuro
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo = TipoUsuario.REGULAR;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    // =============================================
    // CONSTRUCTORES
    // =============================================

    public Usuario() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
        this.tipo = TipoUsuario.REGULAR;
    }

    public Usuario(String nombre, String apellido, String email, String password) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
    }

    // =============================================
    // MÉTODOS DE NEGOCIO
    // =============================================

    /**
     * Actualiza la fecha del último acceso del usuario.
     */
    public void registrarAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    /**
     * Verifica si el usuario es VIP.
     */
    public boolean esVIP() {
        return TipoUsuario.VIP.equals(this.tipo);
    }

    /**
     * Obtiene el nombre completo del usuario.
     */
    public String getNombreCompleto() {
        return String.format("%s %s", nombre, apellido);
    }

    /**
     * Marca el usuario como inactivo (soft delete).
     */
    public void desactivar() {
        this.activo = false;
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Reactiva un usuario.
     */
    public void activar() {
        this.activo = true;
        this.fechaModificacion = LocalDateTime.now();
    }

    // =============================================
    // GETTERS Y SETTERS
    // =============================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.fechaModificacion = LocalDateTime.now();
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
        this.fechaModificacion = LocalDateTime.now();
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
        this.fechaModificacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    // =============================================
    // EQUALS, HASHCODE Y TOSTRING
    // =============================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Usuario{id=%d, nombre='%s', apellido='%s', email='%s', tipo=%s, activo=%s}", 
                           id, nombre, apellido, email, tipo, activo);
    }

    // =============================================
    // ENUM INTERNO
    // =============================================

    public enum TipoUsuario {
        REGULAR("Regular"),
        VIP("VIP"),
        ADMIN("Administrador");

        private final String descripcion;

        TipoUsuario(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
