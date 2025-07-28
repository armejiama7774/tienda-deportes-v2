# Log de Evolución Arquitectónica

## Problema Detectado - Día 1 de Fase 2

### Contexto
Agregamos sistema de notificaciones al crear productos.

### Implementación Inicial (Ingenua)
```java
// Método crearProducto() con notificaciones hardcodeadas
private void enviarNotificaciones(Producto producto) {
    enviarEmailAdministradores(producto);
    actualizarCacheCatalogo(producto);
    registrarAuditoria(producto);
}
```

### Problemas Identificados Inmediatamente

#### 1. **Violación del Principio de Responsabilidad Única (SRP)**
- `ProductoService` ahora maneja:
  - Validación de productos ✓
  - Persistencia de productos ✓
  - Envío de emails ❌
  - Gestión de cache ❌
  - Auditoría ❌

#### 2. **Testing Problemático**
- ¿Cómo testear notificaciones sin enviar emails reales?
- ¿Cómo verificar que el cache se actualizó?
- ¿Cómo simular fallos en servicios externos?
- Métodos privados no se pueden testear directamente

#### 3. **Manejo de Errores Inconsistente**
- Si falla el email, ¿falla toda la creación?
- Si el cache no se actualiza, ¿es crítico?
- ¿Qué hacemos con errores parciales?

#### 4. **Escalabilidad Problemática**
- ¿Dónde agregamos SMS, webhooks, push notifications?
- ¿Cómo manejamos latencia de servicios externos?
- ¿Qué pasa cuando tengamos 10+ tipos de notificaciones?

#### 5. **Acoplamiento Alto**
- Cambios en lógica de email requieren tocar ProductoService
- Nuevos tipos de notificaciones requieren modificar código existente
- Imposible reutilizar notificaciones en otros contextos

### Próximo Paso
Implementar patrón Observer/Publisher-Subscriber para desacoplar notificaciones.

### Métricas de "Dolor"
- **Complejidad**: ProductoService pasó de 1 responsabilidad a 4+
- **Testabilidad**: Difícil testear funcionalidad nueva
- **Mantenibilidad**: Cada cambio en notificaciones toca código crítico
- **Escalabilidad**: No preparado para crecimiento de funcionalidades