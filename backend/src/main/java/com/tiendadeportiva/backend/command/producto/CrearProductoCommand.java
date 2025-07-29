package com.tiendadeportiva.backend.command.producto;

import com.tiendadeportiva.backend.command.Command;
import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.event.ProductoCreadoEvent;
import com.tiendadeportiva.backend.exception.ProductoException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import com.tiendadeportiva.backend.service.DescuentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

/**
 * Comando para crear un nuevo producto.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA:
 * - Encapsula toda la lógica de creación de producto
 * - Permite transacciones complejas con rollback
 * - Facilita testing de operaciones específicas
 * - Preparación para arquitectura hexagonal
 */
public class CrearProductoCommand implements Command<Producto> {
    
    private static final Logger logger = LoggerFactory.getLogger(CrearProductoCommand.class);
    
    private final Producto producto;
    private final ProductoRepository repository;
    private final DescuentoService descuentoService;
    private final ApplicationEventPublisher eventPublisher;
    private final String usuarioCreador;
    
    // Para rollback si es necesario
    private Producto productoCreado;
    
    public CrearProductoCommand(
            Producto producto,
            ProductoRepository repository,
            DescuentoService descuentoService,
            ApplicationEventPublisher eventPublisher,
            String usuarioCreador) {
        this.producto = producto;
        this.repository = repository;
        this.descuentoService = descuentoService;
        this.eventPublisher = eventPublisher;
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
            
            // 4. Persistir
            productoCreado = repository.save(producto);
            logger.info("✅ Producto persistido con ID: {}", productoCreado.getId());
            
            // 5. Publicar eventos
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
    
    @Override
    public void undo() throws CommandExecutionException {
        if (productoCreado != null) {
            try {
                logger.info("🔄 Revirtiendo CrearProductoCommand para producto ID: {}", 
                           productoCreado.getId());
                
                // Soft delete para mantener trazabilidad
                productoCreado.setActivo(false);
                repository.save(productoCreado);
                
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
            
            // Validar duplicados
            boolean existe = repository.existsByNombreAndMarcaAndActivoTrue(
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
        producto.setActivo(true);
        
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
     * Publica eventos relacionados
     */
    private void publicarEventos() {
        try {
            ProductoCreadoEvent evento = new ProductoCreadoEvent(
                this, productoCreado, usuarioCreador
            );
            eventPublisher.publishEvent(evento);
            
            logger.info("📡 Evento ProductoCreadoEvent publicado");
            
        } catch (Exception e) {
            logger.error("❌ Error publicando eventos: {}", e.getMessage(), e);
            // No fallar el comando por eventos, pero logear el problema
        }
    }
}