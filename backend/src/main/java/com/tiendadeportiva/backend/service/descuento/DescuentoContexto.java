package com.tiendadeportiva.backend.service.descuento;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contexto para cálculo de descuentos.
 * 
 * FASE 2: ARQUITECTURA HEXAGONAL - CONTEXT PATTERN
 * 
 * PATRÓN DE DISEÑO: Context Object Pattern
 * - Encapsula toda la información necesaria para cálculos de descuento
 * - Extensible sin romper compatibilidad (agregar campos sin cambiar interface)
 * - Sistema de propiedades dinámicas con soporte multi-tipo
 * - Preparado para casos de uso complejos y futuros microservicios
 * 
 * MEJORA APLICADA: Sistema de propiedades con Object para múltiples tipos
 * - Soporte para String, Integer, Boolean, LocalDateTime
 * - Métodos de conveniencia para acceso tipado
 * - Backward compatibility con tests existentes
 * - Builder sin valores por defecto para máxima flexibilidad
 * 
 * @author Equipo Desarrollo
 * @version 2.1.3
 * @since Fase 2 - Mejoras con Patrones de Diseño
 */
public class DescuentoContexto {
    
    // =============================================
    // DATOS BÁSICOS DE COMPRA
    // =============================================
    private final Integer cantidadEnCarrito;
    private final LocalDateTime fechaCompra;
    private final LocalDateTime fechaCalculo;     // Compatibilidad con tests existentes
    
    // =============================================
    // INFORMACIÓN DE USUARIO
    // =============================================
    private final String tipoUsuario;          // VIP, NORMAL, EMPLEADO, ESTUDIANTE
    private final Long usuarioId;              // Para descuentos personalizados
    
    // =============================================
    // CÓDIGOS Y PROMOCIONES
    // =============================================
    private final String codigoPromocional;    // BLACKFRIDAY20, VERANO15, etc.
    private final String canalVenta;           // WEB, MOBILE, TIENDA_FISICA
    
    // =============================================
    // CONTEXTO TEMPORAL Y GEOGRÁFICO (FASE FUTURA)
    // =============================================
    private final String temporada;            // VERANO, INVIERNO, BLACKFRIDAY
    private final String paisUsuario;          // Para descuentos por región
    
    // =============================================
    // SISTEMA DE PROPIEDADES DINÁMICAS MULTI-TIPO
    // =============================================
    private final Map<String, Object> propiedades;  // Soporte para múltiples tipos
    
    /**
     * Constructor privado - usar Builder para crear instancias.
     */
    private DescuentoContexto(Builder builder) {
        this.cantidadEnCarrito = builder.cantidadEnCarrito;
        this.fechaCompra = builder.fechaCompra;
        this.fechaCalculo = builder.fechaCalculo;
        this.tipoUsuario = builder.tipoUsuario;
        this.usuarioId = builder.usuarioId;
        this.codigoPromocional = builder.codigoPromocional;
        this.canalVenta = builder.canalVenta;
        this.temporada = builder.temporada;
        this.paisUsuario = builder.paisUsuario;
        this.propiedades = new HashMap<>(builder.propiedades);
    }
    
    // =============================================
    // GETTERS - ACCESO INMUTABLE
    // =============================================
    
    public Integer getCantidadEnCarrito() { return cantidadEnCarrito; }
    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public LocalDateTime getFechaCalculo() { return fechaCalculo; }  // Compatibilidad con tests
    public String getTipoUsuario() { return tipoUsuario; }
    public Long getUsuarioId() { return usuarioId; }
    public String getCodigoPromocional() { return codigoPromocional; }
    public String getCanalVenta() { return canalVenta; }
    public String getTemporada() { return temporada; }
    public String getPaisUsuario() { return paisUsuario; }
    
    // =============================================
    // SISTEMA DE PROPIEDADES DINÁMICAS MULTI-TIPO
    // =============================================
    
    /**
     * Obtiene una propiedad dinámica por clave (tipo genérico).
     * 
     * CASO DE USO: Acceso directo al objeto sin conversión
     * 
     * @param clave Clave de la propiedad
     * @return Valor de la propiedad como Object o null si no existe
     */
    public Object getPropiedad(String clave) {
        return propiedades.get(clave);
    }
    
    /**
     * Obtiene una propiedad como String (compatibilidad con versión anterior).
     * 
     * @param clave Clave de la propiedad
     * @return Valor de la propiedad como String o null si no existe
     */
    public String getPropiedadComoString(String clave) {
        Object valor = propiedades.get(clave);
        return valor != null ? valor.toString() : null;
    }
    
