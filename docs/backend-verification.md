# ✅ Guía de Verificación del Backend

## 🎯 Objetivo
Verificar paso a paso que el backend Spring Boot está funcionando correctamente antes de continuar con el frontend.

---

## 📋 Checklist de Verificación

### ✅ 1. Compilación y Build
```bash
cd backend
.\mvnw.cmd clean compile
```

**✅ Resultado esperado:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

### ✅ 2. Ejecutar Tests
```bash
.\mvnw.cmd test
```

**✅ Resultado esperado:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### ✅ 3. Iniciar la Aplicación
```bash
.\mvnw.cmd spring-boot:run
```

**✅ Resultado esperado:**
```
Started TiendaDeportesBackendApplication in X.XXX seconds
Listening on http://localhost:8080
```

### ✅ 4. Verificar Health Check
**Abrir en navegador:** `http://localhost:8080/actuator/health`

**✅ Resultado esperado:**
```json
{
  "status": "UP"
}
```

### ✅ 5. Verificar Consola H2
**Abrir en navegador:** `http://localhost:8080/h2-console`

**Configuración:**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

**✅ Resultado esperado:** Conexión exitosa y tabla `PRODUCTOS` visible

### ✅ 6. Probar API REST

#### 6.1 Obtener todos los productos
```bash
curl http://localhost:8080/api/productos
```

**✅ Resultado esperado:** Array JSON con 12 productos

#### 6.2 Obtener producto específico
```bash
curl http://localhost:8080/api/productos/1
```

**✅ Resultado esperado:** Objeto JSON con detalles del producto

#### 6.3 Buscar por categoría
```bash
curl http://localhost:8080/api/productos/categoria/Calzado
```

**✅ Resultado esperado:** Array con productos de calzado

#### 6.4 Obtener categorías
```bash
curl http://localhost:8080/api/productos/categorias
```

**✅ Resultado esperado:** `["Accesorios", "Calzado", "Equipamiento", "Ropa"]`

---

## 🔧 Resolución de Problemas Comunes

### ❌ Puerto 8080 en uso
**Error:** `Port 8080 was already in use`

**Solución:**
```bash
# Verificar qué usa el puerto
netstat -ano | findstr :8080

# Cambiar puerto en application.properties
server.port=8081
```

### ❌ Java no encontrado
**Error:** `Error: JAVA_HOME not found`

**Solución:**
```bash
# Verificar Java
java -version

# Si no está instalado, descargar Java 17+
# https://adoptium.net/
```

### ❌ Maven Wrapper no ejecuta
**Error:** `mvnw.cmd is not recognized`

**Solución:**
```bash
# Asegurar que estás en el directorio backend
cd backend
ls mvnw.cmd  # Debe existir

# Dar permisos si es necesario
chmod +x mvnw.cmd
```

### ❌ Base de datos no inicializa
**Error:** No aparecen productos en la API

**Verificación:**
1. Revisar logs de `DataInitializer`
2. Verificar en H2 Console que existe la tabla `PRODUCTOS`
3. Confirmar que `spring.jpa.hibernate.ddl-auto=create-drop` está configurado

---

## 📊 Verificación de Datos de Prueba

### Productos Esperados por Categoría

| Categoría | Cantidad | Marcas |
|-----------|----------|--------|
| Calzado | 3 | Nike, Adidas, Puma |
| Ropa | 3 | Nike, Adidas, Puma |
| Equipamiento | 3 | Nike, Wilson, Generic |
| Accesorios | 3 | Nike, Adidas, Under Armour |

### Rangos de Precios
- **Mínimo:** $19.99 (Gorra Adidas)
- **Máximo:** $299.99 (Raqueta Wilson)
- **Promedio:** ~$120

### Stock Inicial
- Todos los productos tienen stock entre 10-59 unidades
- Generado aleatoriamente en `DataInitializer`

---

## 🧪 Tests Funcionales Completos

### Script de Verificación Automática (PowerShell)
```powershell
# Verificar que el servicio responde
$health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
Write-Host "✅ Health: $($health.status)"

# Verificar productos
$productos = Invoke-RestMethod -Uri "http://localhost:8080/api/productos"
Write-Host "✅ Productos encontrados: $($productos.Count)"

# Verificar categorías
$categorias = Invoke-RestMethod -Uri "http://localhost:8080/api/productos/categorias"
Write-Host "✅ Categorías: $($categorias -join ', ')"

# Verificar búsqueda
$nike = Invoke-RestMethod -Uri "http://localhost:8080/api/productos/buscar?nombre=Nike"
Write-Host "✅ Productos Nike: $($nike.Count)"

Write-Host "🎉 Backend verificado correctamente!"
```

### Script de Carga de Datos (Para pruebas)
```powershell
# Crear producto de prueba
$nuevoProducto = @{
    nombre = "Producto Test Backend"
    descripcion = "Creado durante verificación"
    precio = 29.99
    categoria = "Test"
    marca = "Test"
    stockDisponible = 5
} | ConvertTo-Json

$resultado = Invoke-RestMethod -Uri "http://localhost:8080/api/productos" -Method POST -Body $nuevoProducto -ContentType "application/json"
Write-Host "✅ Producto creado con ID: $($resultado.id)"

# Verificar que se creó
$verificacion = Invoke-RestMethod -Uri "http://localhost:8080/api/productos/$($resultado.id)"
Write-Host "✅ Producto verificado: $($verificacion.nombre)"

# Eliminar producto de prueba
Invoke-RestMethod -Uri "http://localhost:8080/api/productos/$($resultado.id)" -Method DELETE
Write-Host "✅ Producto de prueba eliminado"
```

---

## 📈 Métricas de Performance

### Tiempos de Respuesta Esperados
- **GET /api/productos**: < 50ms
- **GET /api/productos/{id}**: < 20ms
- **POST /api/productos**: < 100ms
- **Health check**: < 10ms

### Uso de Memoria
- **Startup**: ~300-500MB
- **Runtime**: ~400-600MB
- **Con cache**: Estable sin memory leaks

### Logs Importantes a Observar
```
INFO --- DataInitializer: Base de datos inicializada con 12 productos
INFO --- TiendaDeportesBackendApplication: Started TiendaDeportesBackendApplication
DEBUG --- ProductoService: Obteniendo todos los productos activos
```

---

## 🎯 Criterios de Aceptación

**✅ El backend está listo para el frontend si:**

1. ✅ Compila sin errores
2. ✅ Tests pasan exitosamente
3. ✅ Aplicación inicia en < 30 segundos
4. ✅ Health check responde `UP`
5. ✅ H2 Console accesible
6. ✅ API REST responde en todos los endpoints
7. ✅ 12 productos de prueba cargados
8. ✅ CORS configurado para React (puerto 3000)
9. ✅ Logs sin errores críticos
10. ✅ Performance aceptable (< 100ms promedio)

---

**🚀 Una vez verificado, estaremos listos para conectar el frontend React y crear una experiencia de usuario completa.**
