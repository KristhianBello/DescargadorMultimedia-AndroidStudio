# 🎯 RESUMEN SIMPLIFICADO - LO QUE NECESITA SABER EL BACKEND

## 📱 QUÉ HACE LA APP

La aplicación Android descarga videos de YouTube en MP4 o los convierte a MP3.

---

## 🔌 DÓNDE SE CONECTA

```
IP: 192.168.1.6
Puerto: 8000
URL Base: http://192.168.1.6:8000
```

---

## 📡 QUÉ ENDPOINTS NECESITA IMPLEMENTAR

### 1. GET `/` (Test de conexión)
```
La app lo llama al iniciar para verificar que el servidor está activo.

Respuesta esperada:
  HTTP 200 OK
  Body: cualquier cosa (ej: "ok" o "servidor activo")

Ejemplo:
  curl http://192.168.1.6:8000/
```

---

### 2. POST `/info` (Obtener información del video)
```
La app envía la URL del video y espera el título.

Parámetros POST:
  - url: String (ej: https://www.youtube.com/watch?v=xxxxx)

Respuesta esperada:
  HTTP 200 OK
  Content-Type: application/json
  Body: {"title": "Nombre del video"}

Ejemplo:
  curl -X POST http://192.168.1.6:8000/info \
    -d "url=https://www.youtube.com/watch?v=dQw4w9WgXcQ"
  
  Retorna:
  {"title": "Rick Astley - Never Gonna Give You Up"}
```

---

### 3. POST `/download` (Descargar video/audio)
```
La app envía la URL y si debe convertir a MP3.

Parámetros POST:
  - url: String (ej: https://www.youtube.com/watch?v=xxxxx)
  - to_mp3: String "true" o "false"

Respuesta esperada:
  HTTP 200 OK
  Content-Type: video/mp4 (o audio/mpeg si to_mp3=true)
  Content-Length: [número de bytes]
  Content-Disposition: attachment; filename="video.mp4"
  Body: [archivo binario]

Ejemplos:
  # Descargar como MP4
  curl -X POST http://192.168.1.6:8000/download \
    -d "url=https://www.youtube.com/watch?v=xxxxx&to_mp3=false" \
    -o video.mp4

  # Descargar como MP3
  curl -X POST http://192.168.1.6:8000/download \
    -d "url=https://www.youtube.com/watch?v=xxxxx&to_mp3=true" \
    -o cancion.mp3
```

---

## ⚠️ HEADERS CRÍTICOS (Especialmente en /download)

```
Content-Type: 
  - video/mp4 (si es video)
  - audio/mpeg (si es MP3)

Content-Length: 
  - OBLIGATORIO
  - Número exacto de bytes del archivo
  - Permite que la app muestre progreso en porcentaje

Content-Disposition:
  - OBLIGATORIO
  - Format: attachment; filename="nombre.mp4"
  - Define el nombre del archivo en el dispositivo
```

---

## ⏱️ LÍMITES DE TIEMPO

La app espera respuestas en:
- GET /: 20 segundos
- POST /info: 60 segundos
- POST /download: 60 segundos (para empezar a descargar)

Si tarda más, la app muestra: "⏱️ Timeout"

---

## 🐍 CÓDIGO DE EJEMPLO (Python Flask)