    /**
     * Obtiene una propiedad como Integer.
     * 
     * @param clave Clave de la propiedad
     * @return Valor de la propiedad como Integer o null si no existe o no es convertible
     */
    public Integer getPropiedadComoInteger(String clave) {
        Object valor = propiedades.get(clave);
        if (valor == null) return null;
        
        if (valor instanceof Integer) {
            return (Integer) valor;
        }
        
        try {
            return Integer.valueOf(valor.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Obtiene una propiedad como Boolean.
     * 
     * @param clave Clave de la propiedad
     * @return Valor de la propiedad como Boolean o null si no existe o no es convertible
     */
    public Boolean getPropiedadComoBoolean(String clave) {
        Object valor = propiedades.get(clave);
        if (valor == null) return null;
        
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        
        // Conversión desde String
        String valorStr = valor.toString().toLowerCase();
        if ("true".equals(valorStr) || "1".equals(valorStr) || "yes".equals(valorStr)) {
            return Boolean.TRUE;
        }
        if ("false".equals(valorStr) || "0".equals(valorStr) || "no".equals(valorStr)) {
            return Boolean.FALSE;
        }
        
        return null;
    }
    
    /**
     * Verifica si existe una propiedad específica.
     * 
     * @param clave Clave de la propiedad a verificar
     * @return true si la propiedad existe, false si no
     */
    public boolean tienePropiedad(String clave) {
        return propiedades.containsKey(clave);
    }
    
    /**
     * Obtiene todas las propiedades como mapa inmutable.
     * 
     * @return Mapa inmutable de propiedades
     */
    public Map<String, Object> getPropiedades() {
        return new HashMap<>(propiedades);
    }
    
    // =============================================
    // MÉTODOS DE CONVENIENCIA
    // =============================================
    
    /**
     * Verifica si el usuario es VIP.
     * Maneja caso donde tipoUsuario puede ser null.
     */
    public boolean esUsuarioVip() {
        return "VIP".equalsIgnoreCase(tipoUsuario);
    }
    
    /**
     * Verifica si el usuario es empleado.
     * Maneja caso donde tipoUsuario puede ser null.
     */
    public boolean esEmpleado() {
        return "EMPLEADO".equalsIgnoreCase(tipoUsuario);
    }
    
    /**
     * Verifica si hay un código promocional válido.
     */
    public boolean tieneCodigoPromocional() {
        return codigoPromocional != null && !codigoPromocional.trim().isEmpty();
    }
    
    /**
     * Verifica si la compra es por volumen (cantidad >= umbral).
     */
    public boolean esCompraVolumen(int umbralMinimo) {
        return cantidadEnCarrito != null && cantidadEnCarrito >= umbralMinimo;
    }
    
    // =============================================
    // BUILDER PATTERN SIN VALORES POR DEFECTO AUTOMÁTICOS
    // =============================================
    
    public static class Builder {
        private Integer cantidadEnCarrito;
        private LocalDateTime fechaCompra;
        private LocalDateTime fechaCalculo;
        private String tipoUsuario;        // Sin valor por defecto
        private Long usuarioId;
        private String codigoPromocional;
        private String canalVenta;         // Sin valor por defecto
        private String temporada;
        private String paisUsuario;        // Sin valor por defecto
        private Map<String, Object> propiedades = new HashMap<>();
        
        public Builder conCantidadEnCarrito(Integer cantidad) {
            this.cantidadEnCarrito = cantidad;
            return this;
        }
        
        public Builder conFechaCompra(LocalDateTime fecha) {
            this.fechaCompra = fecha;
            return this;
        }
        
        /**
         * Establece la fecha de cálculo (compatibilidad con tests existentes).
         */
        public Builder conFechaCalculo(LocalDateTime fecha) {
            this.fechaCalculo = fecha;
            return this;
        }
        
        public Builder conTipoUsuario(String tipo) {
            this.tipoUsuario = tipo;
            return this;
        }
        
        public Builder conUsuarioId(Long id) {
            this.usuarioId = id;
            return this;
        }
        
        public Builder conCodigoPromocional(String codigo) {
            this.codigoPromocional = codigo;
            return this;
        }
        
        public Builder conCanalVenta(String canal) {
            this.canalVenta = canal;
            return this;
        }
        
        public Builder conTemporada(String temporada) {
            this.temporada = temporada;
            return this;
        }
        
        public Builder conPaisUsuario(String pais) {
            this.paisUsuario = pais;
            return this;
        }
        
        /**
         * Agrega una propiedad dinámica al contexto (String).
         * 
         * @param clave Clave de la propiedad
         * @param valor Valor de la propiedad como String
         * @return Builder para chaining
         */
        public Builder conPropiedad(String clave, String valor) {
            this.propiedades.put(clave, valor);
            return this;
        }
        
        /**
         * Agrega una propiedad dinámica al contexto (Integer).
         * 
         * @param clave Clave de la propiedad
         * @param valor Valor de la propiedad como Integer
         * @return Builder para chaining
         */
        public Builder conPropiedad(String clave, Integer valor) {
            this.propiedades.put(clave, valor);
            return this;
        }
        
        /**
         * Agrega una propiedad dinámica al contexto (Boolean).
         * 
         * @param clave Clave de la propiedad
         * @param valor Valor de la propiedad como Boolean
         * @return Builder para chaining
         */
        public Builder conPropiedad(String clave, Boolean valor) {
            this.propiedades.put(clave, valor);
            return this;
        }
        
        /**
         * Agrega una propiedad dinámica al contexto (Object genérico).
         * 
         * @param clave Clave de la propiedad
         * @param valor Valor de la propiedad como Object
         * @return Builder para chaining
         */
        public Builder conPropiedad(String clave, Object valor) {
            this.propiedades.put(clave, valor);
            return this;
        }
        
        /**
         * Agrega múltiples propiedades de una vez.
         */
        public Builder conPropiedades(Map<String, Object> propiedades) {
            this.propiedades.putAll(propiedades);
            return this;
        }
        
        public DescuentoContexto build() {
            // CORRECCIÓN: Establecer fechas por defecto si no se proporcionaron
            if (this.fechaCalculo == null) {
                this.fechaCalculo = LocalDateTime.now();
            }
            if (this.fechaCompra == null) {
                this.fechaCompra = LocalDateTime.now();
            }
            
            return new DescuentoContexto(this);
        }
    }
    
    // =============================================
    // FACTORY METHODS CON VALORES POR DEFECTO SENSIBLES
    // =============================================
    
    /**
     * Crea un builder con valores por defecto para uso en producción.
     * 
     * VALORES POR DEFECTO SENSIBLES:
     * - tipoUsuario: "NORMAL"
     * - canalVenta: "WEB"
     * - paisUsuario: "MX"
     * - fechaCompra: now()
     * - fechaCalculo: now()
     */
    public static Builder builderConDefectos() {
        return new Builder()
                .conTipoUsuario("NORMAL")
                .conCanalVenta("WEB")
                .conPaisUsuario("MX")
                .conFechaCompra(LocalDateTime.now())
                .conFechaCalculo(LocalDateTime.now());
    }
    
    /**
     * Crea un builder completamente vacío para testing.
     * Permite tests que verifican valores null.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Factory method para crear contexto de compra estándar.
     * 
     * @param cantidad Cantidad en carrito
     * @param tipoUsuario Tipo de usuario (VIP, NORMAL, etc.)
     * @return Contexto configurado para compra estándar
     */
    public static DescuentoContexto paraCompraEstandar(Integer cantidad, String tipoUsuario) {
        return builderConDefectos()
                .conCantidadEnCarrito(cantidad)
                .conTipoUsuario(tipoUsuario)
                .build();
    }
    
    /**
     * Factory method para crear contexto de testing.
     * 
     * @return Contexto mínimo para pruebas unitarias
     */
    public static DescuentoContexto paraTesting() {
        return builder()
                .conCantidadEnCarrito(1)
                .conFechaCalculo(LocalDateTime.now())
                .build();
    }
    
    @Override
    public String toString() {
        return String.format(
            "DescuentoContexto{cantidad=%d, tipoUsuario='%s', codigo='%s', canal='%s', propiedades=%d}",
            cantidadEnCarrito, tipoUsuario, codigoPromocional, canalVenta, propiedades.size()
        );
    }
    
    // =============================================
    // MÉTODOS DE UTILIDAD PARA DEBUGGING
    // =============================================
    
    /**
     * Genera un resumen detallado del contexto para logging.
     */
    public String generarResumenDetallado() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("DescuentoContexto {\n");
        resumen.append("  Cantidad en carrito: ").append(cantidadEnCarrito).append("\n");
        resumen.append("  Tipo de usuario: ").append(tipoUsuario).append("\n");
        resumen.append("  Código promocional: ").append(codigoPromocional).append("\n");
        resumen.append("  Canal de venta: ").append(canalVenta).append("\n");
        resumen.append("  Temporada: ").append(temporada).append("\n");
        resumen.append("  País: ").append(paisUsuario).append("\n");
        resumen.append("  Fecha compra: ").append(fechaCompra).append("\n");
        resumen.append("  Fecha cálculo: ").append(fechaCalculo).append("\n");
        
        if (!propiedades.isEmpty()) {
            resumen.append("  Propiedades adicionales:\n");
            propiedades.forEach((k, v) -> 
                resumen.append("    ").append(k).append(": ").append(v)
                       .append(" (").append(v != null ? v.getClass().getSimpleName() : "null").append(")\n"));
        }
        
        resumen.append("}");
        return resumen.toString();
    }
}