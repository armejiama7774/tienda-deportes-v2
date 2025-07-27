# 📚 Guía Educativa: Arquitectura del Backend - Fase 1

## 🎯 Propósito Educativo

Esta documentación está diseñada para **programadores junior** que quieren aprender:
- ✅ Arquitectura de aplicaciones empresariales
- ✅ Patrones de diseño fundamentales
- ✅ Principios SOLID en la práctica
- ✅ Evolución hacia microservicios
- ✅ Buenas prácticas de Spring Boot

---

## 🏗️ Arquitectura Actual: Monolito Modular

### ¿Por qué empezamos con un monolito?

**Ventajas del monolito para aprender:**
1. **Simplicidad inicial**: Más fácil de entender y debuggear
2. **Iteración rápida**: Cambios inmediatos sin complejidad de red
3. **Menos infraestructura**: Un solo proceso, una base de datos
4. **Base sólida**: Fundamentos antes de la complejidad distribuida

### Estructura del Proyecto

```
backend/src/main/java/com/tiendadeportiva/backend/
├── TiendaDeportesBackendApplication.java  # Punto de entrada
├── model/                                  # 📦 Capa de Dominio
│   └── Producto.java                      # Entidad de negocio
├── repository/                             # 📊 Capa de Datos
│   └── ProductoRepository.java            # Acceso a datos
├── service/                                # 🧠 Capa de Negocio
│   └── ProductoService.java               # Lógica de negocio
├── controller/                             # 🌐 Capa de Presentación
│   └── ProductoController.java            # API REST
└── config/                                 # ⚙️ Configuración
    ├── SecurityConfig.java                # Seguridad
    └── DataInitializer.java               # Datos de prueba
```

---

## 🎨 Patrones de Diseño Implementados

### 1. 📦 **Repository Pattern**

**¿Qué es?**
El patrón Repository abstrae el acceso a datos, proporcionando una interfaz uniforme para consultas sin importar el mecanismo de persistencia.

**Implementación:**
```java
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Spring Data JPA genera automáticamente la implementación
    List<Producto> findByCategoriaAndActivoTrue(String categoria);
}
```

**Beneficios:**
- ✅ **Testabilidad**: Fácil de mockear en tests
- ✅ **Flexibilidad**: Cambiar base de datos sin afectar el negocio
- ✅ **Reutilización**: Consultas consistentes en toda la app

### 2. 🧠 **Service Layer Pattern**

**¿Qué es?**
Encapsula la lógica de negocio en servicios reutilizables, separándola de la presentación y persistencia.

**Implementación:**
```java
@Service
@Transactional
public class ProductoService {
    // Lógica de negocio pura, sin detalles de HTTP o base de datos
    public Producto crearProducto(Producto producto) {
        validarProducto(producto); // Reglas de negocio
        return productoRepository.save(producto);
    }
}
```

**Beneficios:**
- ✅ **Reutilización**: Mismo servicio desde web, móvil, batch
- ✅ **Testeo**: Lógica aislada y testeable
- ✅ **Transacciones**: Manejo automático con `@Transactional`

### 3. 🌐 **MVC Pattern**

**¿Qué es?**
Separa la aplicación en Modelo (datos), Vista (presentación) y Controlador (lógica de presentación).

**Implementación:**
```java
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    // Solo maneja HTTP: requests, responses, validaciones de entrada
    // Delega toda la lógica al service
}
```

### 4. 📋 **DTO Pattern** (Preparado para fase 2)

En fases posteriores implementaremos DTOs para separar el modelo de dominio de las APIs.

---

## 🧩 Principios SOLID Aplicados

### 1. **S** - Single Responsibility Principle

**Cada clase tiene una sola razón para cambiar:**

- `Producto.java`: Solo representa el modelo de dominio
- `ProductoRepository.java`: Solo acceso a datos
- `ProductoService.java`: Solo lógica de negocio
- `ProductoController.java`: Solo manejo de HTTP

### 2. **O** - Open/Closed Principle

**Preparado para extensión:**
```java
// Interfaces preparadas para nuevas implementaciones
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Fácil agregar nuevos métodos sin modificar existentes
}
```

### 3. **L** - Liskov Substitution Principle

**Spring maneja esto automáticamente:**
- Los repositories son intercambiables
- Los services pueden tener múltiples implementaciones

### 4. **I** - Interface Segregation Principle

**Interfaces específicas:** (Implementaremos en Fase 2)
```java
// Fase 2: Interfaces específicas por funcionalidad
interface ProductoReader { ... }
interface ProductoWriter { ... }
interface ProductoValidator { ... }
```

### 5. **D** - Dependency Inversion Principle

**Inyección de dependencias con Spring:**
```java
public class ProductoService {
    private final ProductoRepository repository; // Depende de abstracción
    
    public ProductoService(ProductoRepository repository) { // Constructor injection
        this.repository = repository;
    }
}
```

---

## 🏛️ Arquitectura en Capas (Layered Architecture)

