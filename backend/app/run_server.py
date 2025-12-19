#!/usr/bin/env python
import uvicorn
import sys
from pathlib import Path

# Asegurar que el directorio actual está en PYTHONPATH
sys.path.insert(0, str(Path(__file__).parent))

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level="info"
    )