```python
from flask import Flask, request, jsonify, send_file
import yt_dlp

app = Flask(__name__)

# 1. Endpoint de test
@app.route('/', methods=['GET'])
def home():
    return "ok", 200

# 2. Endpoint de información
@app.route('/info', methods=['POST'])
def get_info():
    url = request.form.get('url')
    try:
        with yt_dlp.YoutubeDL({'quiet': True}) as ydl:
            info = ydl.extract_info(url, download=False)
        return jsonify({'title': info.get('title')}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 400

# 3. Endpoint de descarga
@app.route('/download', methods=['POST'])
def download():
    url = request.form.get('url')
    to_mp3 = request.form.get('to_mp3') == 'true'
    
    try:
        opts = {
            'quiet': True,
            'outtmpl': '/tmp/download.%(ext)s',
        }
        
        if to_mp3:
            opts['format'] = 'bestaudio/best'
            opts['postprocessors'] = [{
                'key': 'FFmpegExtractAudio',
                'preferredcodec': 'mp3',
                'preferredquality': '192',
            }]
        else:
            opts['format'] = 'best[ext=mp4]'
        
        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(url, download=True)
            file_path = ydl.prepare_filename(info)
        
        return send_file(
            file_path,
            mimetype='audio/mpeg' if to_mp3 else 'video/mp4',
            as_attachment=True,
            download_name=f'video.{"mp3" if to_mp3 else "mp4"}'
        )
    except Exception as e:
        return jsonify({'error': str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)
```

---

## 📦 DEPENDENCIAS (Python)

```bash
pip install yt-dlp flask flask-cors
```

Si necesitas convertir a MP3, también:
```bash
# Descargar FFmpeg desde: https://ffmpeg.org/download.html
# O en Linux/Mac:
brew install ffmpeg
```

---

## 🧪 CÓMO PROBAR

### Desde tu terminal:

```bash
# 1. ¿Servidor activo?
curl http://192.168.1.6:8000/
→ Debe retornar algo (HTTP 200)

# 2. ¿Info endpoint funciona?
curl -X POST http://192.168.1.6:8000/info \
  -d "url=https://www.youtube.com/watch?v=dQw4w9WgXcQ"
→ Debe retornar JSON: {"title": "..."}

# 3. ¿Download funciona?
curl -X POST http://192.168.1.6:8000/download \
  -d "url=https://www.youtube.com/watch?v=dQw4w9WgXcQ&to_mp3=true" \
  -o cancion.mp3
→ Debe crear archivo cancion.mp3
```

---

## ❌ ERRORES COMUNES

```
"❌ No se conecta al servidor"
  → El servidor no está corriendo
  → Ejecuta: python app.py

"❌ IP no resuelta"
  → La IP 192.168.1.6 no es correcta
  → Verifica con: ipconfig (Windows)

"⏱️ Timeout"
  → El servidor tarda más de 60 segundos
  → Optimiza tu código o aumenta el timeout

"Error: 400"
  → La URL es inválida o no soportada
  → Valida la URL antes de procesarla

"Error: 500"
  → Error en tu servidor
  → Verifica los logs: python app.py (muestra errores)
```

---

## ✅ CHECKLIST PARA IMPLEMENTAR

- [ ] Instalar dependencias (yt-dlp, flask)
- [ ] Crear archivo app.py con los 3 endpoints
- [ ] GET / retorna HTTP 200
- [ ] POST /info retorna JSON con "title"
- [ ] POST /download retorna archivo binario
- [ ] Headers Content-Type correctos
- [ ] Header Content-Length incluido
- [ ] Header Content-Disposition con filename
- [ ] Probar con curl
- [ ] Servidor corriendo en puerto 8000
- [ ] Verificar que Android se conecta

---

## 🚀 PASOS FINALES

1. **Copia el código Python** de arriba
2. **Guarda como `app.py`**
3. **Ejecuta:** `python app.py`
4. **Abre la app Android**
5. **Deberías ver:** "✅ Servidor conectado"
6. **Si no funciona:** Revisa los logs de Python

---

## 📞 DEBUGGING

Cuando ejecutas `python app.py`, verás logs como:
```
 * Running on http://0.0.0.0:8000
127.0.0.1:55123 - - [15/Dec/2024 10:00:00] "GET / HTTP/1.1" 200 -
127.0.0.1:55124 - - [15/Dec/2024 10:00:01] "POST /info HTTP/1.1" 200 -
127.0.0.1:55125 - - [15/Dec/2024 10:00:02] "POST /download HTTP/1.1" 200 -
```

Si ves errores (HTTP 400, 404, 500), ahí está el problema.

---

**Eso es todo lo que necesita saber el backend para implementarlo correctamente.**


