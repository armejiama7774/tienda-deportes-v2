# Instrucciones del Proyecto: Tienda de Ropa Deportiva

<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

## Contexto del Proyecto
Este es un proyecto educativo evolutivo de una tienda online de ropa deportiva que evoluciona desde una arquitectura monolítica hasta microservicios modernos.

## Stack Tecnológico Actual
- **Backend**: Spring Boot 3.x (Java 17+)
- **Frontend**: React 18+ con JavaScript
- **Base de Datos**: PostgreSQL (desarrollo) / H2 (testing)
- **Build Tools**: Maven (backend) / Vite (frontend)

## Stack Tecnológico Futuro (Fases Avanzadas)
- **Containerización**: Docker & Docker Compose
- **Orquestación**: Kubernetes
- **Messaging**: RabbitMQ / Apache Kafka
- **Monitoreo**: Prometheus + Grafana
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Eureka
- **Config Server**: Spring Cloud Config

## Arquitectura Evolutiva

### Fase 1: Monolito Modular (ACTUAL)
- Aplicación monolítica Spring Boot
- React SPA como frontend
- Base de datos única
- Enfoque en patrones de diseño básicos

### Fase 2: Arquitectura Hexagonal
- Implementación de puertos y adaptadores
- Separación clara entre dominio y infraestructura
- Principios SOLID aplicados
- Testing robusto

### Fase 3: Microservicios
- Separación por dominios (Productos, Usuarios, Pedidos, Pagos)
- Comunicación asíncrona con messaging
- Base de datos por servicio

### Fase 4: Cloud Native
- Kubernetes deployment
- Service mesh (Istio)
- Observabilidad completa
- CI/CD automatizado

## Dominios del Negocio
1. **Gestión de Productos** (catálogo, inventario, categorías)
2. **Gestión de Usuarios** (autenticación, perfiles, preferencias)
3. **Gestión de Pedidos** (carrito, checkout, órdenes)
4. **Gestión de Pagos** (procesamiento, facturación)
5. **Gestión de Envíos** (logística, tracking)

## Patrones de Diseño a Implementar
- Repository Pattern
- Factory Pattern
- Observer Pattern
- Strategy Pattern
- Command Pattern
- CQRS (fases avanzadas)
- Event Sourcing (fases avanzadas)

## Convenciones de Código
- **Java**: Google Java Style Guide
- **JavaScript**: ESLint + Prettier
- **Commits**: Conventional Commits
- **Branching**: GitFlow

## Estructura de Directorios
```
tienda-deportes/
├── backend/           # Spring Boot application
├── frontend/          # React application
├── docker/           # Docker configurations (Fase 3+)
├── k8s/              # Kubernetes manifests (Fase 4+)
├── docs/             # Documentación técnica
└── .github/          # GitHub workflows y templates
```

## Notas Importantes
- Mantener backward compatibility entre fases
- Documentar todas las decisiones arquitectónicas
- Implementar testing desde el inicio
- Considerar escalabilidad en cada decisión
git remote set-url origin https://TU_TOKEN_AQUI@github.com/armejiama7774/tienda-deportes.git