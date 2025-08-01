# 🗺️ HOJA DE RUTA EVOLUTIVA - PASO A PASO

## 🎯 **FILOSOFÍA DE EVOLUCIÓN**

> **"No hay que reescribir, hay que evolucionar"**

Este proyecto demuestra cómo evolucionar una aplicación desde monolito hasta microservicios **SIN ROMPER NADA**, modificando 1-2 archivos a la vez, probando inmediatamente, y manteniendo toda la funcionalidad existente.

---

## 📋 **PRINCIPIOS DE EVOLUCIÓN**

### **1. Incremental Development**
- ✅ **Un cambio a la vez** - Máximo 2-3 archivos por commit
- ✅ **Probar inmediatamente** - Tests + demos después de cada cambio
- ✅ **Rollback fácil** - Cada commit es un punto de restauración válido

### **2. Backward Compatibility**
- ✅ **APIs existentes funcionan** - No romper contratos establecidos
- ✅ **Tests siguen pasando** - Verde en todo momento
- ✅ **Demos siguen ejecutando** - Factory y Observer patterns funcionando

### **3. Documentation Driven**
- ✅ **Documentar antes de implementar** - ADRs (Architecture Decision Records)
- ✅ **Actualizar con cada cambio** - Docs reflejan estado real
- ✅ **Explicar el "por qué"** - Decisiones arquitectónicas justificadas

---

## 🛣️ **ROADMAP DETALLADO**

### **🎯 MILESTONE 1: BOUNDED CONTEXTS** 
**Objetivo:** Preparar separación lógica de dominios  
**Tiempo estimado:** 3-5 commits, 1-2 días  
**Impacto:** Reorganización de código, sin cambios funcionales  

#### **Commit 1: Crear estructura de dominios**
```bash
# Crear nuevos packages
mkdir -p backend/src/main/java/com/tiendadeportiva/producto
mkdir -p backend/src/main/java/com/tiendadeportiva/usuario  
mkdir -p backend/src/main/java/com/tiendadeportiva/pedido
mkdir -p backend/src/main/java/com/tiendadeportiva/shared
```

#### **Commit 2: Mover ProductoService a dominio**
```bash
# Mover archivo existente
git mv backend/src/main/java/com/tiendadeportiva/backend/service/ProductoService.java \
       backend/src/main/java/com/tiendadeportiva/producto/service/ProductoService.java

# Actualizar imports en archivos dependientes
# Probar: mvnw test
```

#### **Commit 3: Crear UsuarioService básico**
```java
// Nuevo archivo: usuario/service/UsuarioService.java
@Service
public class UsuarioService {
    public Usuario crearUsuario(Usuario usuario) {
        // Implementación básica
        return usuario;
    }
}
```

#### **Commit 4: Crear PedidoService básico**
```java
// Nuevo archivo: pedido/service/PedidoService.java
@Service  
public class PedidoService {
    public Pedido crearPedido(Pedido pedido) {
        // Implementación básica
        return pedido;
    }
}
```

#### **Commit 5: Documentar bounded contexts**
```markdown
# Nuevo archivo: docs/bounded-contexts.md
## Dominios Identificados
- Producto: Gestión de catálogo, stock, precios
- Usuario: Autenticación, perfiles, preferencias  
- Pedido: Carrito, checkout, órdenes
```

#### **✅ Checkpoint 1: Verificar**
```bash
mvnw test                           # ✅ Todos los tests pasan
mvnw test -Dtest=FactoryPatternDemo # ✅ Factory demo funciona
mvnw test -Dtest=ObserverPatternLiveDemo # ✅ Observer demo funciona
```

---

### **🎯 MILESTONE 2: MULTI-MÓDULO MAVEN**
**Objetivo:** Preparar estructura física para múltiples servicios  
**Tiempo estimado:** 4-6 commits, 2-3 días  
**Impacto:** Reestructuración de build, sin cambios funcionales  

#### **Commit 1: Crear pom.xml principal**
```xml
<!-- Modificar pom.xml raíz -->
<packaging>pom</packaging>
<modules>
    <module>producto-service</module>
    <module>usuario-service</module>
    <module>pedido-service</module>
    <module>shared-lib</module>
</modules>
```

