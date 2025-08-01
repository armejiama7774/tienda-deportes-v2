package com.tiendadeportiva.backend.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test para ejecutar y mostrar la demostración del Factory Pattern.
 */
@SpringBootTest
@ActiveProfiles("test")
public class FactoryPatternShowcaseTest {

    @Autowired
    private FactoryPatternShowcase showcase;

    @Test
    public void ejecutarDemostracionCompleta() {
        System.out.println("🎬 INICIANDO DEMOSTRACIÓN DEL FACTORY PATTERN...\n");
        
        String demoCompleto = showcase.demonstrarFactoryPattern();
        System.out.println(demoCompleto);
        
        System.out.println("\n📊 COMPARACIÓN ADICIONAL:");
        String comparacion = showcase.compararConfiguracionesPorTipo();
        System.out.println(comparacion);
        
        System.out.println("🎉 DEMOSTRACIÓN COMPLETADA EXITOSAMENTE!");
    }
}
