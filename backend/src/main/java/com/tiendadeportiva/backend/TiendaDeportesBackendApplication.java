package com.tiendadeportiva.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.annotation.Validated;

/**
 * Aplicación principal de la Tienda Deportiva
 * 
 * EVOLUCIÓN ARQUITECTÓNICA - Fase 2: Arquitectura Hexagonal
 * - Configuración base para validación automática
 * - Preparado para evolución a microservicios
 */
@SpringBootApplication
@Validated // ✅ Habilitar validación a nivel de aplicación
public class TiendaDeportesBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiendaDeportesBackendApplication.class, args);
    }
}
