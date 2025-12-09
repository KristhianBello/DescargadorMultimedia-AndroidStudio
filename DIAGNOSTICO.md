# Diagnóstico de Conexión - Error: Failed to connect to 192.168.1.48:8000

## Cambios realizados:

### ✅ 1. Configuración de Red Verificada
- **Base URL**: `http://192.168.1.48:8000` ✓
- **Network Security Config**: Permite tráfico sin encriptar en `192.168.1.48` ✓
- **AndroidManifest.xml**: Configurado correctamente ✓
- **Permisos**: INTERNET agregado ✓

### ✅ 2. Mejoras Implementadas

#### a) OkHttpClient Actualizado
```kotlin
private val client = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)  // ← Nuevo
    .build()
```

#### b) Manejo de Errores Mejorado
- Detección específica de `ConnectException`
- Detección de `UnknownHostException`
- Detección de `SocketTimeoutException`
- Mensajes de error más claros

#### c) Prueba de Conexión Automática
- Al abrir la app, intenta conectar a `$BASE_URL/`
- Muestra un mensaje si hay problemas de conectividad

---

## 🔍 Posibles Causas del Error

### 1. **Servidor NO está corriendo**
   - Verifica que el backend en `192.168.1.48:8000` esté activo
   - Comprueba en la consola del servidor

### 2. **IP Incorrecta**
   - Verifica que la IP privada sea correcta con `ipconfig` en Windows
   - Busca la IP en "Ethernet" o "Wi-Fi"

### 3. **No estás en la misma red**
   - El teléfono debe estar conectado a la MISMA red WiFi que la IP
   - No puede ser una red diferente o hotspot

### 4. **Firewall bloqueando**
   - Desactiva el firewall temporalmente en el servidor
   - O permite el puerto 8000 en el firewall

### 5. **Puerto incorrecto**
   - Verifica que el servidor escuche en puerto `8000`
   - No confundir con otros puertos (8080, 5000, etc.)

---

## 🧪 Cómo Diagnosticar

### Desde Windows (en la máquina del servidor):
```powershell
# Ver tu IP
ipconfig

# Probar que el servidor está corriendo
curl http://localhost:8000

# O si tienes netstat:
netstat -ano | findstr :8000
```

### Desde el teléfono Android:
1. Abre la app
2. Verás un mensaje mostrando el estado de conexión
3. Si dice "Conectado a 192.168.1.48:8000" → Todo OK ✓
4. Si dice "No se puede conectar" → Revisa las causas arriba

---

## ✅ Checklist de Solución

- [ ] Servidor backend está activo en `192.168.1.48:8000`
- [ ] Teléfono está conectado a la misma red WiFi
- [ ] IP es correcta (verificar con `ipconfig`)
- [ ] Puerto 8000 está habilitado (no bloqueado por firewall)
- [ ] La app muestra "Conectado a 192.168.1.48:8000" al abrir

---

## 📝 Información Técnica

**Configuración Actual:**
- Base URL: `http://192.168.1.48:8000`
- Timeout conexión: 20 segundos
- Timeout lectura: 60 segundos
- Reintentos: Habilitados

**Archivos Modificados:**
- `MainActivity.kt` - Mejorado manejo de errores
- Network security config - Ya correctamente configurado

---

## 🔧 Si Necesitas Cambiar la IP

Si la IP cambia, edita esta línea en `MainActivity.kt`:
```kotlin
private val BASE_URL = "http://192.168.1.48:8000"  // ← Cambia aquí la IP
```

Luego reconstruye y redeploy la app.

