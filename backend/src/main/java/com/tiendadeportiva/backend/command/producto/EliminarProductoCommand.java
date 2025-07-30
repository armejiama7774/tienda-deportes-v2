package com.tiendadeportiva.backend.command.producto;

import com.tiendadeportiva.backend.command.Command;
import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.domain.port.EventPublisherPort;
import com.tiendadeportiva.backend.domain.port.ProductoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Comando para eliminar un producto (soft delete).
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2:
 * - Implementa soft delete para mantener trazabilidad
 * - Permite rollback completo
 * - Valida que el producto se pueda eliminar
 */
public class EliminarProductoCommand implements Command<Boolean> {
    
    private static final Logger logger = LoggerFactory.getLogger(EliminarProductoCommand.class);
    
    private final Long productoId;
    private final ProductoRepositoryPort repositoryPort;     // 🎯 CAMBIO
    private final EventPublisherPort eventPublisherPort;     // 🎯 CAMBIO
    private final String usuarioEliminador;
    
    // Para rollback
    private Producto productoEliminado;
    
    public EliminarProductoCommand(
            Long productoId,
            ProductoRepositoryPort repositoryPort,    // 🎯 CAMBIO: Puerto en lugar de implementación
            EventPublisherPort eventPublisherPort,    // 🎯 CAMBIO: Puerto en lugar de implementación
            String usuarioEliminador) {
        this.productoId = productoId;
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.usuarioEliminador = usuarioEliminador;
    }
    
    @Override
    public Boolean execute() throws CommandExecutionException {
        try {
            logger.info("🗑️ Ejecutando EliminarProductoCommand para ID: {}", productoId);
            
            // 1. Obtener producto
            Optional<Producto> productoOpt = repositoryPort.findById(productoId);
            if (productoOpt.isEmpty()) {
                throw new CommandExecutionException(
                    "EliminarProductoCommand",
                    "PRODUCTO_NO_ENCONTRADO",
                    "No se encontró producto con ID: " + productoId
                );
            }
            
            Producto producto = productoOpt.get();
            
            // 2. Validar que se puede eliminar
            if (!sePuedeEliminar(producto)) {
                throw new CommandExecutionException(
                    "EliminarProductoCommand",
                    "PRODUCTO_NO_ELIMINABLE",
                    "El producto no se puede eliminar en su estado actual"
                );
            }
        
            // 3. Mantener estado para rollback
            productoEliminado = clonarProducto(producto);
            
            // 4. Soft delete
            producto.setActivo(Boolean.FALSE); // ✅ CORRECCIÓN: Boolean wrapper
            producto.setFechaModificacion(LocalDateTime.now()); // ✅ CORRECCIÓN: Nombre consistente
            
            // 5. Persistir
            repositoryPort.save(producto);
            
            // 6. Publicar eventos
            publicarEventosEliminacion(producto);
            
            logger.info("✅ Producto eliminado exitosamente: {}", producto.getNombre());
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error eliminando producto ID {}: {}", productoId, e.getMessage(), e);
            throw new CommandExecutionException(
                "EliminarProductoCommand",
                "EXECUTION_FAILED",
                "Error eliminando producto: " + e.getMessage(),
                e
            );
        }
    }
    
    @Override
    public void undo() throws CommandExecutionException {
        if (productoEliminado != null) {
            try {
                logger.info("🔄 Revirtiendo EliminarProductoCommand para ID: {}", productoId);
                
                // Restaurar producto activo
                productoEliminado.setActivo(Boolean.TRUE);
                repositoryPort.save(productoEliminado);
                
                logger.info("✅ Eliminación revertida exitosamente");
                
            } catch (Exception e) {
                throw new CommandExecutionException(
                    "EliminarProductoCommand",
                    "UNDO_FAILED",
                    "Error revirtiendo eliminación: " + e.getMessage(),
                    e
                );
            }
        }
    }
    
    @Override
    public boolean isValid() {
        return productoId != null && productoId > 0;
    }
    
    @Override
    public String getDescription() {
        return String.format("Eliminar producto ID '%s' por usuario '%s'", 
                           productoId, usuarioEliminador);
    }
    
    /**
     * Valida si el producto se puede eliminar
     */
    private boolean sePuedeEliminar(Producto producto) {
        // Ya está eliminado
        if (!Boolean.TRUE.equals(producto.isActivo())) { // ✅ CORRECCIÓN: Safe Boolean comparison
            logger.warn("⚠️ El producto ya está eliminado");
            return false;
        }
        
        return true;
    }
    
    /**
     * Publica eventos relacionados con la eliminación
     */
    private void publicarEventosEliminacion(Producto producto) {
        // TODO: Implementar ProductoEliminadoEvent
        logger.info("📡 Eventos de eliminación pendientes de implementar");
    }
    
    /**
     * Clona un producto para rollback
     */
    private Producto clonarProducto(Producto original) {
        Producto clon = new Producto();
        clon.setId(original.getId());
        clon.setNombre(original.getNombre());
        clon.setDescripcion(original.getDescripcion());
        clon.setPrecio(original.getPrecio());
        clon.setCategoria(original.getCategoria());
        clon.setMarca(original.getMarca());
        clon.setStockDisponible(original.getStockDisponible());
        clon.setActivo(original.isActivo()); // ✅ Boolean wrapper
        clon.setFechaCreacion(original.getFechaCreacion());
        clon.setFechaModificacion(original.getFechaModificacion()); // ✅ CORRECCIÓN: Nombre consistente
        return clon;
    }
}