# 🏭 Factory Pattern - Implementación Profesional

## 📋 **RESUMEN DE IMPLEMENTACIÓN**

Hemos implementado un **Factory Pattern sofisticado** que demuestra las mejores prácticas profesionales para la creación especializada de productos en nuestra tienda deportiva.

## 🎯 **COMPONENTES IMPLEMENTADOS**

### **1. Estructura Base del Factory Pattern**

```
factory/
├── ProductoFactory.java                    # Interfaz base del factory
├── ProductoCreationRequest.java            # DTO con Builder Pattern
├── ProductoCreationException.java          # Manejo de errores especializado
├── ProductoFactoryManager.java             # Coordinador central
└── impl/
    ├── BaseProductoFactory.java            # Template Method + Factory
    └── CalzadoFactory.java                 # Factory especializada
```

### **2. Interfaces y Contratos**

#### **ProductoFactory (Interfaz Base)**
```java
public interface ProductoFactory {
    Producto crearProducto(ProductoCreationRequest request);
    boolean puedeCrear(String categoria, String tipo);
    String[] getTiposSoportados();
    String getCategoriaPrincipal();
    void validarDatos(ProductoCreationRequest request);
    void aplicarConfiguracionesDefecto(Producto producto, ProductoCreationRequest request);
}
```

**🎯 Beneficios:**
- Contrato claro para todas las factories
- Facilita testing con mocks
- Permite extensión sin modificar código existente
- Separación clara de responsabilidades

#### **ProductoCreationRequest (Builder Pattern)**
```java
ProductoCreationRequest request = ProductoCreationRequest.builder()
    .conNombre("Air Max Running Pro")
    .conCategoria("CALZADO")
    .conTipo("RUNNING")
    .conPrecio(new BigDecimal("150.00"))
    .conTalla("42")
    .conMaterial("MESH")
    .aplicarDescuentoLanzamiento(true)
    .conPropiedadExtendida("amortiguacion", "ALTA")
    .build();
```

**🎯 Beneficios:**
- Evita constructores con muchos parámetros
- Fluent API fácil de usar
- Propiedades extendidas para flexibilidad
- Validación en tiempo de construcción

### **3. Implementación Especializada**

#### **CalzadoFactory - Factory Concreta**

**Tipos Soportados:**
- `RUNNING` - Zapatillas para correr
- `FUTBOL` - Tacos y botas de fútbol  
- `BASKETBALL` - Zapatillas de baloncesto
- `TRAINING` - Calzado para entrenamiento
- `HIKING` - Botas y zapatillas de senderismo
- `CASUAL` - Calzado informal deportivo

**Validaciones Específicas:**
```java
// Validación de tallas específicas de calzado
private static final List<String> TALLAS_VALIDAS = Arrays.asList(
    "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48"
);

// Validación de materiales apropiados
private static final List<String> MATERIALES_VALIDOS = Arrays.asList(
    "CUERO", "SINTETICO", "MESH", "CANVAS", "TEXTIL", "GORE_TEX", "NEOPRENO"
);
```

**Configuraciones Automáticas por Tipo:**

```java
// RUNNING - Configuración automática
case "RUNNING":
    propiedades.put("material_superior", "MESH");
    propiedades.put("tipo_suela", "EVA");
    propiedades.put("amortiguacion", "ALTA");
    propiedades.put("transpirabilidad", "ALTA");
    stockDefecto = 15;
    break;

// FUTBOL - Configuración automática
case "FUTBOL":
    switch (subtipo) {
        case "CESPED": propiedades.put("tipo_tacos", "FG"); break;
        case "SINTETICO": propiedades.put("tipo_tacos", "TF"); break;
        case "SALON": propiedades.put("tipo_suela", "LISA"); break;
    }
    stockDefecto = 20;
    break;
```

### **4. Template Method Pattern Integrado**

#### **BaseProductoFactory - Algoritmo Base**

```java
public final Producto crearProducto(ProductoCreationRequest request) {
    // PASO 1: Validaciones generales
    validarDatosBasicos(request);
    
    // PASO 2: Validaciones específicas del tipo (implementado por subclases)
    validarDatos(request);
    
    // PASO 3: Crear producto base
    Producto producto = crearProductoBase(request);
    
    // PASO 4: Aplicar configuraciones específicas del tipo
    aplicarConfiguracionesDefecto(producto, request);
    
    // PASO 5: Aplicar configuraciones especializadas (implementado por subclases)
    aplicarConfiguracionesEspecializadas(producto, request);
    
    // PASO 6: Validación final
    validarProductoCreado(producto);
    
    return producto;
}
```

**🎯 Beneficios del Template Method:**
- Garantiza algoritmo consistente
- Permite customización en puntos específicos
- Centraliza validaciones comunes
- Facilita mantenimiento

### **5. Factory Manager - Coordinador Central**

#### **ProductoFactoryManager**

```java
@Service
public class ProductoFactoryManager {
    private final List<ProductoFactory> factories;
    
    public Producto crearProducto(ProductoCreationRequest request) {
        ProductoFactory factory = seleccionarFactory(request.getCategoria(), request.getTipo());
        return factory.crearProducto(request);
    }
    
    private ProductoFactory seleccionarFactory(String categoria, String tipo) {
        return factories.stream()
            .filter(f -> f.puedeCrear(categoria, tipo))
            .findFirst()
            .orElse(null);
    }
}
```

