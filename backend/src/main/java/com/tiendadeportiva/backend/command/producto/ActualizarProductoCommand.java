package com.tiendadeportiva.backend.command.producto;

import com.tiendadeportiva.backend.command.Command;
import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.domain.port.EventPublisherPort;
import com.tiendadeportiva.backend.domain.port.ProductoRepositoryPort;
import com.tiendadeportiva.backend.service.DescuentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Comando para actualizar un producto existente.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2:
 * - Encapsula lógica de actualización con validaciones
 * - Mantiene estado anterior para rollback
 * - Aplica reglas de negocio específicas de actualización
 */
public class ActualizarProductoCommand implements Command<Producto> {
    
    private static final Logger logger = LoggerFactory.getLogger(ActualizarProductoCommand.class);
    
    private final Long productoId;
    private final Producto datosActualizacion;
    private final ProductoRepositoryPort repositoryPort;     // 🎯 CAMBIO
    private final DescuentoService descuentoService;
    private final EventPublisherPort eventPublisherPort;     // 🎯 CAMBIO
    private final String usuarioModificador;
    
    // Para rollback
    private Producto estadoAnterior;
    private Producto estadoActualizado;
    
    public ActualizarProductoCommand(
            Long productoId,
            Producto datosActualizacion,
            ProductoRepositoryPort repositoryPort,    // 🎯 CAMBIO: Puerto en lugar de implementación
            DescuentoService descuentoService,
            EventPublisherPort eventPublisherPort,    // 🎯 CAMBIO: Puerto en lugar de implementación  
            String usuarioModificador) {
        this.productoId = productoId;
        this.datosActualizacion = datosActualizacion;
        this.repositoryPort = repositoryPort;
        this.descuentoService = descuentoService;
        this.eventPublisherPort = eventPublisherPort;
        this.usuarioModificador = usuarioModificador;
    }
    
    @Override
    public Producto execute() throws CommandExecutionException {
        try {
            logger.info("🔄 Ejecutando ActualizarProductoCommand para ID: {}", productoId);
            
            // 1. Obtener producto actual
            Optional<Producto> productoOpt = repositoryPort.findById(productoId);
            if (productoOpt.isEmpty() || !Boolean.TRUE.equals(productoOpt.get().isActivo())) { // ✅ CORRECCIÓN
                throw new CommandExecutionException(
                    "ActualizarProductoCommand",
                    "PRODUCTO_NO_ENCONTRADO",
                    "No se encontró producto activo con ID: " + productoId
                );
            }
            
            estadoAnterior = clonarProducto(productoOpt.get());
            Producto producto = productoOpt.get();
            
            // 2. Aplicar cambios
            aplicarCambios(producto, datosActualizacion);
            
            // 3. Validar estado final
            if (!esActualizacionValida(producto)) {
                throw new CommandExecutionException(
                    "ActualizarProductoCommand",
                    "ACTUALIZACION_INVALIDA",
                    "Los datos de actualización no son válidos"
                );
            }
            
            // 4. Aplicar reglas de negocio
            aplicarReglasActualizacion(producto);
            
            // 5. Persistir
            estadoActualizado = repositoryPort.save(producto);
            
            // 6. Publicar eventos si hubo cambios significativos
            publicarEventosSiNecesario(estadoAnterior, estadoActualizado);
            
            logger.info("✅ Producto actualizado exitosamente: {}", estadoActualizado.getNombre());
            return estadoActualizado;
            
        } catch (Exception e) {
            logger.error("❌ Error actualizando producto ID {}: {}", productoId, e.getMessage(), e);
            throw new CommandExecutionException(
                "ActualizarProductoCommand",
                "EXECUTION_FAILED",
                "Error actualizando producto: " + e.getMessage(),
                e
            );
        }
    }
    