### Capa de Presentación (Controller)
- **Responsabilidad**: Manejo de HTTP, validación de entrada, serialización
- **Tecnologías**: Spring MVC, Jackson, Bean Validation
- **No debe**: Contener lógica de negocio

### Capa de Negocio (Service)
- **Responsabilidad**: Reglas de negocio, coordinación, transacciones
- **Tecnologías**: Spring Transactions, validaciones custom
- **No debe**: Conocer detalles de HTTP o base de datos

### Capa de Persistencia (Repository)
- **Responsabilidad**: Acceso a datos, queries, mapeo ORM
- **Tecnologías**: Spring Data JPA, Hibernate
- **No debe**: Contener lógica de negocio

### Capa de Dominio (Model)
- **Responsabilidad**: Representar conceptos del negocio
- **Tecnologías**: JPA annotations, Bean Validation
- **Debe**: Ser el corazón de la aplicación

---

## 🔧 Tecnologías y Decisiones Arquitectónicas

### Spring Boot 3.3.2
**¿Por qué?**
- ✅ **Auto-configuración**: Menos boilerplate
- ✅ **Starter dependencies**: Gestión simplificada
- ✅ **Actuator**: Métricas y health checks listos
- ✅ **Embedded server**: Deploy simplificado

### Spring Data JPA
**¿Por qué?**
- ✅ **Menos código**: Repository methods automáticos
- ✅ **Query derivation**: `findByNombreContaining` se genera solo
- ✅ **Custom queries**: JPQL cuando se necesita
- ✅ **Transacciones**: Manejo automático

### H2 Database (Desarrollo)
**¿Por qué para aprender?**
- ✅ **Sin setup**: Base de datos en memoria
- ✅ **Console web**: Visualizar datos fácilmente
- ✅ **Iteración rápida**: Reinicia limpio cada vez

### Bean Validation
**¿Por qué?**
- ✅ **Declarativo**: Validaciones como annotations
- ✅ **Reutilizable**: Mismas validaciones en todas las capas
- ✅ **Estándar**: JSR-303/380

---

## 🧪 Testing Strategy (Preparado para Fase 2)

### Pirámide de Testing
```
      🔺 E2E Tests (Pocas)
     🔹🔹 Integration Tests (Algunas)
   🔸🔸🔸🔸 Unit Tests (Muchas)
```

**Fase 2 implementará:**
- Unit tests para services
- Integration tests para repositories
- MockMvc tests para controllers

---

## 📊 Modelo de Datos

### Entidad Producto

```java
@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank @Size(min = 2, max = 100)
    private String nombre;
    
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal precio;
    
    // Audit fields
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
```

**Decisiones de diseño:**
- ✅ **Auto-incremento**: Simplifica la gestión de IDs
- ✅ **BigDecimal**: Precisión en precios (crítico en e-commerce)
- ✅ **Audit fields**: Trazabilidad desde el inicio
- ✅ **Soft delete**: `activo` flag para preservar data

---

## 🌟 Preparación para Evolución

### Hacia Arquitectura Hexagonal (Fase 2)
```
🔷 Ports (Interfaces)     🔷 Adapters (Implementations)
   ProductoService   <->     ProductoServiceImpl
   ProductoRepo      <->     JpaProductoRepository
   ProductoAPI       <->     RestProductoController
```

### Hacia Microservicios (Fase 3)
```
🏪 Producto Service    🛒 Pedido Service    👤 Usuario Service
   ├── API REST          ├── API REST         ├── API REST
   ├── Base datos        ├── Base datos       ├── Base datos
   └── Lógica negocio    └── Lógica negocio   └── Lógica negocio
```

---

## 🚀 Siguientes Pasos Educativos

### Inmediato (Fase 1.5)
1. **Testing completo**: Unit + Integration tests
2. **DTOs**: Separar API del dominio
3. **Exception handling**: Manejo global de errores
4. **Logging**: Structured logging con MDC

### Fase 2: Arquitectura Hexagonal
1. **Ports & Adapters**: Interfaces claras
2. **Domain services**: Lógica de dominio pura
3. **Event-driven**: Domain events
4. **CQRS básico**: Separar lecturas de escrituras

### Fase 3: Microservicios
1. **Service discovery**: Eureka
2. **API Gateway**: Spring Cloud Gateway
3. **Circuit breakers**: Resilience4j
4. **Distributed tracing**: Sleuth + Zipkin

---

## 💡 Conceptos Clave para Recordar

### 🎯 **Separation of Concerns**
Cada capa tiene responsabilidades específicas y bien definidas.

### 🔄 **Dependency Injection**
Spring maneja las dependencias, nosotros definimos las relaciones.

### 📦 **Encapsulation**
Los detalles internos están ocultos, solo exponemos interfaces.

### 🧪 **Testability**
Cada componente puede probarse en aislamiento.

### 📈 **Scalability Preparation**
Diseño que facilita el crecimiento futuro.

---

*Esta es la base sólida sobre la cual construiremos una arquitectura moderna y escalable. Cada decisión tomada aquí facilita la evolución futura hacia patrones más sofisticados.*
