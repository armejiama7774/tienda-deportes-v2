package com.tiendadeportiva.backend.service;

import com.tiendadeportiva.backend.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio especializado en cálculo de descuentos automáticos.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA:
 * - Responsabilidad única: Solo maneja lógica de descuentos
 * - Separado del ProductoService para cumplir SRP
 * - Testeable independientemente
 * - Preparado para configuración externa y reglas dinámicas
 */
@Service
public class DescuentoService {
    
    private static final Logger logger = LoggerFactory.getLogger(DescuentoService.class);
    
    /**
     * Aplica descuentos automáticos a un producto
     * 
     * @param producto Producto al que aplicar descuentos
     * @return Información sobre descuentos aplicados
     */
    public DescuentoInfo aplicarDescuentosAutomaticos(Producto producto) {
        BigDecimal precioOriginal = producto.getPrecio();
        BigDecimal precioConDescuento = precioOriginal;
        StringBuilder descuentosAplicados = new StringBuilder();
        
        try {
            // Descuento por categoría
            BigDecimal descuentoCategoria = calcularDescuentoPorCategoria(producto);
            if (descuentoCategoria.compareTo(BigDecimal.ZERO) > 0) {
                precioConDescuento = precioConDescuento.subtract(descuentoCategoria);
                descuentosAplicados.append(String.format("Categoría %s: %.0f%%; ", 
                    producto.getCategoria(), 
                    descuentoCategoria.divide(precioOriginal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"))));
            }
            
            // Descuento por temporada
            BigDecimal descuentoTemporada = calcularDescuentoPorTemporada(producto);
            if (descuentoTemporada.compareTo(BigDecimal.ZERO) > 0) {
                precioConDescuento = precioConDescuento.subtract(descuentoTemporada);
                descuentosAplicados.append("Temporada Verano: 15%; ");
            }
            
            // Descuento por stock alto
            BigDecimal descuentoStock = calcularDescuentoPorStock(producto);
            if (descuentoStock.compareTo(BigDecimal.ZERO) > 0) {
                precioConDescuento = precioConDescuento.subtract(descuentoStock);
                descuentosAplicados.append("Stock Alto: 8%; ");
            }
            
            // Descuento por marca premium
            BigDecimal descuentoMarca = calcularDescuentoPorMarca(producto);
            if (descuentoMarca.compareTo(BigDecimal.ZERO) > 0) {
                precioConDescuento = precioConDescuento.subtract(descuentoMarca);
                descuentosAplicados.append("Marca Premium: 5%; ");
            }
            
            // Aplicar precio final
            producto.setPrecio(precioConDescuento);
            
            return new DescuentoInfo(
                precioOriginal, 
                precioConDescuento, 
                descuentosAplicados.toString(),
                precioOriginal.subtract(precioConDescuento)
            );
            
        } catch (Exception e) {
            logger.error("Error calculando descuentos para producto {}: {}", 
                        producto.getNombre(), e.getMessage(), e);
            // Revertir al precio original por seguridad
            producto.setPrecio(precioOriginal);
            return DescuentoInfo.sinDescuentos(precioOriginal);
        }
    }
    
    private BigDecimal calcularDescuentoPorCategoria(Producto producto) {
        return switch (producto.getCategoria()) {
            case "Zapatos" -> producto.getPrecio().multiply(new BigDecimal("0.10"));
            case "Camisetas" -> producto.getPrecio().multiply(new BigDecimal("0.05"));
            default -> BigDecimal.ZERO;
        };
    }
    
    private BigDecimal calcularDescuentoPorTemporada(Producto producto) {
        if (esTemporadaVerano()) {
            return producto.getPrecio().multiply(new BigDecimal("0.15"));
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calcularDescuentoPorStock(Producto producto) {
        if (producto.getStockDisponible() > 50) {
            return producto.getPrecio().multiply(new BigDecimal("0.08"));
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calcularDescuentoPorMarca(Producto producto) {
        if (esMarcaPremium(producto.getMarca())) {
            return producto.getPrecio().multiply(new BigDecimal("0.05"));
        }
        return BigDecimal.ZERO;
    }
    
    private boolean esTemporadaVerano() {
        int mes = LocalDateTime.now().getMonthValue();
        return mes >= 6 && mes <= 8;
    }
    
    private boolean esMarcaPremium(String marca) {
        List<String> marcasPremium = List.of("Nike", "Adidas", "Puma", "Under Armour");
        return marcasPremium.contains(marca);
    }
    
    /**
     * Clase para encapsular información sobre descuentos aplicados
     */
    public static class DescuentoInfo {
        private final BigDecimal precioOriginal;
        private final BigDecimal precioFinal;
        private final String descuentosAplicados;
        private final BigDecimal totalDescuento;
        
        public DescuentoInfo(BigDecimal precioOriginal, BigDecimal precioFinal, 
                           String descuentosAplicados, BigDecimal totalDescuento) {
            this.precioOriginal = precioOriginal;
            this.precioFinal = precioFinal;
            this.descuentosAplicados = descuentosAplicados;
            this.totalDescuento = totalDescuento;
        }
        
        public static DescuentoInfo sinDescuentos(BigDecimal precio) {
            return new DescuentoInfo(precio, precio, "", BigDecimal.ZERO);
        }
        
        // Getters...
        public BigDecimal getPrecioOriginal() { return precioOriginal; }
        public BigDecimal getPrecioFinal() { return precioFinal; }
        public String getDescuentosAplicados() { return descuentosAplicados; }
        public BigDecimal getTotalDescuento() { return totalDescuento; }
        
        public boolean tieneDescuentos() {
            return totalDescuento.compareTo(BigDecimal.ZERO) > 0;
        }
    }
}