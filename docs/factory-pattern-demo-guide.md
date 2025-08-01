## 🏭 **DEMOSTRACIÓN INTERACTIVA DEL FACTORY PATTERN**

### **🎯 LO QUE VERÁS EN LA EJECUCIÓN:**

#### **1. Factories Disponibles** 📋
```
🏭 CalzadoFactory: CALZADO - RUNNING, CASUAL, FUTBOL, BASKETBALL, TRAINING, HIKING
```

#### **2. Creación de Zapatillas Running** 👟
```java
ProductoCreationRequest request = ProductoCreationRequest.builder()
    .conNombre("Air Max Running Pro")
    .conCategoria("CALZADO")
    .conTipo("RUNNING")
    .conTalla("42")
    .conPrecio(new BigDecimal("150.00"))
    .build();

Producto producto = factoryManager.crearProducto(request);
```

**Resultado Esperado:**
- ✅ Validación automática de talla (35-48)
- ✅ Configuración automática: material MESH, suela EVA
- ✅ Stock inicial: 15 unidades (específico para RUNNING)
- ✅ Propiedades automáticas: amortiguación ALTA, transpirabilidad ALTA

#### **3. Validaciones Especializadas** 🔍

**Caso 1: Talla Válida**
```
Talla: "42" → ✅ Válida (dentro del rango 35-48)
```

**Caso 2: Talla Inválida**
```
Talla: "99" → ❌ Error: "Talla no válida: 99. Tallas válidas: [35, 36, 37, ...]"
```

**Caso 3: Tipo No Soportado**
```
Tipo: "VOLEIBOL" → ❌ Error: "Tipo de calzado no válido: VOLEIBOL"
```

#### **4. Configuraciones Automáticas por Tipo** ⚙️

| **Tipo** | **Stock Defecto** | **Configuraciones Automáticas** |
|----------|-------------------|----------------------------------|
| RUNNING  | 15 unidades       | Material: MESH, Suela: EVA, Amortiguación: ALTA |
| FUTBOL   | 20 unidades       | Tacos: FG/TF según superficie, Material: CUERO_SINTETICO |
| BASKETBALL | 12 unidades     | Corte: MID_TOP, Soporte tobillo: ALTO, Tracción: MULTIDIRECCIONAL |
| HIKING   | 8 unidades        | Impermeabilidad: ALTA, Suela: VIBRAM, Resistencia: ALTA |

#### **5. Integración con Observer Pattern** 🔔

Cuando se crea un producto con Factory:
```
🏭 Factory Pattern: Producto creado → CalzadoFactory
🔔 Observer Pattern: Evento emitido → PRODUCTO_CREADO
📝 LoggingObserver: "Producto creado con Factory Pattern - Tipo: RUNNING"
📊 StockObserver: "Stock inicial configurado: 15 unidades"
💰 PrecioObserver: "Precio registrado: $150.00"
```

### **🎬 COMANDOS PARA EJECUTAR LOS DEMOS:**

```bash
# Demo completo del Factory Pattern
mvnw test -Dtest=FactoryPatternDemo

# Demo unitario específico  
mvnw test -Dtest=FactoryPatternDemoTest

# Demo simple con Spring Boot
mvnw spring-boot:run -Ddemo.factory.enabled=true

# Demo del Observer Pattern (para ver integración)
mvnw spring-boot:run -Ddemo.observer.enabled=true
```

### **🔍 QUÉ BUSCAR EN LA SALIDA:**

1. **Registro de Factories:**
   ```
   🏭 ProductoFactoryManager inicializado con 1 factories
   📋 Factory registrada: CalzadoFactory - Categoría: CALZADO
   ```

2. **Selección Automática:**
   ```
   🔍 Buscando factory para categoría: 'CALZADO', tipo: 'RUNNING'
   ✅ Factory encontrada: CalzadoFactory puede crear CALZADO/RUNNING
   ```

3. **Validaciones en Acción:**
   ```
   🔍 Validando datos específicos de calzado para: Air Max Running Pro
   ✅ Tipo válido: RUNNING
   ✅ Talla válida: 42
   ⚠️ Material no estándar: CUSTOM_MESH
   ```

4. **Configuraciones Aplicadas:**
   ```
   🔧 Aplicando configuraciones especializadas de calzado
   ⚙️ Configuración RUNNING: material_superior=MESH, amortiguacion=ALTA
   📦 Stock inicial configurado automáticamente: 15 unidades
   ```

5. **Producto Final:**
   ```
   ✅ Producto creado exitosamente: Air Max Running Pro (ID: 1)
   🎉 Configuraciones automáticas aplicadas por CalzadoFactory
   ```

### **🎓 PUNTOS CLAVE A OBSERVAR:**

1. **Selección Automática**: El `ProductoFactoryManager` elige automáticamente `CalzadoFactory`
2. **Validaciones Específicas**: Cada factory tiene validaciones únicas (tallas, materiales, tipos)
3. **Configuraciones Inteligentes**: Propiedades automáticas según el tipo de producto
4. **Manejo de Errores**: Errores descriptivos y específicos por contexto
5. **Integración Seamless**: Factory Pattern funciona perfectamente con Observer Pattern
6. **Extensibilidad**: Fácil agregar nuevas factories sin modificar código existente

### **🚀 DESPUÉS DEL DEMO:**

El Factory Pattern está listo para:
- ✅ Crear productos especializados automáticamente
- ✅ Validar datos específicos por tipo
- ✅ Aplicar configuraciones inteligentes
- ✅ Integrarse con otros patrones (Observer, Command)
- ✅ Escalar con nuevos tipos de productos
- ✅ Facilitar testing y mantenimiento

¡El Factory Pattern demuestra cómo los patrones de diseño profesionales mejoran significativamente la calidad y mantenibilidad del código!
