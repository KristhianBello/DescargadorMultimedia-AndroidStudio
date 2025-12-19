from fastapi import FastAPI, UploadFile, Form, HTTPException
from fastapi.responses import FileResponse
from fastapi.middleware.cors import CORSMiddleware
import shutil
import os
import sys
import mimetypes
from pathlib import Path

# Asegurar que el directorio actual está en PYTHONPATH
sys.path.insert(0, str(Path(__file__).parent))

try:
    from downloader import fetch_info, download
except ImportError as e:
    print(f"Error importing downloader: {e}")
    raise


app = FastAPI()

# Configurar CORS para permitir conexiones desde el frontend Android
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Permite todas las IPs en desarrollo
    allow_credentials=True,
    allow_methods=["*"],  # Permite todos los métodos (GET, POST, etc)
    allow_headers=["*"],
)


@app.get('/')
async def health_check():
    """Endpoint para verificar que el servidor está activo"""
    return {"status": "ok", "message": "Servidor conectado"}


@app.post('/info')
async def info(url: str = Form(...)):
    """Obtener información del video/audio desde URL"""
    try:
        data = await fetch_info(url)
        formats = []
        for f in data.get('formats', [])[:20]:
            formats.append({
                'format_id': f.get('format_id'),
                'ext': f.get('ext'),
                'height': f.get('height'),
                'abr': f.get('abr'),
            })
        return {
            'title': data.get('title', 'Sin título'),
            'duration': data.get('duration', 0),
            'formats': formats
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post('/download')
async def api_download(url: str = Form(...), to_mp3: bool = Form(False)):
    """Descargar video en MP4 o convertir a MP3"""
    try:
        path = await download(url, to_mp3=to_mp3)
        
        # Determinar MIME type correcto según extensión
        if to_mp3 or path.suffix.lower() == '.mp3':
            media_type = 'audio/mpeg'
            filename = f"{path.stem}.mp3"
        else:
            media_type = 'video/mp4'
            filename = f"{path.stem}.mp4"
        
        return FileResponse(
            path,
            media_type=media_type,
            filename=filename,
            headers={
                'Content-Disposition': f'attachment; filename="{filename}"'
            }
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))