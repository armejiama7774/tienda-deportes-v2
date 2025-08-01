package com.tiendadeportiva.backend.factory;

import com.tiendadeportiva.backend.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manager central del Factory Pattern que coordina todas las factories especializadas.
 * 
 * PATRÓN FACTORY MANAGER (CHAIN OF FACTORIES):
 * - Coordina múltiples factories especializadas
 * - Selecciona automáticamente la factory apropiada
 * - Proporciona una interfaz unificada para creación de productos
 * - Maneja fallbacks y estrategias de error
 * 
 * VENTAJAS PROFESIONALES:
 * - Punto único de entrada para creación de productos
 * - Desacoplamiento entre cliente y factories específicas
 * - Facilita testing con mocks
 * - Permite configuración dinámica de factories
 * - Logging centralizado de operaciones de factory
 */
@Service
public class ProductoFactoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoFactoryManager.class);
    
    private final List<ProductoFactory> factories;
    
    public ProductoFactoryManager(List<ProductoFactory> factories) {
        this.factories = factories;
        logger.info("🏭 ProductoFactoryManager inicializado con {} factories", factories.size());
        
        // Log de factories disponibles
        for (ProductoFactory factory : factories) {
            logger.debug("   📋 Factory registrada: {} - Categoría: {}, Tipos: {}", 
                        factory.getClass().getSimpleName(),
                        factory.getCategoriaPrincipal(),
                        String.join(", ", factory.getTiposSoportados()));
        }
    }
    
    /**
     * Crea un producto seleccionando automáticamente la factory apropiada.
     * 
     * @param request Datos completos para crear el producto
     * @return Producto creado y configurado
     * @throws ProductoCreationException Si no se puede crear el producto
     */
    public Producto crearProducto(ProductoCreationRequest request) {
        logger.info("🏭 Solicitud de creación de producto: {} (Categoría: {}, Tipo: {})", 
                   request.getNombre(), request.getCategoria(), request.getTipo());
        
        // Buscar factory apropiada
        ProductoFactory factorySeleccionada = seleccionarFactory(request.getCategoria(), request.getTipo());
        
        if (factorySeleccionada == null) {
            String tiposDisponibles = obtenerTiposDisponibles();
            throw new ProductoCreationException(
                "ProductoFactoryManager",
                request.getTipo(),
                "NO_FACTORY_FOUND",
                String.format("No se encontró factory para categoría '%s' y tipo '%s'. Tipos disponibles: %s",
                            request.getCategoria(), request.getTipo(), tiposDisponibles)
            );
        }
        
        logger.debug("✅ Factory seleccionada: {} para {}/{}", 
                    factorySeleccionada.getClass().getSimpleName(),
                    request.getCategoria(), request.getTipo());
        
        try {
            // Delegar creación a la factory especializada
            Producto producto = factorySeleccionada.crearProducto(request);
            
            logger.info("🎉 Producto creado exitosamente: {} (ID: {}, Factory: {})",
                       producto.getNombre(), producto.getId(), 
                       factorySeleccionada.getClass().getSimpleName());
            
            return producto;
            
        } catch (ProductoCreationException e) {
            logger.error("❌ Error en factory {}: {}", 
                        factorySeleccionada.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ Error inesperado en factory manager: {}", e.getMessage(), e);
            throw new ProductoCreationException(
                "ProductoFactoryManager",
                request.getTipo(),
                "MANAGER_ERROR",
                "Error inesperado en factory manager: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Selecciona la factory apropiada para el tipo de producto solicitado.
     */
    private ProductoFactory seleccionarFactory(String categoria, String tipo) {
        logger.debug("🔍 Buscando factory para categoría: '{}', tipo: '{}'", categoria, tipo);
        
        for (ProductoFactory factory : factories) {
            if (factory.puedeCrear(categoria, tipo)) {
                logger.debug("✅ Factory encontrada: {} puede crear {}/{}", 
                           factory.getClass().getSimpleName(), categoria, tipo);
                return factory;
            }
        }
        
        logger.warn("❌ No se encontró factory para {}/{}", categoria, tipo);
        return null;
    }
    
    /**
     * Obtiene información sobre todas las factories disponibles.
     */
    public FactoryInfo getFactoryInfo() {
        FactoryInfo info = new FactoryInfo();
        
        for (ProductoFactory factory : factories) {
            FactoryInfo.FactoryDetails details = new FactoryInfo.FactoryDetails(
                factory.getClass().getSimpleName(),
                factory.getCategoriaPrincipal(),
                factory.getTiposSoportados()
            );
            info.addFactory(details);
        }
        
        return info;
    }
    
    /**
     * Verifica si se puede crear un producto del tipo especificado.
     */
    public boolean puedeCrear(String categoria, String tipo) {
        return seleccionarFactory(categoria, tipo) != null;
    }
    
    /**
     * Obtiene todos los tipos de productos que se pueden crear.
     */
    public String obtenerTiposDisponibles() {
        StringBuilder tipos = new StringBuilder();
        
        for (ProductoFactory factory : factories) {
            if (tipos.length() > 0) {
                tipos.append(", ");
            }
            tipos.append(factory.getCategoriaPrincipal())
                 .append("=[")
                 .append(String.join(", ", factory.getTiposSoportados()))
                 .append("]");
        }
        
        return tipos.toString();
    }
    
    /**
     * Valida una petición antes de intentar crear el producto.
     */
    public ValidationResult validarPeticion(ProductoCreationRequest request) {
        try {
            ProductoFactory factory = seleccionarFactory(request.getCategoria(), request.getTipo());
            
            if (factory == null) {
                return new ValidationResult(false, "No hay factory disponible para este tipo de producto");
            }
            
            // Intentar validación sin crear el producto
            factory.validarDatos(request);
            
            return new ValidationResult(true, "Validación exitosa");
            
        } catch (Exception e) {
            return new ValidationResult(false, "Error de validación: " + e.getMessage());
        }
    }
    
    // ===============================================
    // CLASES DE SOPORTE
    // ===============================================
    
    /**
     * Información sobre factories disponibles.
     */
    public static class FactoryInfo {
        private final List<FactoryDetails> factories = new java.util.ArrayList<>();
        
        public void addFactory(FactoryDetails details) {
            factories.add(details);
        }
        
        public List<FactoryDetails> getFactories() { return factories; }
        
        public static class FactoryDetails {
            private final String nombre;
            private final String categoria;
            private final String[] tipos;
            
            public FactoryDetails(String nombre, String categoria, String[] tipos) {
                this.nombre = nombre;
                this.categoria = categoria;
                this.tipos = tipos;
            }
            
            public String getNombre() { return nombre; }
            public String getCategoria() { return categoria; }
            public String[] getTipos() { return tipos; }
            
            @Override
            public String toString() {
                return String.format("%s: %s - %s", nombre, categoria, String.join(", ", tipos));
            }
        }
    }
    
    /**
     * Resultado de validación de peticiones.
     */
    public static class ValidationResult {
        private final boolean valida;
        private final String mensaje;
        
        public ValidationResult(boolean valida, String mensaje) {
            this.valida = valida;
            this.mensaje = mensaje;
        }
        
        public boolean isValida() { return valida; }
        public String getMensaje() { return mensaje; }
        
        @Override
        public String toString() {
            return String.format("ValidationResult{valida=%s, mensaje='%s'}", valida, mensaje);
        }
    }
}
