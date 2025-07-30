package com.tiendadeportiva.backend.service.descuento;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Contexto para el cálculo de descuentos usando Strategy Pattern.
 * 
 * PATRÓN DE DISEÑO: Context Object
 * 
 * ¿POR QUÉ UN CONTEXTO?
 * - Encapsula toda la información necesaria para el cálculo
 * - Evita pasar múltiples parámetros a los métodos
 * - Facilita agregar nueva información sin romper interfaces
 * - Mejora la legibilidad del código
 * 
 * CASOS DE USO PROFESIONALES:
 * - Información del usuario (VIP, nuevo cliente, etc.)
 * - Datos temporales (temporada, día especial)
 * - Contexto de compra (carrito total, cantidad)
 * - Configuraciones dinámicas del sistema
 * 
 * @author Equipo Desarrollo
 * @version 2.1
 * @since Fase 2 - Mejoras con Patrones de Diseño
 */
public class DescuentoContexto {
    
    private final LocalDateTime fechaCalculo;
    private final String tipoUsuario;
    private final Integer cantidadEnCarrito;
    private final String codigoPromocional;
    private final String temporada;
    private final Map<String, Object> propiedadesAdicionales;
    
    /**
     * Constructor principal del contexto de descuento.
     * 
     * PATRÓN APLICADO: Builder Pattern implícito
     * - Facilita la creación de objetos complejos
     * - Permite valores opcionales
     * - Mejora la legibilidad del código
     */
    private DescuentoContexto(Builder builder) {
        this.fechaCalculo = builder.fechaCalculo != null ? builder.fechaCalculo : LocalDateTime.now();
        this.tipoUsuario = builder.tipoUsuario;
        this.cantidadEnCarrito = builder.cantidadEnCarrito;
        this.codigoPromocional = builder.codigoPromocional;
        this.temporada = builder.temporada;
        this.propiedadesAdicionales = new HashMap<>(builder.propiedadesAdicionales);
    }
    
    // =============================================
    // GETTERS - Acceso inmutable a las propiedades
    // =============================================
    
    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }
    
    public String getTipoUsuario() {
        return tipoUsuario;
    }
    
    public Integer getCantidadEnCarrito() {
        return cantidadEnCarrito;
    }
    
    public String getCodigoPromocional() {
        return codigoPromocional;
    }
    
    public String getTemporada() {
        return temporada;
    }
    
    /**
     * Obtiene una propiedad adicional del contexto.
     * 
     * @param clave La clave de la propiedad
     * @return El valor de la propiedad, o null si no existe
     */
    public Object getPropiedad(String clave) {
        return propiedadesAdicionales.get(clave);
    }
    
    /**
     * Verifica si una propiedad existe en el contexto.
     * 
     * @param clave La clave a verificar
     * @return true si la propiedad existe, false en caso contrario
     */
    public boolean tienePropiedad(String clave) {
        return propiedadesAdicionales.containsKey(clave);
    }
    
    // =============================================
    // BUILDER PATTERN IMPLEMENTATION
    // =============================================
    
    /**
     * Builder para crear instancias de DescuentoContexto.
     * 
     * PATRÓN DE DISEÑO: Builder
     * BENEFICIO PARA JUNIORS: Código más legible y mantenible
     */
    public static class Builder {
        private LocalDateTime fechaCalculo;
        private String tipoUsuario;
        private Integer cantidadEnCarrito;
        private String codigoPromocional;
        private String temporada;
        private Map<String, Object> propiedadesAdicionales = new HashMap<>();
        
        /**
         * Establece la fecha de cálculo del descuento.
         */
        public Builder conFechaCalculo(LocalDateTime fechaCalculo) {
            this.fechaCalculo = fechaCalculo;
            return this;
        }
        
        /**
         * Establece el tipo de usuario (VIP, NORMAL, NUEVO).
         */
        public Builder conTipoUsuario(String tipoUsuario) {
            this.tipoUsuario = tipoUsuario;
            return this;
        }
        
        /**
         * Establece la cantidad de productos en el carrito.
         */
        public Builder conCantidadEnCarrito(Integer cantidad) {
            this.cantidadEnCarrito = cantidad;
            return this;
        }
        
        /**
         * Establece un código promocional.
         */
        public Builder conCodigoPromocional(String codigo) {
            this.codigoPromocional = codigo;
            return this;
        }
        
        /**
         * Establece la temporada actual.
         */
        public Builder conTemporada(String temporada) {
            this.temporada = temporada;
            return this;
        }
        
        /**
         * Agrega una propiedad adicional al contexto.
         */
        public Builder conPropiedad(String clave, Object valor) {
            this.propiedadesAdicionales.put(clave, valor);
            return this;
        }
        
        /**
         * Construye la instancia de DescuentoContexto.
         */
        public DescuentoContexto build() {
            return new DescuentoContexto(this);
        }
    }
    
    /**
     * Método estático para iniciar el builder.
     * 
     * BENEFICIO PARA JUNIORS: API fluida y fácil de usar
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // =============================================
    // MÉTODOS DE UTILIDAD
    // =============================================
    
    @Override
    public String toString() {
        return String.format(
            "DescuentoContexto{fechaCalculo=%s, tipoUsuario='%s', cantidadEnCarrito=%d, " +
            "codigoPromocional='%s', temporada='%s', propiedadesAdicionales=%s}",
            fechaCalculo, tipoUsuario, cantidadEnCarrito, 
            codigoPromocional, temporada, propiedadesAdicionales
        );
    }
}