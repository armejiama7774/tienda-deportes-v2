package com.tiendadeportiva.backend.service.descuento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para DescuentoContexto.
 * 
 * OBJETIVO DIDÁCTICO:
 * - Mostrar cómo testear Builder Pattern
 * - Validar immutabilidad del objeto
 * - Probar casos edge y valores por defecto
 * 
 * @author Equipo Desarrollo
 * @version 2.1
 */
@DisplayName("DescuentoContexto - Builder Pattern Tests")
class DescuentoContextoTest {
    
    @Test
    @DisplayName("Builder debe crear contexto con valores por defecto")
    void builderDebeCrearContextoConValoresPorDefecto() {
        // Act
        DescuentoContexto contexto = DescuentoContexto.builder().build();
        
        // Assert
        assertThat(contexto.getFechaCalculo()).isNotNull();
        assertThat(contexto.getTipoUsuario()).isNull();
        assertThat(contexto.getCantidadEnCarrito()).isNull();
        assertThat(contexto.getCodigoPromocional()).isNull();
        assertThat(contexto.getTemporada()).isNull();
    }
    
    @Test
    @DisplayName("Builder debe permitir construcción fluida")
    void builderDebePermitirConstruccionFluida() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.of(2025, 1, 15, 10, 30);
        
        // Act
        DescuentoContexto contexto = DescuentoContexto.builder()
                .conFechaCalculo(fecha)
                .conTipoUsuario("VIP")
                .conCantidadEnCarrito(5)
                .conCodigoPromocional("DESCUENTO20")
                .conTemporada("INVIERNO")
                .conPropiedad("esClienteNuevo", true)
                .conPropiedad("montoTotal", 150.00)
                .build();
        
        // Assert
        assertThat(contexto.getFechaCalculo()).isEqualTo(fecha);
        assertThat(contexto.getTipoUsuario()).isEqualTo("VIP");
        assertThat(contexto.getCantidadEnCarrito()).isEqualTo(5);
        assertThat(contexto.getCodigoPromocional()).isEqualTo("DESCUENTO20");
        assertThat(contexto.getTemporada()).isEqualTo("INVIERNO");
        assertThat(contexto.getPropiedad("esClienteNuevo")).isEqualTo(true);
        assertThat(contexto.getPropiedad("montoTotal")).isEqualTo(150.00);
    }
    
    @Test
    @DisplayName("Debe manejar propiedades adicionales correctamente")
    void debeManejarPropiedadesAdicionalesCorrectamente() {
        // Arrange & Act
        DescuentoContexto contexto = DescuentoContexto.builder()
                .conPropiedad("clave1", "valor1")
                .conPropiedad("clave2", 42)
                .build();
        
        // Assert
        assertThat(contexto.tienePropiedad("clave1")).isTrue();
        assertThat(contexto.tienePropiedad("clave2")).isTrue();
        assertThat(contexto.tienePropiedad("claveInexistente")).isFalse();
        
        assertThat(contexto.getPropiedad("clave1")).isEqualTo("valor1");
        assertThat(contexto.getPropiedad("clave2")).isEqualTo(42);
        assertThat(contexto.getPropiedad("claveInexistente")).isNull();
    }
}