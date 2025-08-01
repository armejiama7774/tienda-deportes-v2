package com.tiendadeportiva.backend.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test para verificar que el contexto de Spring Boot se carga correctamente.
 */
@SpringBootTest
@ActiveProfiles("test")
public class SpringContextTest {

    @Test
    public void contextLoads() {
        System.out.println("✅ Contexto de Spring cargado exitosamente");
    }
}