#### **Commit 2: Crear producto-service**
```bash
# Mover backend/ completo a producto-service/
git mv backend/ producto-service/

# Crear producto-service/pom.xml específico
# Probar: mvnw clean install
```

#### **Commit 3: Crear shared-lib**
```bash
# Crear shared-lib/src/main/java/com/tiendadeportiva/shared/
# Mover DTOs compartidos, eventos, exceptions comunes
# Crear shared-lib/pom.xml
```

#### **Commit 4: Crear usuario-service**
```bash
# Crear usuario-service/ con estructura Spring Boot básica
# Copiar configuración base de producto-service
# Agregar dependencia a shared-lib
```

#### **Commit 5: Crear pedido-service**
```bash
# Crear pedido-service/ con estructura Spring Boot básica  
# Copiar configuración base de producto-service
# Agregar dependencia a shared-lib
```

#### **Commit 6: Actualizar scripts y docs**
```bash
# Actualizar .github/copilot-instructions.md
# Actualizar docs con nueva estructura
# Crear scripts de build para todos los módulos
```

#### **✅ Checkpoint 2: Verificar**
```bash
mvnw clean install                 # ✅ Build completo exitoso
cd producto-service && mvnw test   # ✅ Tests producto-service
cd usuario-service && mvnw test    # ✅ Tests usuario-service  
cd pedido-service && mvnw test     # ✅ Tests pedido-service
```

---

### **🎯 MILESTONE 3: SERVICE DISCOVERY**
**Objetivo:** Configurar Eureka para comunicación entre servicios  
**Tiempo estimado:** 5-7 commits, 2-3 días  
**Impacto:** Nuevas capacidades, servicios pueden comunicarse  

#### **Commit 1: Crear eureka-server**
```java
// Nuevo módulo: eureka-server/
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

#### **Commit 2: Configurar eureka-server**
```yaml
# eureka-server/src/main/resources/application.yml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

#### **Commit 3: Configurar producto-service como cliente**
```xml
<!-- Agregar a producto-service/pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```java
// Modificar ProductoServiceApplication.java
@SpringBootApplication
@EnableEurekaClient  // ← Nueva línea
public class ProductoServiceApplication {
    // ...
}
```

#### **Commit 4: Configurar usuario-service como cliente**
```yaml
# usuario-service/src/main/resources/application.yml
spring:
  application:
    name: usuario-service
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

#### **Commit 5: Configurar pedido-service como cliente**
```yaml
# pedido-service/src/main/resources/application.yml  
spring:
  application:
    name: pedido-service
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

#### **Commit 6: Agregar comunicación básica**
```java
// En pedido-service: Comunicarse con producto-service
@Service
public class PedidoService {
    
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public Producto obtenerProducto(Long id) {
        return restTemplate.getForObject(
            "http://producto-service/api/productos/" + id, 
            Producto.class
        );
    }
}
```

#### **Commit 7: Crear test de comunicación**
```java
// Nuevo test: pedido-service/src/test/.../ServiceCommunicationTest.java
@SpringBootTest
public class ServiceCommunicationTest {
    
    @Test
    public void debePoderComunicarseConProductoService() {
        // Test de comunicación entre servicios
    }
}
```

#### **✅ Checkpoint 3: Verificar**
```bash
# Terminal 1: Iniciar Eureka Server
cd eureka-server && mvnw spring-boot:run

# Terminal 2: Iniciar Producto Service  
cd producto-service && mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# Terminal 3: Iniciar Usuario Service
cd usuario-service && mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"

# Terminal 4: Iniciar Pedido Service
cd pedido-service && mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"

# Verificar en http://localhost:8761 que todos los servicios aparecen registrados ✅
# Probar comunicación entre servicios ✅
```

---

### **🎯 MILESTONE 4: API GATEWAY**
**Objetivo:** Punto único de entrada con enrutamiento inteligente  
**Tiempo estimado:** 4-6 commits, 2-3 días  
**Impacto:** Nueva funcionalidad, consolidación de APIs  

#### **Commit 1: Crear api-gateway**
```java
// Nuevo módulo: api-gateway/
@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

