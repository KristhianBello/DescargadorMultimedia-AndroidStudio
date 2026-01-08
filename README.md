# 📥 Descargador Multimedia

Una aplicación Android para descargar videos y audios de plataformas como YouTube, con un backend Python basado en FastAPI.

## 🎯 Características

- ✅ Descarga videos de YouTube y otras plataformas
- ✅ Extrae información de videos (formatos disponibles, duración, etc.)
- ✅ Interfaz intuitiva en Android
- ✅ Backend escalable con FastAPI
- ✅ Soporte para múltiples formatos de descarga
- ✅ Comunicación cliente-servidor segura

## 📁 Estructura del Proyecto

```
DescargadorMultimedia/
├── app/                              # Código Android (Kotlin/Java)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/               # Código fuente Java/Kotlin
│   │   │   ├── res/                # Recursos (layouts, strings, etc)
│   │   │   └── AndroidManifest.xml # Configuración de la app
│   │   ├── test/                   # Tests unitarios
│   │   └── androidTest/            # Tests instrumentados
│   └── build.gradle.kts            # Dependencias Android
│
├── backend/                         # Servidor Python
│   ├── app/
│   │   ├── main.py                # Servidor FastAPI
│   │   ├── downloader.py          # Lógica de descargas (yt-dlp)
│   │   ├── run_server.py          # Script para ejecutar servidor
│   │   ├── requirements.txt       # Dependencias Python
│   │   └── tmp/                   # Archivos temporales
│   └── Dockerfile                 # Para ejecutar en Docker
│
├── gradle/                         # Configuración de Gradle
├── build.gradle.kts               # Configuración raíz
├── settings.gradle.kts            # Configuración de módulos
├── gradle.properties              # Propiedades de Gradle
└── local.properties               # Configuración local (SDK path)
```

## 🛠️ Tecnologías Utilizadas

### Frontend (Android)
- **Lenguaje:** Kotlin/Java
- **SDK Android:** API 23 (Android 6.0) - API 36 (Android 15)
- **Framework:** Android Jetpack
- **Dependencias principales:**
  - AndroidX Core
  - AppCompat
  - CardView
  - Lifecycle

### Backend (Python)
- **Framework:** FastAPI
- **Servidor:** Uvicorn
- **Descarga de media:** yt-dlp
- **CORS:** Habilitado para comunicación cliente-servidor
- **Async:** Soporte completo

## 🚀 Instalación y Configuración

### Frontend (Android)

#### Requisitos Previos
- Android Studio 2023.3 o superior
- JDK 11 o superior
- SDK Android API 36

#### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/KristhianBello/DescargadorMultimedia-AndroidStudio.git
   cd DescargadorMultimedia
   ```

2. **Abrir en Android Studio**
   - Abre Android Studio
   - Selecciona "File → Open" y busca la carpeta del proyecto
   - Espera a que se descarguen las dependencias

3. **Configurar local.properties**
   - Crea o edita `local.properties`
   - Agrega la ruta del SDK:
     ```
     sdk.dir=/path/to/android/sdk
     ```

4. **Compilar y ejecutar**
   - Conecta un dispositivo Android o usa el emulador
   - Click en "Run" o presiona `Shift + F10`

### Backend (Python)

#### Requisitos Previos
- Python 3.8 o superior
- pip o conda

#### Pasos de Instalación

1. **Instalar dependencias**
   ```bash
   cd backend
   pip install -r app/requirements.txt
   ```

2. **Ejecutar el servidor**
   ```bash
   python app/run_server.py
   ```
   
   O manualmente:
   ```bash
   cd app
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```

3. **Verificar conexión**
   - Desktop: Abre `http://localhost:8000/`
   - Android: Usa la IP de tu máquina: `http://TU_IP:8000/`

#### Con Docker
```bash
cd backend
docker build -t descargador-multimedia .
docker run -p 8000:8000 descargador-multimedia
```

## 📡 API Endpoints

### Health Check
```http
GET /
```
Verifica que el servidor está activo

**Respuesta:**
```json
{
  "status": "ok",
  "message": "Servidor conectado"
}
```

### Obtener Información del Video
```http
POST /info
Content-Type: application/x-www-form-urlencoded

url=https://www.youtube.com/watch?v=...
```

**Respuesta:**
```json
{
  "title": "Título del video",
  "duration": 300,
  "formats": [
    {
      "format_id": "18",
      "ext": "mp4",
      "height": 360
    }
  ]
}
```

## 📱 Uso de la Aplicación

1. **Abre la app en tu dispositivo Android**
2. **Ingresa la URL del video** (YouTube, Spotify, etc.)
3. **Selecciona formato y calidad** de la lista disponible
4. **Presiona descargar** y espera a que se complete
5. **Accede a tus descargas** en la carpeta de la app

## ⚙️ Configuración de Red

### En la Aplicación Android
- Reemplaza `localhost` por tu IP local
- En Windows: `ipconfig`
- En Mac/Linux: `ifconfig`

Ejemplo: `http://162.188.1.100:8000`

### Firewall
- Asegúrate que el puerto 8000 esté abierto en tu firewall
- Windows Defender: Permitir la app en Redes Públicas/Privadas

## 📋 Requisitos del Sistema

### Android
- Mínimo: Android 6.0 (API 23)
- Objetivo: Android 15 (API 36)
- Espacio: 100 MB

### Python (Backend)
- Python 3.8+
- 200 MB de espacio en disco (sin descargas)

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver `LICENSE` para más detalles.

## 📧 Contacto

**Autor:** Kristhian Bello  
**Repositorio:** [GitHub - DescargadorMultimedia-AndroidStudio](https://github.com/KristhianBello/DescargadorMultimedia-AndroidStudio.git)

## 🔗 Enlaces Útiles

- [yt-dlp Documentation](https://github.com/yt-dlp/yt-dlp)
- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [Android Documentation](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)

## 🐛 Reporte de Errores

Si encuentras un error, por favor:
1. Verifica que el servidor está corriendo
2. Revisa la consola de Android Studio para logs
3. Abre un issue en GitHub con los detalles

---

**Última actualización:** 8 de enero de 2026
