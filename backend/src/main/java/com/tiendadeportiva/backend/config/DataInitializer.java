package com.tiendadeportiva.backend.config;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Configuración para inicializar datos de prueba en desarrollo.
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2:
 * - Actualizado para usar constructor por defecto + setters
 * - Compatible con modelo Producto mejorado (Boolean wrapper)
 * - Datos realistas para tienda deportiva
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(ProductoRepository repository) {
        return args -> {
            logger.info("🚀 Iniciando carga de datos de desarrollo...");

            // Verificar si ya hay datos
            if (repository.count() > 0) {
                logger.info("ℹ️ Base de datos ya contiene productos, omitiendo inicialización");
                return;
            }

            // ✅ CORRECCIÓN: Usar constructor por defecto + setters
            Producto producto1 = crearProducto(
                "Camiseta Nike Dri-FIT",
                "Camiseta deportiva de alta tecnología con tecnología Dri-FIT para mantener la piel seca",
                new BigDecimal("49.99"),
                "Camisetas",
                "Nike",
                25
            );

            Producto producto2 = crearProducto(
                "Short Adidas Climacool",
                "Short deportivo con tecnología Climacool para máxima ventilación",
                new BigDecimal("39.99"),
                "Shorts",
                "Adidas",
                30
            );

            Producto producto3 = crearProducto(
                "Zapatillas Puma Running",
                "Zapatillas de running con amortiguación avanzada y suela antideslizante",
                new BigDecimal("89.99"),
                "Calzado",
                "Puma",
                15
            );

            Producto producto4 = crearProducto(
                "Sudadera Under Armour",
                "Sudadera con capucha, perfecta para entrenamientos en clima frío",
                new BigDecimal("69.99"),
                "Sudaderas",
                "Under Armour",
                20
            );

            Producto producto5 = crearProducto(
                "Mallas Reebok Compression",
                "Mallas de compresión para mejor rendimiento y recuperación muscular",
                new BigDecimal("34.99"),
                "Mallas",
                "Reebok",
                18
            );

            Producto producto6 = crearProducto(
                "Chaqueta Columbia Windbreaker",
                "Chaqueta cortavientos ligera e impermeable para actividades al aire libre",
                new BigDecimal("79.99"),
                "Chaquetas",
                "Columbia",
                12
            );

            Producto producto7 = crearProducto(
                "Gorra New Era Deportiva",
                "Gorra ajustable con protección UV y diseño ergonómico",
                new BigDecimal("24.99"),
                "Accesorios",
                "New Era",
                40
            );

            Producto producto8 = crearProducto(
                "Calcetines Nike Elite",
                "Pack de 3 pares de calcetines deportivos con acolchado en zonas clave",
                new BigDecimal("19.99"),
                "Accesorios",
                "Nike",
                50
            );

            Producto producto9 = crearProducto(
                "Pantalón Adidas Tiro",
                "Pantalón de entrenamiento con ajuste cómodo y diseño moderno",
                new BigDecimal("54.99"),
                "Pantalones",
                "Adidas",
                22
            );

            Producto producto10 = crearProducto(
                "Polo Lacoste Sport",
                "Polo deportivo clásico con tejido transpirable y logo bordado",
                new BigDecimal("64.99"),
                "Polos",
                "Lacoste",
                16
            );

            Producto producto11 = crearProducto(
                "Botella Hydro Flask",
                "Botella de acero inoxidable con aislamiento térmico de 24 horas",
                new BigDecimal("29.99"),
                "Accesorios",
                "Hydro Flask",
                35
            );

            Producto producto12 = crearProducto(
                "Toalla Nike Premium",
                "Toalla de microfibra ultra absorbente y de secado rápido",
                new BigDecimal("14.99"),
                "Accesorios",
                "Nike",
                28
            );

            // Guardar todos los productos
            repository.save(producto1);
            repository.save(producto2);
            repository.save(producto3);
            repository.save(producto4);
            repository.save(producto5);
            repository.save(producto6);
            repository.save(producto7);
            repository.save(producto8);
            repository.save(producto9);
            repository.save(producto10);
            repository.save(producto11);
            repository.save(producto12);

            logger.info("✅ Datos de desarrollo cargados: {} productos creados", repository.count());
            logger.info("🏪 Tienda deportiva lista para operar!");
        };
    }

    /**
     * Método auxiliar para crear productos usando constructor por defecto + setters
     */
    private Producto crearProducto(String nombre, String descripcion, BigDecimal precio, 
                                 String categoria, String marca, Integer stock) {
        Producto producto = new Producto(); // ✅ Constructor por defecto
        
        // ✅ Usar setters para configurar el producto
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setStockDisponible(stock);
        producto.setActivo(Boolean.TRUE); // ✅ Boolean wrapper
        producto.setFechaCreacion(LocalDateTime.now());
        
        return producto;
    }
}