#### **Commit 2: Configurar rutas básicas**
```yaml
# api-gateway/src/main/resources/application.yml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: producto-service
          uri: lb://producto-service
          predicates:
            - Path=/api/productos/**
        - id: usuario-service
          uri: lb://usuario-service  
          predicates:
            - Path=/api/usuarios/**
        - id: pedido-service
          uri: lb://pedido-service
          predicates:
            - Path=/api/pedidos/**
```

#### **Commit 3: Agregar filtros de logging**
```java
// api-gateway: Nuevo filtro de logging
@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("🚪 Gateway: {} {}", request.getMethod(), request.getPath());
        return chain.filter(exchange);
    }
}
```

#### **Commit 4: Configurar CORS**
```java
// api-gateway: Configuración CORS
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000"); // React frontend
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        
        return new CorsWebFilter(new UrlBasedCorsConfigurationSource());
    }
}
```

#### **Commit 5: Crear tests del gateway**
```java
// api-gateway/src/test/.../ApiGatewayTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiGatewayTest {
    
    @Test
    public void debeEnrutarCorrectamenteAProductoService() {
        // Test de enrutamiento
    }
}
```

#### **Commit 6: Actualizar documentación**
```markdown
# docs/api-gateway-config.md
## Configuración API Gateway

### Rutas configuradas:
- /api/productos/** → producto-service
- /api/usuarios/** → usuario-service  
- /api/pedidos/** → pedido-service

### Filtros aplicados:
- Logging de requests
- CORS para frontend
- Rate limiting (futuro)
```

#### **✅ Checkpoint 4: Verificar**
```bash
# Iniciar todos los servicios + gateway
cd api-gateway && mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"

# Probar enrutamiento
curl http://localhost:8080/api/productos  # ✅ → producto-service
curl http://localhost:8080/api/usuarios   # ✅ → usuario-service
curl http://localhost:8080/api/pedidos    # ✅ → pedido-service

# Verificar logs del gateway ✅
```

---

### **🎯 MILESTONE 5: CIRCUIT BREAKER**
**Objetivo:** Resilencia y tolerancia a fallos  
**Tiempo estimado:** 3-5 commits, 1-2 días  
**Impacto:** Mejor resilencia, fallbacks automáticos  

#### **Commit 1: Agregar Resilience4j**
```xml
<!-- Agregar a shared-lib/pom.xml -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
</dependency>
```

#### **Commit 2: Configurar circuit breaker**
```yaml
# pedido-service/application.yml - Ejemplo de configuración
resilience4j:
  circuitbreaker:
    instances:
      producto-service:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
```

#### **Commit 3: Implementar circuit breaker**
```java
// En PedidoService
@Service
public class PedidoService {
    
    @CircuitBreaker(name = "producto-service", fallbackMethod = "fallbackObtenerProducto")
    public Producto obtenerProducto(Long id) {
        return restTemplate.getForObject(
            "http://producto-service/api/productos/" + id, 
            Producto.class
        );
    }
    
    public Producto fallbackObtenerProducto(Long id, Exception ex) {
        log.warn("🔄 Fallback activado para producto {}: {}", id, ex.getMessage());
        return new Producto("Producto no disponible", "Servicio temporalmente no disponible");
    }
}
```

#### **Commit 4: Agregar métricas**
```java
// Endpoint para métricas de circuit breaker
@RestController
public class MetricsController {
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @GetMapping("/metrics/circuit-breakers")
    public Map<String, Object> getCircuitBreakerMetrics() {
        return circuitBreakerRegistry.getAllCircuitBreakers()
            .stream()
            .collect(Collectors.toMap(
                CircuitBreaker::getName,
                cb -> Map.of(
                    "state", cb.getState(),
                    "metrics", cb.getMetrics()
                )
            ));
    }
}
```

#### **Commit 5: Tests de resilencia**
```java
// Test de circuit breaker
@SpringBootTest
public class CircuitBreakerTest {
    
    @Test
    public void debeActivarFallbackCuandoServicioFalla() {
        // Simular fallo del servicio
        // Verificar que fallback se activa
        // Verificar métricas del circuit breaker
    }
}
```