**🎯 Funcionalidades:**
- Auto-descubrimiento de factories vía Spring
- Selección automática de factory apropiada
- Validación previa de peticiones
- Información de factories disponibles
- Manejo centralizado de errores

## 🔗 **INTEGRACIÓN CON OTROS PATRONES**

### **1. Observer Pattern Integration**

```java
public Producto crearProductoConFactory(ProductoCreationRequest request) {
    // Factory Pattern: Crear producto especializado
    Producto productoCreado = factoryManager.crearProducto(request);
    
    // Persistir usando método tradicional
    Producto productoGuardado = crearProducto(productoCreado);
    
    // Observer Pattern: Notificar creación con factory info
    observerPublisher.notifyEvent(
        ProductoEventType.PRODUCTO_CREADO,
        productoGuardado,
        "Producto creado con Factory Pattern - Tipo: " + request.getTipo()
    );
    
    return productoGuardado;
}
```

### **2. Command Pattern Integration**

El Factory Pattern se integra perfectamente con el Command Pattern existente:

```java
// El producto creado por Factory se puede usar en Commands
Producto producto = factoryManager.crearProducto(request);
CrearProductoCommand command = new CrearProductoCommand(producto, ...);
commandHandler.handle(command);
```

### **3. Strategy Pattern Integration**

Las factories pueden usar Strategy Pattern internamente para diferentes configuraciones:

```java
// Diferentes estrategias de configuración según el tipo
switch (tipo) {
    case "RUNNING": aplicarEstrategiaRunning(producto); break;
    case "FUTBOL": aplicarEstrategiaFutbol(producto); break;
    // ...
}
```

## 📊 **CASOS DE USO DEMOSTRADOS**

### **1. Creación de Zapatillas Running**
```java
ProductoCreationRequest request = ProductoCreationRequest.builder()
    .conNombre("Air Max Running Pro")
    .conCategoria("CALZADO")
    .conTipo("RUNNING")
    .conTalla("42")
    .conMaterial("MESH")
    .aplicarDescuentoLanzamiento(true)
    .conPropiedadExtendida("drop_tacon", "10mm")
    .build();

Producto producto = productoService.crearProductoConFactory(request);
```

**Resultado Automático:**
- ✅ Validación de talla específica
- ✅ Configuración de material superior: MESH
- ✅ Configuración de suela: EVA
- ✅ Amortiguación: ALTA
- ✅ Stock inicial: 15 unidades
- ✅ Descuento de lanzamiento aplicado
- ✅ Eventos Observer disparados

### **2. Validaciones Especializadas**
```java
// Automáticamente detecta y rechaza tallas inválidas
request.conTalla("99"); // ❌ Error: Talla no válida

// Automáticamente detecta tipos no soportados  
request.conTipo("VOLEIBOL"); // ❌ Error: Tipo no soportado

// Validación de precios según tipo
request.conTipo("RUNNING").conPrecio(BigDecimal.ONE); // ⚠️ Warning: Precio fuera de rango
```

## 🎯 **VENTAJAS PROFESIONALES LOGRADAS**

### **1. Extensibilidad**
- ✅ Nuevos tipos de productos sin modificar código existente
- ✅ Nuevas factories se auto-registran vía Spring
- ✅ Configuraciones específicas por tipo fácilmente extensibles

### **2. Mantenibilidad**
- ✅ Separación clara de responsabilidades
- ✅ Validaciones centralizadas por tipo
- ✅ Logging detallado para debugging
- ✅ Manejo de errores especializado

### **3. Testabilidad**
- ✅ Factories pueden mockearse independientemente
- ✅ Validaciones unitarias por tipo
- ✅ Factory Manager testeable con factories mock
- ✅ Integration tests con Spring Boot

### **4. Escalabilidad**
- ✅ Auto-descubrimiento de factories
- ✅ Configuración dinámica vía Spring
- ✅ Performance optimizada (selección O(n))
- ✅ Memory efficient (lazy loading de configuraciones)

## 🚀 **PRÓXIMOS PASOS SUGERIDOS**

### **1. Nuevas Factories**
- `RopaFactory` - Para camisetas, pantalones, etc.
- `AccesoriosFactory` - Para mochilas, pelotas, etc.
- `EquipamientoFactory` - Para equipos deportivos especializados

### **2. Configuración Avanzada**
- Factory configuration via properties
- Dynamic factory loading
- A/B testing con diferentes factories
- Métricas de performance por factory

### **3. Integración Externa**
- Validación con catálogos externos
- Pricing automático vía APIs
- Configuración desde bases de datos
- Cache de configuraciones por tipo

## 🎓 **APRENDIZAJES CLAVE**

1. **Factory Pattern** no es solo crear objetos, es crear objetos **correctamente configurados**
2. **Template Method** garantiza consistencia mientras permite especialización
3. **Builder Pattern** hace las APIs más usables y extensibles
4. **Spring Integration** facilita auto-discovery y configuration
5. **Error Handling** especializado mejora experiencia de desarrollo
6. **Integration con otros patterns** multiplica los beneficios

El Factory Pattern implementado demuestra cómo los patrones de diseño profesionales pueden mejorar significativamente la calidad, mantenibilidad y extensibilidad del código en aplicaciones reales.
