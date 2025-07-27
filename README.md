# 🏃‍♂️ Tienda de Ropa Deportiva

Un proyecto educativo evolutivo que demuestra la transformación de una aplicación monolítica a una arquitectura de microservicios moderna.

## 🎯 Objetivo del Proyecto

Este proyecto está diseñado para ser **educativo y evolutivo**, mostrando cómo una aplicación puede crecer desde una implementación básica hasta una arquitectura empresarial robusta con:

- ✅ Patrones de diseño
- ✅ Principios SOLID
- ✅ Arquitectura hexagonal
- ✅ Microservicios
- ✅ Containerización con Docker
- ✅ Orquestación con Kubernetes
- ✅ Messaging con RabbitMQ/Kafka

## 🛠️ Stack Tecnológico

### Fase Actual (Monolito)
- **Backend**: Spring Boot 3.x (Java 17+)
- **Frontend**: React 18+ con JavaScript
- **Base de Datos**: PostgreSQL / H2
- **Build Tools**: Maven / Vite

### Fases Futuras
- **Containerización**: Docker & Docker Compose
- **Orquestación**: Kubernetes
- **Messaging**: RabbitMQ / Apache Kafka
- **Monitoreo**: Prometheus + Grafana
- **Service Mesh**: Istio

## 📋 Funcionalidades Planificadas

### MVP (Fase 1)
- [ ] Catálogo de productos deportivos
- [ ] Carrito de compras
- [ ] Gestión de usuarios
- [ ] Sistema de pedidos básico

### Características Avanzadas
- [ ] Sistema de pagos
- [ ] Gestión de inventario
- [ ] Sistema de recomendaciones
- [ ] Panel de administración
- [ ] Análitica y reportes

## 🏗️ Arquitectura Evolutiva

### 🔄 Fase 1: Monolito Modular
- Aplicación Spring Boot única
- React SPA
- Base de datos compartida
- Enfoque en patrones básicos

### 🏛️ Fase 2: Arquitectura Hexagonal
- Separación de responsabilidades
- Puertos y adaptadores
- Testing robusto
- Principios SOLID

### 🔧 Fase 3: Microservicios
- Servicios independientes por dominio
- Comunicación asíncrona
- Base de datos por servicio
- API Gateway

### ☁️ Fase 4: Cloud Native
- Deployment en Kubernetes
- Service mesh
- Observabilidad completa
- CI/CD automatizado

## 🚀 Comenzando

### Prerrequisitos
- Java 17+
- Node.js 18+
- Maven 3.8+
- PostgreSQL (opcional, usa H2 por defecto)

### Instalación

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/tienda-deportes.git
cd tienda-deportes

# Backend
cd backend
mvn spring-boot:run

# Frontend (en otra terminal)
cd frontend
npm install
npm run dev
```

## 📁 Estructura del Proyecto

```
tienda-deportes/
├── backend/           # Spring Boot application
├── frontend/          # React application
├── docker/           # Docker configurations (Fase 3+)
├── k8s/              # Kubernetes manifests (Fase 4+)
├── docs/             # Documentación técnica
└── .github/          # GitHub workflows y templates
```

## 🤝 Contribución

Este es un proyecto educativo. Las contribuciones son bienvenidas, especialmente aquellas que mejoren los aspectos educativos del código.

## 📚 Documentación Adicional

- [Decisiones Arquitectónicas](docs/architecture-decisions.md)
- [Guía de Desarrollo](docs/development-guide.md)
- [Patrones Implementados](docs/design-patterns.md)

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

---

⭐ **¡Dale una estrella al proyecto si te parece útil para aprender!**
