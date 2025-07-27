package com.tiendadeportiva.backend.config;

import com.tiendadeportiva.backend.model.Producto;
import com.tiendadeportiva.backend.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Configuración para cargar datos de prueba en desarrollo.
 * Esta clase se ejecuta al iniciar la aplicación y carga productos de ejemplo.
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(ProductoRepository repository) {
        return args -> {
            logger.info("Inicializando base de datos con datos de prueba...");

            // Verificar si ya existen productos
            if (repository.count() == 0) {
                // Productos de Calzado Deportivo
                repository.save(new Producto(
                    "Nike Air Max 270",
                    "Zapatillas deportivas con tecnología Air Max para máxima comodidad",
                    new BigDecimal("129.99"),
                    "Calzado",
                    "Nike"
                ));

                repository.save(new Producto(
                    "Adidas Ultraboost 22",
                    "Zapatillas de running con tecnología Boost para mayor retorno de energía",
                    new BigDecimal("189.99"),
                    "Calzado",
                    "Adidas"
                ));

                repository.save(new Producto(
                    "Puma RS-X³",
                    "Zapatillas lifestyle con diseño retro-futurista",
                    new BigDecimal("89.99"),
                    "Calzado",
                    "Puma"
                ));

                // Productos de Ropa Deportiva
                repository.save(new Producto(
                    "Camiseta Dri-FIT Nike",
                    "Camiseta de entrenamiento con tecnología Dri-FIT que absorbe el sudor",
                    new BigDecimal("29.99"),
                    "Ropa",
                    "Nike"
                ));

                repository.save(new Producto(
                    "Shorts Adidas 3-Stripes",
                    "Shorts deportivos con las icónicas 3 rayas de Adidas",
                    new BigDecimal("39.99"),
                    "Ropa",
                    "Adidas"
                ));

                repository.save(new Producto(
                    "Sudadera Puma Essentials",
                    "Sudadera cómoda para entrenamientos y uso casual",
                    new BigDecimal("59.99"),
                    "Ropa",
                    "Puma"
                ));

                // Productos de Equipamiento
                repository.save(new Producto(
                    "Pelota de Fútbol Nike Premier League",
                    "Pelota oficial de la Premier League con tecnología aeroflex",
                    new BigDecimal("149.99"),
                    "Equipamiento",
                    "Nike"
                ));

                repository.save(new Producto(
                    "Raqueta de Tenis Wilson Pro Staff",
                    "Raqueta profesional utilizada por tenistas de élite",
                    new BigDecimal("299.99"),
                    "Equipamiento",
                    "Wilson"
                ));

                repository.save(new Producto(
                    "Mancuernas Ajustables 20kg",
                    "Set de mancuernas ajustables para entrenamiento en casa",
                    new BigDecimal("199.99"),
                    "Equipamiento",
                    "Generic"
                ));

                // Productos de Accesorios
                repository.save(new Producto(
                    "Botella de Agua Nike HyperCharge",
                    "Botella deportiva de 24oz con boquilla de flujo rápido",
                    new BigDecimal("24.99"),
                    "Accesorios",
                    "Nike"
                ));

                repository.save(new Producto(
                    "Gorra Adidas Baseball",
                    "Gorra ajustable con logo bordado de Adidas",
                    new BigDecimal("19.99"),
                    "Accesorios",
                    "Adidas"
                ));

                repository.save(new Producto(
                    "Guantes de Entrenamiento Under Armour",
                    "Guantes con grip superior para levantamiento de pesas",
                    new BigDecimal("34.99"),
                    "Accesorios",
                    "Under Armour"
                ));

                // Configurar stock inicial
                repository.findAll().forEach(producto -> {
                    producto.setStockDisponible((int) (Math.random() * 50) + 10); // Stock entre 10 y 59
                    repository.save(producto);
                });

                logger.info("Base de datos inicializada con {} productos", repository.count());
            } else {
                logger.info("Base de datos ya contiene datos, omitiendo inicialización");
            }
        };
    }
}
