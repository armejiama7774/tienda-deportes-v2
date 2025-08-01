package com.tiendadeportiva.backend.factory.impl;

import com.tiendadeportiva.backend.factory.ProductoCreationException;
import com.tiendadeportiva.backend.factory.ProductoCreationRequest;
import com.tiendadeportiva.backend.factory.ProductoFactory;
import com.tiendadeportiva.backend.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Factory base abstracto que implementa validaciones comunes y lógica compartida.
 * 
 * PATRÓN TEMPLATE METHOD + FACTORY:
 * - Define el algoritmo general de creación de productos
 * - Permite que las subclases especialicen pasos específicos
 * - Centraliza validaciones comunes
 * - Proporciona hooks para customización
 * 
 * BENEFICIOS PROFESIONALES:
 * - Reduce código duplicado entre factories
 * - Garantiza validaciones consistentes
 * - Facilita mantenimiento y testing
 * - Permite extensión controlada
 */
public abstract class BaseProductoFactory implements ProductoFactory {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseProductoFactory.class);
    
    @Override
    public final Producto crearProducto(ProductoCreationRequest request) {
        logger.debug("🏭 Iniciando creación de producto con factory: {}", this.getClass().getSimpleName());
        
        try {
            // PASO 1: Validaciones generales
            validarDatosBasicos(request);
            
            // PASO 2: Validaciones específicas del tipo (implementado por subclases)
            validarDatos(request);
            
            // PASO 3: Crear producto base
            Producto producto = crearProductoBase(request);
            
            // PASO 4: Aplicar configuraciones específicas del tipo
            aplicarConfiguracionesDefecto(producto, request);
            
            // PASO 5: Aplicar configuraciones especializadas (implementado por subclases)
            aplicarConfiguracionesEspecializadas(producto, request);
            
            // PASO 6: Validación final
            validarProductoCreado(producto);
            
            logger.info("✅ Producto creado exitosamente: {} (Tipo: {}, Factory: {})", 
                       producto.getNombre(), request.getTipo(), this.getClass().getSimpleName());
            
            return producto;
            
        } catch (ProductoCreationException e) {
            logger.error("❌ Error en factory {}: {}", this.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ Error inesperado en factory {}: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            throw new ProductoCreationException(
                this.getClass().getSimpleName(),
                request.getTipo(),
                "CREATION_ERROR",
                "Error inesperado creando producto: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Validaciones básicas comunes a todos los tipos de productos.
     */
    protected void validarDatosBasicos(ProductoCreationRequest request) {
        if (request == null) {
            throw new ProductoCreationException(
                this.getClass().getSimpleName(), 
                "UNKNOWN", 
                "NULL_REQUEST", 
                "La petición de creación no puede ser null"
            );
        }
        
        // Validar nombre
        if (!StringUtils.hasText(request.getNombre())) {
            throw new ProductoCreationException(
                this.getClass().getSimpleName(),
                request.getTipo(),
                "INVALID_NAME",
                "El nombre del producto es obligatorio"
            );
        }
        
        if (request.getNombre().length() > 100) {
            throw new ProductoCreationException(
                this.getClass().getSimpleName(),
                request.getTipo(),
                "NAME_TOO_LONG",
                "El nombre no puede exceder 100 caracteres"
            );
        }
        
        // Validar precio
        if (request.getPrecio() == null || request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductoCreationException(
                this.getClass().getSimpleName(),
                request.getTipo(),
                "INVALID_PRICE",
                "El precio debe ser mayor a cero"
            );
        }
        
        // Validar categoría
        if (!StringUtils.hasText(request.getCategoria())) {
            throw new ProductoCreationException(
                this.getClass().getSimpleName(),
                request.getTipo(),
                "INVALID_CATEGORY",
                "La categoría es obligatoria"
            );
        }
        
        // Validar que esta factory puede crear este tipo
        if (!puedeCrear(request.getCategoria(), request.getTipo())) {
            throw new ProductoCreationException(
                this.getClass().getSimpleName(),
                request.getTipo(),
                "UNSUPPORTED_TYPE",
                String.format("Esta factory no puede crear productos de tipo %s/%s", 
                            request.getCategoria(), request.getTipo())
            );
        }
    }
    
    /**
     * Crea el producto base con las propiedades comunes.
     */
    protected Producto crearProductoBase(ProductoCreationRequest request) {
        Producto producto = new Producto();
        
        // Propiedades básicas
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCategoria(request.getCategoria());
        producto.setMarca(request.getMarca());
        producto.setStockDisponible(request.getStockInicial() != null ? request.getStockInicial() : 0);
        
        // Metadatos
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setActivo(request.isActivarImmediatamente());
        
        return producto;
    }
    
    @Override
    public void aplicarConfiguracionesDefecto(Producto producto, ProductoCreationRequest request) {
        // Configuraciones comunes por defecto
        
        // Si no se especifica descripción, crear una básica
        if (!StringUtils.hasText(producto.getDescripcion())) {
            producto.setDescripcion(String.format("%s de marca %s", 
                                                producto.getNombre(), 
                                                producto.getMarca()));
        }
        
        // Activar por defecto si no se especifica
        if (request.isActivarImmediatamente()) {
            producto.setActivo(true);
        }
    }
    
    /**
     * Validación final del producto creado antes de devolverlo.
     */
    protected void validarProductoCreado(Producto producto) {
        if (producto == null) {
            throw new ProductoCreationException(
                this.getClass().getSimpleName(),
                "UNKNOWN",
                "NULL_PRODUCT",
                "El producto creado no puede ser null"
            );
        }
        
        if (!StringUtils.hasText(producto.getNombre()) || 
            !StringUtils.hasText(producto.getCategoria()) ||
            producto.getPrecio() == null) {
            throw new ProductoCreationException(
                this.getClass().getSimpleName(),
                producto.getCategoria(),
                "INCOMPLETE_PRODUCT",
                "El producto creado no tiene todas las propiedades requeridas"
            );
        }
    }
    
    // ===============================================
    // MÉTODOS ABSTRACTOS PARA ESPECIALIZACIÓN
    // ===============================================
    
    /**
     * Aplica configuraciones específicas del tipo de producto.
     * Implementado por cada factory especializada.
     */
    protected abstract void aplicarConfiguracionesEspecializadas(Producto producto, ProductoCreationRequest request);
    
    /**
     * Obtiene el nombre descriptivo de esta factory para logging.
     */
    protected abstract String getFactoryName();
}