#### **✅ Checkpoint 5: Verificar**
```bash
# Probar resilencia
curl http://localhost:8080/api/pedidos/1  # ✅ Funciona normalmente

# Parar producto-service intencionalmente
# Probar otra vez
curl http://localhost:8080/api/pedidos/1  # ✅ Fallback activado

# Verificar métricas
curl http://localhost:8083/metrics/circuit-breakers  # ✅ Estado del circuit breaker
```

---

## 🎯 **SIGUIENTES FASES (FUTURAS)**

### **FASE 4: EVENT-DRIVEN ARCHITECTURE**
- Implementar Apache Kafka/RabbitMQ
- Event sourcing básico  
- CQRS completo
- Sagas para transacciones distribuidas

### **FASE 5: CONTAINERIZATION**
- Dockerfiles para cada servicio
- Docker Compose para desarrollo
- Optimización de imágenes
- Multi-stage builds

### **FASE 6: KUBERNETES**
- Deployments y Services
- ConfigMaps y Secrets
- Ingress Controllers
- Horizontal Pod Autoscaling

### **FASE 7: OBSERVABILITY**
- Distributed tracing (Jaeger)
- Metrics (Prometheus)
- Dashboards (Grafana)
- Centralized logging (ELK)

---

## 📋 **CHECKLIST POR COMMIT**

### **Antes de cada commit:**
- [ ] 🧪 **Tests pasan** - `mvnw test` verde
- [ ] 🎭 **Demos funcionan** - Factory y Observer patterns ejecutan
- [ ] 📖 **Docs actualizadas** - Cambios reflejados en documentación
- [ ] 🔍 **Code review propio** - Revisar cambios antes de commit
- [ ] 💬 **Mensaje claro** - Commit message descriptivo

### **Después de cada milestone:**
- [ ] 🚀 **Demo completo** - Todos los servicios inician correctamente
- [ ] 📊 **Performance check** - Sin degradación significativa
- [ ] 🧪 **Integration tests** - Comunicación entre servicios funciona
- [ ] 📚 **Documentation review** - Docs reflejan nueva arquitectura
- [ ] 🎯 **Retrospectiva** - Lecciones aprendidas documentadas

---

## ⚠️ **SEÑALES DE ALERTA**

### **🚨 PARAR SI:**
- Tests empiezan a fallar y no sabes por qué
- Demos de patterns dejan de funcionar
- Performance se degrada significativamente
- Complejidad crece sin valor claro
- Te sientes perdido en los cambios

### **🔄 EN ESE CASO:**
1. **Git revert** al último commit estable
2. **Analizar** qué salió mal
3. **Planificar** enfoque diferente  
4. **Documentar** lecciones aprendidas
5. **Intentar** de nuevo con pasos más pequeños

---

## 🎓 **PRINCIPIOS EDUCATIVOS**

### **1. Aprendizaje Incremental**
- Cada commit enseña un concepto específico
- Cambios pequeños permiten entender impacto
- Errores son oportunidades de aprendizaje
- Documentación refuerza conceptos

### **2. Feedback Inmediato**
- Tests dan feedback instantáneo
- Demos muestran funcionalidad real
- Logs permiten debugging efectivo
- Métricas validan decisiones

### **3. Construcción de Confianza**
- Cada paso exitoso construye confianza
- Rollback fácil reduce miedo al cambio
- Documentación da seguridad
- Patterns conocidos proporcionan base sólida

---

## 🚀 **CONCLUSIÓN**

Esta hoja de ruta permite evolucionar el proyecto **de forma segura y educativa**, manteniendo siempre la funcionalidad existente mientras se agregan nuevas capacidades.

**Cada paso está diseñado para:**
- ✅ Enseñar un concepto específico
- ✅ Mantener todo funcionando
- ✅ Ser reversible si algo sale mal
- ✅ Preparar el siguiente paso

**Siguiente acción recomendada:** Comenzar con Milestone 1 - Bounded Contexts

---

**Última actualización:** 1 de Agosto de 2025  
**Revisión programada:** Después de cada milestone completado
