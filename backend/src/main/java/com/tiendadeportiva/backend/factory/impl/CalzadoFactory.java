package com.tiendadeportiva.backend.factory.impl;

import com.tiendadeportiva.backend.factory.ProductoCreationException;
import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Factory especializada para crear productos de calzado deportivo.
 * 
 * ESPECIALIZACIÓN DEL FACTORY PATTERN:
 * - Validaciones específicas para calzado (tallas, materiales, tipos)
 * - Configuraciones automáticas según el tipo de calzado
 * - Cálculo de precios base según características
 * - Propiedades extendidas específicas de calzado
 * 
 * TIPOS DE CALZADO SOPORTADOS:
 * - RUNNING: Zapatillas para correr
 * - CASUAL: Calzado informal deportivo  
 * - FUTBOL: Tacos y botas de fútbol
 * - BASKETBALL: Zapatillas de baloncesto
 * - TRAINING: Calzado para entrenamiento
 * - HIKING: Botas y zapatillas de senderismo
 */
@Component
public class CalzadoFactory extends BaseProductoFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(CalzadoFactory.class);
    
    // Constantes específicas de calzado
    private static final String CATEGORIA_CALZADO = "CALZADO";
    private static final String[] TIPOS_SOPORTADOS = {
        "RUNNING", "CASUAL", "FUTBOL", "BASKETBALL", "TRAINING", "HIKING"
    };
    
    private static final List<String> TALLAS_VALIDAS = Arrays.asList(
        "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48"
    );
    
    private static final List<String> MATERIALES_VALIDOS = Arrays.asList(
        "CUERO", "SINTETICO", "MESH", "CANVAS", "TEXTIL", "GORE_TEX", "NEOPRENO"
    );
    
    @Override
    public boolean puedeCrear(String categoria, String tipo) {
        return CATEGORIA_CALZADO.equalsIgnoreCase(categoria) && 
               (tipo == null || Arrays.asList(TIPOS_SOPORTADOS).contains(tipo.toUpperCase()));
    }
    
    @Override
    public String[] getTiposSoportados() {
        return TIPOS_SOPORTADOS.clone();
    }
    
    @Override
    public String getCategoriaPrincipal() {
        return CATEGORIA_CALZADO;
    }
    
    @Override
    public void validarDatos(ProductoCreationRequest request) {
        logger.debug("🔍 Validando datos específicos de calzado para: {}", request.getNombre());
        
        // Validar tipo específico
        if (StringUtils.hasText(request.getTipo())) {
            String tipoUpper = request.getTipo().toUpperCase();
            if (!Arrays.asList(TIPOS_SOPORTADOS).contains(tipoUpper)) {
                throw new ProductoCreationException(
                    getFactoryName(),
                    request.getTipo(),
                    "INVALID_SHOE_TYPE",
                    String.format("Tipo de calzado no válido: %s. Tipos soportados: %s", 
                                request.getTipo(), Arrays.toString(TIPOS_SOPORTADOS))
                );
            }
        }
        
        // Validar talla si se proporciona
        if (StringUtils.hasText(request.getTalla())) {
            if (!TALLAS_VALIDAS.contains(request.getTalla())) {
                throw new ProductoCreationException(
                    getFactoryName(),
                    request.getTipo(),
                    "INVALID_SIZE",
                    String.format("Talla no válida: %s. Tallas válidas: %s", 
                                request.getTalla(), TALLAS_VALIDAS)
                );
            }
        }
        
        // Validar material si se proporciona
        if (StringUtils.hasText(request.getMaterial())) {
            String materialUpper = request.getMaterial().toUpperCase();
            if (!MATERIALES_VALIDOS.contains(materialUpper)) {
                logger.warn("⚠️ Material no estándar para calzado: {}", request.getMaterial());
            }
        }
        
        // Validaciones de precio según tipo
        validarPrecioSegunTipo(request);
    }
    
    @Override
    protected void aplicarConfiguracionesEspecializadas(Producto producto, ProductoCreationRequest request) {
        logger.debug("🔧 Aplicando configuraciones especializadas de calzado para: {}", producto.getNombre());
        
        // Aplicar configuraciones por defecto según el tipo
        aplicarConfiguracionesPorTipo(producto, request);
        
        // Ajustar descripción con detalles específicos
        mejorarDescripcion(producto, request);
        
        // Calcular precio sugerido basado en características
        ajustarPrecioSegunCaracteristicas(producto, request);
        
        // Configurar stock inicial según tipo
        configurarStockInicial(producto, request);
    }
    
    /**
     * Aplica configuraciones específicas según el tipo de calzado.
     */
    private void aplicarConfiguracionesPorTipo(Producto producto, ProductoCreationRequest request) {
        String tipo = request.getTipo() != null ? request.getTipo().toUpperCase() : "";
        
        switch (tipo) {
            case "RUNNING":
                aplicarConfiguracionRunning(producto, request);
                break;
            case "FUTBOL":
                aplicarConfiguracionFutbol(producto, request);
                break;
            case "BASKETBALL":
                aplicarConfiguracionBasketball(producto, request);
                break;
            case "HIKING":
                aplicarConfiguracionHiking(producto, request);
                break;
            default:
                aplicarConfiguracionGeneral(producto, request);
                break;
        }
    }
    
    private void aplicarConfiguracionRunning(Producto producto, ProductoCreationRequest request) {
        // Running shoes necesitan características específicas
        if (!StringUtils.hasText(request.getMaterial())) {
            // Material por defecto para running
            if (request.getPropiedadesExtendidas() == null) {
                request.setPropiedadesExtendidas(new java.util.HashMap<>());
            }
            request.getPropiedadesExtendidas().put("material_superior", "MESH");
            request.getPropiedadesExtendidas().put("tipo_suela", "EVA");
        }
        
        // Características típicas de running
        if (request.getPropiedadesExtendidas() != null) {
            request.getPropiedadesExtendidas().put("amortiguacion", "ALTA");
            request.getPropiedadesExtendidas().put("transpirabilidad", "ALTA");
            request.getPropiedadesExtendidas().put("peso_aproximado", "250-300g");
        }
    }
    
    private void aplicarConfiguracionFutbol(Producto producto, ProductoCreationRequest request) {
        // Configuraciones específicas de fútbol
        if (request.getPropiedadesExtendidas() == null) {
            request.setPropiedadesExtendidas(new java.util.HashMap<>());
        }
        
        String subtipo = request.getSubtipo() != null ? request.getSubtipo().toUpperCase() : "CESPED";
        
        switch (subtipo) {
            case "CESPED":
                request.getPropiedadesExtendidas().put("tipo_tacos", "FG"); // Firm Ground
                break;
            case "SINTETICO":
                request.getPropiedadesExtendidas().put("tipo_tacos", "TF"); // Turf
                break;
            case "SALON":
                request.getPropiedadesExtendidas().put("tipo_suela", "LISA");
                break;
        }
        
        request.getPropiedadesExtendidas().put("material_exterior", "CUERO_SINTETICO");
    }
    
    private void aplicarConfiguracionBasketball(Producto producto, ProductoCreationRequest request) {
        // Configuraciones para basketball
        if (request.getPropiedadesExtendidas() == null) {
            request.setPropiedadesExtendidas(new java.util.HashMap<>());
        }
        
        request.getPropiedadesExtendidas().put("altura_corte", "MID_TOP");
        request.getPropiedadesExtendidas().put("soporte_tobillo", "ALTO");
        request.getPropiedadesExtendidas().put("traccion", "MULTIDIRECCIONAL");
    }
    
    private void aplicarConfiguracionHiking(Producto producto, ProductoCreationRequest request) {
        // Configuraciones para senderismo
        if (request.getPropiedadesExtendidas() == null) {
            request.setPropiedadesExtendidas(new java.util.HashMap<>());
        }
        
        request.getPropiedadesExtendidas().put("impermeabilidad", "ALTA");
        request.getPropiedadesExtendidas().put("resistencia_abrasion", "ALTA");
        request.getPropiedadesExtendidas().put("tipo_suela", "VIBRAM");
    }
    
    private void aplicarConfiguracionGeneral(Producto producto, ProductoCreationRequest request) {
        // Configuraciones generales para calzado casual o training
        if (request.getPropiedadesExtendidas() == null) {
            request.setPropiedadesExtendidas(new java.util.HashMap<>());
        }
        
        request.getPropiedadesExtendidas().put("versatilidad", "ALTA");
        request.getPropiedadesExtendidas().put("comodidad", "ALTA");
    }
    
    /**
     * Mejora la descripción con detalles específicos del calzado.
     */
    private void mejorarDescripcion(Producto producto, ProductoCreationRequest request) {
        StringBuilder descripcionMejorada = new StringBuilder(producto.getDescripcion());
        
        if (StringUtils.hasText(request.getTipo())) {
            descripcionMejorada.append(" - Tipo: ").append(request.getTipo());
        }
        
        if (StringUtils.hasText(request.getTalla())) {
            descripcionMejorada.append(" - Talla: ").append(request.getTalla());
        }
        
        if (StringUtils.hasText(request.getMaterial())) {
            descripcionMejorada.append(" - Material: ").append(request.getMaterial());
        }
        
        producto.setDescripcion(descripcionMejorada.toString());
    }
    
    /**
     * Valida que el precio esté en rangos apropiados según el tipo.
     */
    private void validarPrecioSegunTipo(ProductoCreationRequest request) {
        BigDecimal precio = request.getPrecio();
        String tipo = request.getTipo() != null ? request.getTipo().toUpperCase() : "";
        
        BigDecimal precioMinimo = BigDecimal.ZERO;
        BigDecimal precioMaximo = new BigDecimal("1000");
        
        switch (tipo) {
            case "RUNNING":
                precioMinimo = new BigDecimal("50");
                precioMaximo = new BigDecimal("300");
                break;
            case "FUTBOL":
                precioMinimo = new BigDecimal("40");
                precioMaximo = new BigDecimal("250");
                break;
            case "BASKETBALL":
                precioMinimo = new BigDecimal("60");
                precioMaximo = new BigDecimal("200");
                break;
            case "HIKING":
                precioMinimo = new BigDecimal("80");
                precioMaximo = new BigDecimal("400");
                break;
        }
        
        if (precio.compareTo(precioMinimo) < 0 || precio.compareTo(precioMaximo) > 0) {
            logger.warn("⚠️ Precio fuera del rango típico para {}: ${} (Rango: ${}-${})", 
                       tipo, precio, precioMinimo, precioMaximo);
        }
    }
    
    /**
     * Ajusta el precio basado en características específicas.
     */
    private void ajustarPrecioSegunCaracteristicas(Producto producto, ProductoCreationRequest request) {
        // Este es un ejemplo de lógica de pricing automático
        // En un sistema real, esto podría conectarse con un servicio de pricing
        
        if (request.isAplicarDescuentoLanzamiento()) {
            BigDecimal descuento = producto.getPrecio().multiply(new BigDecimal("0.10")); // 10% descuento
            BigDecimal precioConDescuento = producto.getPrecio().subtract(descuento);
            
            logger.info("💰 Aplicando descuento de lanzamiento: ${} -> ${}", 
                       producto.getPrecio(), precioConDescuento);
            
            // En lugar de modificar el precio, podríamos guardar el descuento como metadato
            if (request.getPropiedadesExtendidas() == null) {
                request.setPropiedadesExtendidas(new java.util.HashMap<>());
            }
            request.getPropiedadesExtendidas().put("descuento_lanzamiento", descuento);
            request.getPropiedadesExtendidas().put("precio_original", producto.getPrecio());
        }
    }
    
    /**
     * Configura el stock inicial según el tipo de producto.
     */
    private void configurarStockInicial(Producto producto, ProductoCreationRequest request) {
        if (request.getStockInicial() == null || request.getStockInicial() == 0) {
            // Stock por defecto según tipo
            int stockDefecto = 10; // Valor base
            
            String tipo = request.getTipo() != null ? request.getTipo().toUpperCase() : "";
            switch (tipo) {
                case "RUNNING":
                    stockDefecto = 15; // Mayor rotación
                    break;
                case "FUTBOL":
                    stockDefecto = 20; // Muy popular
                    break;
                case "HIKING":
                    stockDefecto = 8;  // Menor rotación
                    break;
            }
            
            producto.setStockDisponible(stockDefecto);
            logger.debug("📦 Stock inicial configurado automáticamente: {} unidades", stockDefecto);
        }
    }
    
    @Override
    protected String getFactoryName() {
        return "CalzadoFactory";
    }
}
