# 🏃‍♂️ Backend - Tienda Deportiva

## 🎯 Resumen Ejecutivo

**Backend Spring Boot 3.3.2** para una tienda de ropa deportiva, diseñado como proyecto educativo que evoluciona desde monolito hasta microservicios. Implementa patrones de diseño modernos y principios SOLID.

---

## ⚡ Quick Start

```bash
# Compilar y ejecutar
.\mvnw.cmd spring-boot:run

# Verificar que funciona
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/productos
```

**🌐 URLs Importantes:**
- API REST: `http://localhost:8080/api/productos`
- H2 Console: `http://localhost:8080/h2-console`
- Health Check: `http://localhost:8080/actuator/health`

---

## 🏗️ Arquitectura Implementada

### Patrón: Monolito Modular en Capas
```
📱 Controller Layer    → REST APIs, validación HTTP
🧠 Service Layer       → Lógica de negocio, transacciones
📊 Repository Layer    → Acceso a datos, queries
📦 Model Layer         → Entidades de dominio
```

### Principios SOLID Aplicados
- ✅ **Single Responsibility**: Cada clase una responsabilidad
- ✅ **Open/Closed**: Extensible via interfaces
- ✅ **Liskov Substitution**: Spring maneja intercambiabilidad
- ✅ **Interface Segregation**: Preparado para Fase 2
- ✅ **Dependency Inversion**: Inyección de dependencias

---

## 📋 Funcionalidades Implementadas

### ✅ CRUD Completo de Productos
- Crear, leer, actualizar, eliminar productos
- Validaciones automáticas con Bean Validation
- Soft delete (marca como inactivo)

### ✅ Búsquedas Especializadas
- Por categoría, marca, nombre
- Rango de precios
- Productos con stock disponible
- Metadata (categorías y marcas disponibles)

### ✅ Gestión de Inventario
- Control de stock por producto
- Operaciones de actualización de inventario

### ✅ Datos de Prueba
- 12 productos deportivos pre-cargados
- 4 categorías: Calzado, Ropa, Equipamiento, Accesorios
- 5 marcas: Nike, Adidas, Puma, Wilson, Under Armour

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología | Versión | Propósito |
|------------|------------|---------|-----------|
| Framework | Spring Boot | 3.3.2 | Base de la aplicación |
| Language | Java | 17+ | Lenguaje principal |
| Database | H2 / PostgreSQL | Latest | Persistencia |
| ORM | Spring Data JPA | Incluido | Mapeo objeto-relacional |
| Security | Spring Security | Incluido | Autenticación básica |
| Validation | Bean Validation | Incluido | Validaciones declarativas |
| Build Tool | Maven | 3.9.4 | Gestión de dependencias |
| DevTools | Spring DevTools | Incluido | Hot reload |

---

## 📂 Estructura del Código

```
src/main/java/com/tiendadeportiva/backend/
├── TiendaDeportesBackendApplication.java    # 🚀 Main class
├── model/
│   └── Producto.java                        # 📦 Domain entity
├── repository/
│   └── ProductoRepository.java              # 📊 Data access
├── service/
│   └── ProductoService.java                 # 🧠 Business logic
├── controller/
│   └── ProductoController.java              # 🌐 REST API
└── config/
    ├── SecurityConfig.java                  # 🛡️ Security setup
    └── DataInitializer.java                 # 📋 Test data
```

---

## 🔌 Endpoints de la API

### Productos Base
```http
GET    /api/productos           # Obtener todos
GET    /api/productos/{id}      # Obtener por ID
POST   /api/productos           # Crear nuevo
PUT    /api/productos/{id}      # Actualizar
DELETE /api/productos/{id}      # Eliminar (soft)
```

### Búsquedas
```http
GET /api/productos/categoria/{categoria}
GET /api/productos/marca/{marca}
GET /api/productos/buscar?nombre={texto}
GET /api/productos/precio?min={min}&max={max}
GET /api/productos/con-stock
```

### Metadata
```http
GET /api/productos/categorias   # Lista de categorías
GET /api/productos/marcas       # Lista de marcas
```

### Inventario
```http
PATCH /api/productos/{id}/stock?stock={cantidad}
```

---

## 📊 Modelo de Datos

### Entidad Producto
```java
@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue
    private Long id;
    
    @NotBlank @Size(min = 2, max = 100)
    private String nombre;
    
    @Size(max = 500)
    private String descripcion;
    
    @NotNull @DecimalMin("0.0")
    private BigDecimal precio;
    
    @NotBlank
    private String categoria;
    
    @NotBlank
    private String marca;
    
    private Integer stockDisponible;
    private String imagenUrl;
    private Boolean activo = true;
    
    // Audit fields
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
```

---

## 🧪 Testing

### Ejecutar Tests
```bash
.\mvnw.cmd test                    # Todos los tests
.\mvnw.cmd test -Dtest=*Test      # Tests específicos
```

### Coverage Actual
- ✅ Context loading test
- 📋 **Próximo**: Unit tests para services
- 📋 **Próximo**: Integration tests para repositories
- 📋 **Próximo**: MockMvc tests para controllers

---

## 🔧 Configuración

### Base de Datos H2 (Desarrollo)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
```

### CORS (Para React)
```java
@CrossOrigin(origins = "http://localhost:3000")
```

### Logging
```properties
logging.level.com.tiendadeportiva=DEBUG
```

---

## 🚀 Comandos Útiles

### Desarrollo
```bash
# Ejecutar en modo desarrollo
.\mvnw.cmd spring-boot:run

# Compilar sin ejecutar
.\mvnw.cmd compile

# Limpiar y compilar
.\mvnw.cmd clean compile

# Ver dependencias
.\mvnw.cmd dependency:tree
```

### Verificación
```bash
# Health check
curl http://localhost:8080/actuator/health

# Obtener productos
curl http://localhost:8080/api/productos

# Crear producto de prueba
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test","precio":99.99,"categoria":"Test","marca":"Test"}'
```

---

## 🐛 Troubleshooting

### Puerto 8080 ocupado
```bash
# Cambiar puerto
echo "server.port=8081" >> src/main/resources/application.properties
```

### Java no encontrado
```bash
# Verificar Java 17+
java -version
echo %JAVA_HOME%
```

### Maven wrapper no ejecuta
```bash
# Desde directorio backend
dir mvnw.cmd  # Debe existir
```

---

## 📈 Evolución Futura

### 🎯 Fase 2: Arquitectura Hexagonal
- Ports & Adapters pattern
- Domain services puros
- DTOs y mappers
- Testing exhaustivo

### 🎯 Fase 3: Microservicios
- Separación por bounded contexts
- Message brokers (RabbitMQ/Kafka)
- Service discovery
- Circuit breakers

### 🎯 Fase 4: Cloud Native
- Kubernetes deployment
- Service mesh (Istio)
- Observabilidad completa
- CI/CD automatizado

---

## 📚 Documentación Adicional

- [🏗️ Guía Arquitectónica](../docs/backend-architecture-guide.md)
- [🔌 API Reference](../docs/api-reference.md)
- [✅ Verificación Backend](../docs/backend-verification.md)
- [🗺️ Roadmap Educativo](../docs/roadmap-educativo.md)

---

**🎓 Este backend es la base sólida para aprender arquitecturas modernas. Cada línea de código está diseñada para enseñar conceptos que se usan en empresas reales.**
