# 🔌 API Reference - Tienda Deportiva Backend

## 📋 Endpoints Disponibles

### Base URL
```
http://localhost:8080/api/productos
```

---

## 📦 CRUD Básico de Productos

### 1. 📋 Obtener todos los productos
```http
GET /api/productos
```

**Response:**
```json
[
  {
    "id": 1,
    "nombre": "Nike Air Max 270",
    "descripcion": "Zapatillas deportivas con tecnología Air Max",
    "precio": 129.99,
    "categoria": "Calzado",
    "marca": "Nike",
    "stockDisponible": 25,
    "imagenUrl": null,
    "activo": true,
    "fechaCreacion": "2025-01-26T10:30:00",
    "fechaActualizacion": "2025-01-26T10:30:00"
  }
]
```

### 2. 🔍 Obtener producto por ID
```http
GET /api/productos/{id}
```

**Ejemplo:**
```bash
curl http://localhost:8080/api/productos/1
```

### 3. ➕ Crear nuevo producto
```http
POST /api/productos
Content-Type: application/json
```

**Body:**
```json
{
  "nombre": "Adidas Ultraboost 23",
  "descripcion": "Zapatillas de running premium",
  "precio": 199.99,
  "categoria": "Calzado",
  "marca": "Adidas",
  "stockDisponible": 15
}
```

**Validaciones automáticas:**
- `nombre`: Obligatorio, 2-100 caracteres
- `precio`: Obligatorio, mayor que 0
- `categoria`: Obligatorio
- `marca`: Obligatorio

### 4. ✏️ Actualizar producto
```http
PUT /api/productos/{id}
Content-Type: application/json
```

### 5. 🗑️ Eliminar producto (Soft Delete)
```http
DELETE /api/productos/{id}
```

---

## 🔍 Búsquedas Especializadas

### Por Categoría
```http
GET /api/productos/categoria/{categoria}
```

**Ejemplos:**
```bash
curl http://localhost:8080/api/productos/categoria/Calzado
curl http://localhost:8080/api/productos/categoria/Ropa
curl http://localhost:8080/api/productos/categoria/Equipamiento
```

### Por Marca
```http
GET /api/productos/marca/{marca}
```

**Ejemplos:**
```bash
curl http://localhost:8080/api/productos/marca/Nike
curl http://localhost:8080/api/productos/marca/Adidas
```

### Por Nombre (Búsqueda parcial)
```http
GET /api/productos/buscar?nombre={texto}
```

**Ejemplos:**
```bash
curl "http://localhost:8080/api/productos/buscar?nombre=Nike"
curl "http://localhost:8080/api/productos/buscar?nombre=Air"
```

### Por Rango de Precios
```http
GET /api/productos/precio?min={precio_min}&max={precio_max}
```

**Ejemplos:**
```bash
curl "http://localhost:8080/api/productos/precio?min=50&max=150"
curl "http://localhost:8080/api/productos/precio?min=0&max=100"
```

### Productos con Stock
```http
GET /api/productos/con-stock
```

---

## 📊 Endpoints de Metadata

### Obtener Categorías Disponibles
```http
GET /api/productos/categorias
```

**Response:**
```json
["Accesorios", "Calzado", "Equipamiento", "Ropa"]
```

### Obtener Marcas Disponibles
```http
GET /api/productos/marcas
```

**Response:**
```json
["Adidas", "Generic", "Nike", "Puma", "Under Armour", "Wilson"]
```

---

## 🔧 Operaciones de Inventario

### Actualizar Stock
```http
PATCH /api/productos/{id}/stock?stock={nuevo_stock}
```

**Ejemplo:**
```bash
curl -X PATCH "http://localhost:8080/api/productos/1/stock?stock=50"
```

---

## ⚡ Scripts de Prueba Rápida

### PowerShell (Windows)
```powershell
# Obtener todos los productos
Invoke-RestMethod -Uri "http://localhost:8080/api/productos" -Method GET

# Crear un producto
$nuevoProducto = @{
    nombre = "Test Producto"
    descripcion = "Producto de prueba"
    precio = 99.99
    categoria = "Test"
    marca = "Test"
    stockDisponible = 10
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/productos" -Method POST -Body $nuevoProducto -ContentType "application/json"
```

### cURL (Linux/Mac/WSL)
```bash
# Obtener todos los productos
curl http://localhost:8080/api/productos

# Crear un producto
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Producto",
    "descripcion": "Producto de prueba",
    "precio": 99.99,
    "categoria": "Test",
    "marca": "Test",
    "stockDisponible": 10
  }'
```

---

## 🛡️ Manejo de Errores

### Códigos de Estado HTTP

| Código | Descripción | Cuándo ocurre |
|--------|-------------|---------------|
| 200 | OK | Operación exitosa |
| 201 | Created | Producto creado |
| 204 | No Content | Producto eliminado |
| 400 | Bad Request | Validación fallida |
| 404 | Not Found | Producto no encontrado |
| 500 | Internal Error | Error del servidor |

### Ejemplos de Errores

**Validación fallida (400):**
```json
{
  "timestamp": "2025-01-26T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/productos"
}
```

**Producto no encontrado (404):**
```json
{
  "timestamp": "2025-01-26T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado",
  "path": "/api/productos/999"
}
```

---

## 🔍 Herramientas de Desarrollo

### H2 Console (Base de Datos)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: password
```

### Actuator (Health Check)
```bash
curl http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP"
}
```

---

## 🧪 Tests de Integración Recomendados

### Flujo Completo de E-commerce
```bash
# 1. Verificar que el servicio está activo
curl http://localhost:8080/actuator/health

# 2. Obtener catálogo inicial
curl http://localhost:8080/api/productos

# 3. Buscar productos por categoría
curl http://localhost:8080/api/productos/categoria/Calzado

# 4. Verificar stock disponible
curl http://localhost:8080/api/productos/con-stock

# 5. Buscar producto específico
curl "http://localhost:8080/api/productos/buscar?nombre=Nike"

# 6. Obtener detalles de un producto
curl http://localhost:8080/api/productos/1

# 7. Simular compra (reducir stock)
curl -X PATCH "http://localhost:8080/api/productos/1/stock?stock=20"
```

---

## 📈 Métricas y Monitoreo

### Logs Importantes
El backend genera logs detallados:

```
2025-01-26 10:30:00.123 INFO  --- ProductoController : Petición recibida: GET /api/productos
2025-01-26 10:30:00.125 DEBUG --- ProductoService    : Obteniendo todos los productos activos
2025-01-26 10:30:00.130 INFO  --- ProductoService    : Productos encontrados: 12
```

### Monitoreo de Performance
- **Tiempo de respuesta**: < 100ms para operaciones simples
- **Throughput**: Capaz de manejar múltiples requests concurrentes
- **Memoria**: Uso eficiente con JPA Level 1 cache

---

*Esta API está diseñada siguiendo principios REST y está preparada para escalar hacia una arquitectura de microservicios en futuras fases del proyecto.*
