package com.tiendadeportiva.backend.command.producto;

import com.tiendadeportiva.backend.command.Command;
import com.tiendadeportiva.backend.command.CommandExecutionException;
import com.tiendadeportiva.backend.exception.ProductoNoEncontradoException;
import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import com.tiendadeportiva.backend.service.DescuentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

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
    private final ProductoRepository repository;
    private final DescuentoService descuentoService;
    private final ApplicationEventPublisher eventPublisher;
    private final String usuarioModificador;
    
    // Para rollback
    private Producto estadoAnterior;
    private Producto estadoActualizado;
    
    public ActualizarProductoCommand(
            Long productoId,
            Producto datosActualizacion,
            ProductoRepository repository,
            DescuentoService descuentoService,
            ApplicationEventPublisher eventPublisher,
            String usuarioModificador) {
        this.productoId = productoId;
        this.datosActualizacion = datosActualizacion;
        this.repository = repository;
        this.descuentoService = descuentoService;
        this.eventPublisher = eventPublisher;
        this.usuarioModificador = usuarioModificador;
    }
    
    @Override
    public Producto execute() throws CommandExecutionException {
        try {
            logger.info("🔄 Ejecutando ActualizarProductoCommand para ID: {}", productoId);
            
            // 1. Obtener producto actual
            Optional<Producto> productoOpt = repository.findById(productoId);
            if (productoOpt.isEmpty() || !productoOpt.get().getActivo()) {
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
            estadoActualizado = repository.save(producto);
            
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
                repository.save(estadoAnterior);
                
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
        
        // Actualizar metadatos
        destino.setFechaActualizacion(LocalDateTime.now());
    }
    
    /**
     * Valida que la actualización sea válida
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
        clon.setActivo(original.getActivo());
        clon.setFechaCreacion(original.getFechaCreacion());
        clon.setFechaActualizacion(original.getFechaActualizacion());
        return clon;
    }
}