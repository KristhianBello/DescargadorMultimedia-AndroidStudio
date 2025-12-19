import asyncio
import os
from yt_dlp import YoutubeDL
from pathlib import Path
import shutil


TMP_DIR = Path("./tmp")
TMP_DIR.mkdir(exist_ok=True)

# Buscar FFmpeg en el PATH
FFMPEG_PATH = shutil.which("ffmpeg")
FFPROBE_PATH = shutil.which("ffprobe")


async def fetch_info(url: str) -> dict:
    ydl_opts = { 'skip_download': True }
    loop = asyncio.get_event_loop()
    def run():
        with YoutubeDL(ydl_opts) as ydl:
            return ydl.extract_info(url, download=False)
    info = await loop.run_in_executor(None, run)
    return info


async def download(url: str, to_mp3: bool = False) -> Path:
    """Descargar video/audio y convertir a MP3 si es necesario"""
    out_template = str(TMP_DIR / '%(id)s.%(ext)s')
    ydl_opts = {
        'format': 'best',
        'outtmpl': out_template,
        'noplaylist': True,
        'quiet': False,
    }
    
    # Solo agregar postprocessors si FFmpeg está disponible
    if to_mp3 and FFMPEG_PATH:
        ydl_opts.update({
            'ffmpeg_location': FFMPEG_PATH,
            'postprocessors': [{
                'key': 'FFmpegExtractAudio',
                'preferredcodec': 'mp3',
                'preferredquality': '192',
            }]
        })
    
    loop = asyncio.get_event_loop()
    
    def run():
        with YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            video_id = info.get('id')
            
            # Si es MP3, buscar el archivo .mp3 resultante
            if to_mp3 and FFMPEG_PATH:
                mp3_path = TMP_DIR / f'{video_id}.mp3'
                if mp3_path.exists():
                    return mp3_path
                # Si no existe, intentar con otros nombres posibles
                for file in TMP_DIR.glob(f'{video_id}.*'):
                    if file.suffix.lower() == '.mp3':
                        return file
            
            # De lo contrario, usar el nombre del archivo descargado
            filename = ydl.prepare_filename(info)
            return Path(filename)
    
    path = await loop.run_in_executor(None, run)
    return path