    @Override
    public void undo() throws CommandExecutionException {
        if (estadoAnterior != null && estadoActualizado != null) {
            try {
                logger.info("🔄 Revirtiendo ActualizarProductoCommand para ID: {}", productoId);
                
                // Restaurar estado anterior
                repositoryPort.save(estadoAnterior);
                
                logger.info("✅ Actualización revertida exitosamente");
                
            } catch (Exception e) {
                throw new CommandExecutionException(
                    "ActualizarProductoCommand",
                    "UNDO_FAILED",
                    "Error revirtiendo actualización: " + e.getMessage(),
                    e
                );
            }
        }
    }
    
    @Override
    public boolean isValid() {
        return productoId != null && 
               productoId > 0 && 
               datosActualizacion != null;
    }
    
    @Override
    public String getDescription() {
        return String.format("Actualizar producto ID '%s' por usuario '%s'", 
                           productoId, usuarioModificador);
    }
    
    /**
     * Aplica los cambios del DTO al producto existente
     */
    private void aplicarCambios(Producto destino, Producto origen) {
        if (origen.getNombre() != null) {
            destino.setNombre(origen.getNombre());
        }
        if (origen.getDescripcion() != null) {
            destino.setDescripcion(origen.getDescripcion());
        }
        if (origen.getPrecio() != null) {
            destino.setPrecio(origen.getPrecio());
        }
        if (origen.getCategoria() != null) {
            destino.setCategoria(origen.getCategoria());
        }
        if (origen.getMarca() != null) {
            destino.setMarca(origen.getMarca());
        }
        if (origen.getStockDisponible() != null) {
            destino.setStockDisponible(origen.getStockDisponible());
        }
        
        // ✅ CORRECCIÓN: Actualizar fecha de modificación
        destino.setFechaModificacion(LocalDateTime.now()); // ✅ NOMBRE CONSISTENTE
    }
    
    /**
     * Valida si el producto se puede actualizar
     */
    private boolean esActualizacionValida(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            logger.warn("⚠️ Nombre del producto no puede estar vacío");
            return false;
        }
        
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            logger.warn("⚠️ Precio del producto debe ser mayor que 0");
            return false;
        }
        
        if (producto.getStockDisponible() != null && producto.getStockDisponible() < 0) {
            logger.warn("⚠️ Stock no puede ser negativo");
            return false;
        }
        
        return true;
    }
    
    /**
     * Aplica reglas de negocio específicas de actualización
     */
    private void aplicarReglasActualizacion(Producto producto) {
        // Recalcular descuentos si cambió el precio o categoría
        boolean precioOCategoriaCambiaron = estadoAnterior != null && (
            !estadoAnterior.getPrecio().equals(producto.getPrecio()) ||
            !estadoAnterior.getCategoria().equals(producto.getCategoria())
        );
        
        if (precioOCategoriaCambiaron) {
            DescuentoService.DescuentoInfo descuentoInfo = 
                descuentoService.aplicarDescuentosAutomaticos(producto);
            
            if (descuentoInfo.tieneDescuentos()) {
                logger.info("💰 Descuentos recalculados: {} | Nuevo ahorro: ${}", 
                           descuentoInfo.getDescuentosAplicados(), 
                           descuentoInfo.getTotalDescuento());
            }
        }
    }
    
    /**
     * Publica eventos si hubo cambios significativos
     */
    private void publicarEventosSiNecesario(Producto anterior, Producto actual) {
        // TODO: Implementar ProductoActualizadoEvent cuando tengamos más eventos
        logger.info("📡 Eventos de actualización pendientes de implementar");
    }
    
    /**
     * Clona un producto para mantener estado anterior
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
        clon.setActivo(original.isActivo()); // ✅ CORRECCIÓN: Boolean wrapper
        clon.setFechaCreacion(original.getFechaCreacion());
        clon.setFechaModificacion(original.getFechaModificacion()); // ✅ CORRECCIÓN: Nombre consistente
        return clon;
    }
}