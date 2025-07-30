package com.tiendadeportiva.backend.command.producto;

import com.tiendadeportiva.backend.command.Command;
import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.domain.port.EventPublisherPort;
import com.tiendadeportiva.backend.domain.port.ProductoRepositoryPort;
import com.tiendadeportiva.backend.event.ProductoCreadoEvent;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.service.DescuentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Comando para crear un nuevo producto usando puertos de dominio.
 * 
 * ARQUITECTURA HEXAGONAL COMPLETADA:
 * - Usa puertos en lugar de implementaciones concretas
 * - Independiente de Spring Data JPA
 * - Independiente de Spring Events
 * - Testeable con mocks simples
 */
public class CrearProductoCommand implements Command<Producto> {
    
    private static final Logger logger = LoggerFactory.getLogger(CrearProductoCommand.class);
    
    private final Producto producto;
    private final ProductoRepositoryPort repositoryPort; // 🎯 PUERTO EN LUGAR DE IMPLEMENTACIÓN
    private final DescuentoService descuentoService;
    private final EventPublisherPort eventPublisherPort; // 🎯 PUERTO EN LUGAR DE IMPLEMENTACIÓN
    private final String usuarioCreador;
    
    // Para rollback si es necesario
    private Producto productoCreado;
    
    public CrearProductoCommand(
            Producto producto,
            ProductoRepositoryPort repositoryPort,
            DescuentoService descuentoService,
            EventPublisherPort eventPublisherPort,
            String usuarioCreador) {
        this.producto = producto;
        this.repositoryPort = repositoryPort;
        this.descuentoService = descuentoService;
        this.eventPublisherPort = eventPublisherPort;
        this.usuarioCreador = usuarioCreador;
    }
    
    @Override
    public Producto execute() throws CommandExecutionException {
        try {
            logger.info("🚀 Ejecutando CrearProductoCommand para: {}", producto.getNombre());
            
            // 1. Validaciones previas
            if (!isValid()) {
                throw new CommandExecutionException(
                    "CrearProductoCommand", 
                    "VALIDATION_FAILED", 
                    "Validación del comando falló"
                );
            }
            
            // 2. Preparar producto
            prepararProducto();
            
            // 3. Aplicar reglas de negocio
            aplicarReglasNegocio();
            
            // 4. Persistir usando puerto
            productoCreado = repositoryPort.save(producto);
            logger.info("✅ Producto persistido con ID: {}", productoCreado.getId());
            
            // 5. Publicar eventos usando puerto
            publicarEventos();
            
            logger.info("🎉 CrearProductoCommand ejecutado exitosamente para: {}", 
                       productoCreado.getNombre());
            
            return productoCreado;
            
        } catch (Exception e) {
            logger.error("❌ Error ejecutando CrearProductoCommand: {}", e.getMessage(), e);
            throw new CommandExecutionException(
                "CrearProductoCommand", 
                "EXECUTION_FAILED", 
                "Error ejecutando comando: " + e.getMessage(), 
                e
            );
        }
    }
    
    /**
     * Corregir método undo() - línea aproximada 97
     */
    @Override
    public void undo() throws CommandExecutionException {
        if (productoCreado != null) {
            try {
                logger.info("🔄 Revirtiendo CrearProductoCommand para producto ID: {}", 
                           productoCreado.getId());
                
                // ✅ CORRECCIÓN: Boolean wrapper
                productoCreado.setActivo(Boolean.FALSE);
                repositoryPort.save(productoCreado);
                
                logger.info("✅ CrearProductoCommand revertido exitosamente");
                
            } catch (Exception e) {
                throw new CommandExecutionException(
                    "CrearProductoCommand", 
                    "UNDO_FAILED", 
                    "Error revirtiendo comando: " + e.getMessage(), 
                    e
                );
            }
        }
    }
    
    @Override
    public boolean isValid() {
        try {
            // Validaciones básicas
            if (producto == null) {
                logger.warn("⚠️ Producto es null");
                return false;
            }
            
            if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                logger.warn("⚠️ Nombre del producto es requerido");
                return false;
            }
            
            if (producto.getPrecio() == null || producto.getPrecio().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                logger.warn("⚠️ Precio del producto debe ser mayor que 0");
                return false;
            }
            
            // Validar duplicados usando puerto
            boolean existe = repositoryPort.existsActiveByNameAndBrand(
                producto.getNombre(), producto.getMarca()
            );
            
            if (existe) {
                logger.warn("⚠️ Ya existe un producto con nombre '{}' y marca '{}'", 
                           producto.getNombre(), producto.getMarca());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error validando comando: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getDescription() {
        return String.format("Crear producto '%s' de marca '%s' por usuario '%s'", 
                           producto.getNombre(), producto.getMarca(), usuarioCreador);
    }
    
    /**
     * Prepara el producto con datos básicos
     */
    private void prepararProducto() {
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setActivo(Boolean.TRUE); // ✅ CORRECCIÓN: Boolean wrapper
        
        // Asegurar que no tenga ID (es creación)
        producto.setId(null);
    }
    
    /**
     * Aplica reglas de negocio específicas
     */
    private void aplicarReglasNegocio() {
        // Aplicar descuentos automáticos
        DescuentoService.DescuentoInfo descuentoInfo = 
            descuentoService.aplicarDescuentosAutomaticos(producto);
        
        if (descuentoInfo.tieneDescuentos()) {
            logger.info("💰 Descuentos aplicados: {} | Ahorro: ${}", 
                       descuentoInfo.getDescuentosAplicados(), 
                       descuentoInfo.getTotalDescuento());
        }
    }
    
    /**
     * Publica eventos relacionados usando puerto
     */
    private void publicarEventos() {
        try {
            ProductoCreadoEvent evento = new ProductoCreadoEvent(
                this, productoCreado, usuarioCreador
            );
            eventPublisherPort.publishEvent(evento);
            
            logger.info("📡 Evento ProductoCreadoEvent publicado");
            
        } catch (Exception e) {
            logger.error("❌ Error publicando eventos: {}", e.getMessage(), e);
            // No fallar el comando por eventos, pero logear el problema
        }
    }
}