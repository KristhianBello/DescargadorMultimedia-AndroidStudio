# Script para obtener la IP de tu máquina
Write-Host "========================================" -ForegroundColor Green
Write-Host "OBTENER IP PARA CONECTAR DESDE ANDROID" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Obtener todas las IPs
$ips = ipconfig | Select-String "Dirección IPv4"

Write-Host "IPs disponibles en tu máquina:" -ForegroundColor Yellow
$ips

Write-Host ""
Write-Host "IMPORTANTE:" -ForegroundColor Red
Write-Host "1. Busca la IP que comience con 192.168.x.x o 10.x.x.x" -ForegroundColor Red
Write-Host "2. Esa es la IP de tu máquina en la red local" -ForegroundColor Red
Write-Host "3. El teléfono/emulador DEBE estar en la misma red WiFi" -ForegroundColor Red
Write-Host ""
Write-Host "Ejemplo de cambio en MainActivity.kt:" -ForegroundColor Cyan
Write-Host 'private val BASE_URL = "http://192.168.1.100:8000"' -ForegroundColor Cyan
Write-Host ""
Write-Host "Luego:" -ForegroundColor Yellow
Write-Host "1. Recompila la app" -ForegroundColor Yellow
Write-Host "2. Instala en el dispositivo" -ForegroundColor Yellow
Write-Host "3. Asegúrate que el servidor está corriendo en Python" -ForegroundColor Yellow
Write-Host ""

