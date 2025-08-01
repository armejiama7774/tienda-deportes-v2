package com.tiendadeportiva.backend.factory;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO para encapsular todos los datos necesarios para crear un producto.
 * 
 * PATRÓN FACTORY - DATA TRANSFER OBJECT:
 * Encapsula todos los parámetros necesarios para la creación de productos,
 * incluyendo propiedades específicas por tipo y metadatos adicionales.
 * 
 * VENTAJAS:
 * - Evita constructores con muchos parámetros
 * - Permite propiedades opcionales y específicas por tipo
 * - Facilita validación centralizada
 * - Extensible para futuras necesidades
 */
public class ProductoCreationRequest {
    
    // ===============================================
    // PROPIEDADES BÁSICAS (OBLIGATORIAS)
    // ===============================================
    
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String categoria;
    private String marca;
    private Integer stockInicial;
    
    // ===============================================
    // PROPIEDADES ESPECÍFICAS POR TIPO
    // ===============================================
    
    private String tipo;                    // Tipo específico (ej: "RUNNING", "CASUAL", "PROFESIONAL")
    private String subtipo;                 // Subtipo más específico (ej: "TRAIL", "URBANO")
    private String genero;                  // "HOMBRE", "MUJER", "UNISEX", "NIÑO"
    private String talla;                   // Para ropa y calzado
    private String color;                   // Color principal
    private String material;                // Material principal
    private String temporada;               // "VERANO", "INVIERNO", "TODO_AÑO"
    
    // ===============================================
    // METADATOS Y CONFIGURACIÓN
    // ===============================================
    
    private String usuarioCreador;          // Usuario que solicita la creación
    private String canal;                   // "WEB", "MOBILE", "ADMIN", "API"
    private boolean aplicarDescuentoLanzamiento; // Si aplicar descuento automático
    private boolean activarImmediatamente;  // Si activar el producto al crearlo
    
    // ===============================================
    // PROPIEDADES EXTENDIDAS (FLEXIBLES)
    // ===============================================
    
    private Map<String, Object> propiedadesExtendidas; // Para propiedades específicas del tipo
    
    // ===============================================
    // CONSTRUCTORES
    // ===============================================
    
    public ProductoCreationRequest() {}
    
    /**
     * Constructor para propiedades básicas obligatorias
     */
    public ProductoCreationRequest(String nombre, String descripcion, BigDecimal precio, 
                                 String categoria, String marca, Integer stockInicial) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.stockInicial = stockInicial;
    }
    
    // ===============================================
    // BUILDER PATTERN PARA FACILITAR CONSTRUCCIÓN
    // ===============================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ProductoCreationRequest request = new ProductoCreationRequest();
        
        public Builder conNombre(String nombre) {
            request.nombre = nombre;
            return this;
        }
        
        public Builder conDescripcion(String descripcion) {
            request.descripcion = descripcion;
            return this;
        }
        
        public Builder conPrecio(BigDecimal precio) {
            request.precio = precio;
            return this;
        }
        
        public Builder conCategoria(String categoria) {
            request.categoria = categoria;
            return this;
        }
        
        public Builder conMarca(String marca) {
            request.marca = marca;
            return this;
        }
        
        public Builder conStockInicial(Integer stock) {
            request.stockInicial = stock;
            return this;
        }
        
        public Builder conTipo(String tipo) {
            request.tipo = tipo;
            return this;
        }
        
        public Builder conSubtipo(String subtipo) {
            request.subtipo = subtipo;
            return this;
        }
        
        public Builder conGenero(String genero) {
            request.genero = genero;
            return this;
        }
        
        public Builder conTalla(String talla) {
            request.talla = talla;
            return this;
        }
        
        public Builder conColor(String color) {
            request.color = color;
            return this;
        }
        
        public Builder conMaterial(String material) {
            request.material = material;
            return this;
        }
        
        public Builder conTemporada(String temporada) {
            request.temporada = temporada;
            return this;
        }
        
        public Builder conUsuarioCreador(String usuario) {
            request.usuarioCreador = usuario;
            return this;
        }
        
        public Builder conCanal(String canal) {
            request.canal = canal;
            return this;
        }
        
        public Builder aplicarDescuentoLanzamiento(boolean aplicar) {
            request.aplicarDescuentoLanzamiento = aplicar;
            return this;
        }
        
        public Builder activarImmediatamente(boolean activar) {
            request.activarImmediatamente = activar;
            return this;
        }
        
        public Builder conPropiedadExtendida(String clave, Object valor) {
            if (request.propiedadesExtendidas == null) {
                request.propiedadesExtendidas = new java.util.HashMap<>();
            }
            request.propiedadesExtendidas.put(clave, valor);
            return this;
        }
        
        public ProductoCreationRequest build() {
            return request;
        }
    }
    
    // ===============================================
    // GETTERS Y SETTERS
    // ===============================================
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public Integer getStockInicial() { return stockInicial; }
    public void setStockInicial(Integer stockInicial) { this.stockInicial = stockInicial; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getSubtipo() { return subtipo; }
    public void setSubtipo(String subtipo) { this.subtipo = subtipo; }
    
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    
    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    
    public String getTemporada() { return temporada; }
    public void setTemporada(String temporada) { this.temporada = temporada; }
    
    public String getUsuarioCreador() { return usuarioCreador; }
    public void setUsuarioCreador(String usuarioCreador) { this.usuarioCreador = usuarioCreador; }
    
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    
    public boolean isAplicarDescuentoLanzamiento() { return aplicarDescuentoLanzamiento; }
    public void setAplicarDescuentoLanzamiento(boolean aplicarDescuentoLanzamiento) { 
        this.aplicarDescuentoLanzamiento = aplicarDescuentoLanzamiento; 
    }
    
    public boolean isActivarImmediatamente() { return activarImmediatamente; }
    public void setActivarImmediatamente(boolean activarImmediatamente) { 
        this.activarImmediatamente = activarImmediatamente; 
    }
    
    public Map<String, Object> getPropiedadesExtendidas() { return propiedadesExtendidas; }
    public void setPropiedadesExtendidas(Map<String, Object> propiedadesExtendidas) { 
        this.propiedadesExtendidas = propiedadesExtendidas; 
    }
    
    // ===============================================
    // MÉTODOS DE UTILIDAD
    // ===============================================
    
    public Object getPropiedadExtendida(String clave) {
        return propiedadesExtendidas != null ? propiedadesExtendidas.get(clave) : null;
    }
    
    public boolean tienePropiedadExtendida(String clave) {
        return propiedadesExtendidas != null && propiedadesExtendidas.containsKey(clave);
    }
    
    @Override
    public String toString() {
        return String.format("ProductoCreationRequest{nombre='%s', categoria='%s', tipo='%s', marca='%s', precio=%s}", 
                           nombre, categoria, tipo, marca, precio);
    }
}
