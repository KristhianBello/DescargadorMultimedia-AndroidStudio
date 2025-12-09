# Script de Diagnóstico Completo - PowerShell
# Ejecutar como: powershell -ExecutionPolicy Bypass -File diagnose_backend.ps1

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         DIAGNÓSTICO COMPLETO DE BACKEND                   ║" -ForegroundColor Cyan
Write-Host "║         Servidor esperado: 192.168.1.48:8001              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Mostrar IP Local
Write-Host "[1] Mostrando IP Local..." -ForegroundColor Yellow
Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray
$ips = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.IPAddress -notmatch "^127\." -and $_.IPAddress -notmatch "^169\."}
foreach ($ip in $ips) {
    Write-Host "  $($ip.InterfaceAlias): $($ip.IPAddress)" -ForegroundColor Green
}
Write-Host ""

# Paso 2: Verificar si puerto 8001 está abierto localmente
Write-Host "[2] Verificando puertos abiertos en esta máquina..." -ForegroundColor Yellow
Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray
$puerto_local = Get-NetTCPConnection -LocalPort 8001 -ErrorAction SilentlyContinue
if ($puerto_local) {
    Write-Host "  ✅ Puerto 8001 está ESCUCHANDO en esta máquina:" -ForegroundColor Green
    $puerto_local | Select-Object LocalAddress, State, OwningProcess | Format-Table -AutoSize
} else {
    Write-Host "  ⚠️  Puerto 8001 NO está escuchando en esta máquina" -ForegroundColor Yellow
    Write-Host "     Buscar otros puertos con servidor:" -ForegroundColor Gray
    Get-NetTCPConnection -State Listen | Where-Object {$_.LocalAddress -notmatch "^127\." -and $_.LocalAddress -notmatch "^169\."} | Select-Object LocalAddress, LocalPort, State | Format-Table -AutoSize
}
Write-Host ""

# Paso 3: Probar ping a 192.168.1.48
Write-Host "[3] Probando conectividad a 192.168.1.48..." -ForegroundColor Yellow
Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray
$ping_result = Test-Connection -ComputerName 192.168.1.48 -Count 1 -Quiet -ErrorAction SilentlyContinue
if ($ping_result) {
    Write-Host "  ✅ IP 192.168.1.48 es ALCANZABLE" -ForegroundColor Green
    Test-Connection -ComputerName 192.168.1.48 -Count 1 | Select-Object Address, IPV4Address, ResponseTime | Format-Table
} else {
    Write-Host "  ❌ IP 192.168.1.48 NO es alcanzable" -ForegroundColor Red
    Write-Host "     Posibles causas:" -ForegroundColor Red
    Write-Host "     - IP incorrecta" -ForegroundColor Red
    Write-Host "     - Dispositivo desconectado" -ForegroundColor Red
    Write-Host "     - Red diferente" -ForegroundColor Red
}
Write-Host ""

# Paso 4: Probar conexión a puerto 8001
Write-Host "[4] Probando conexión a http://192.168.1.48:8001/" -ForegroundColor Yellow
Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://192.168.1.48:8001/" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  ✅ CONEXIÓN EXITOSA AL SERVIDOR" -ForegroundColor Green
    Write-Host "     Código HTTP: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "     Descripción: $($response.StatusDescription)" -ForegroundColor Green
    Write-Host "     Tamaño respuesta: $($response.RawContentLength) bytes" -ForegroundColor Green
} catch [System.Net.Http.HttpRequestException] {
    if ($_.Exception.Message -like "*Connection refused*" -or $_.Exception.Message -like "*refused*") {
        Write-Host "  ❌ CONEXIÓN RECHAZADA EN PUERTO 8001" -ForegroundColor Red
        Write-Host "     Posibles causas:" -ForegroundColor Red
        Write-Host "     - Servidor NO está corriendo en puerto 8001" -ForegroundColor Red
        Write-Host "     - Servidor escucha en puerto diferente" -ForegroundColor Red
        Write-Host "     - Firewall bloqueando puerto 8001" -ForegroundColor Red
    } elseif ($_.Exception.Message -like "*timeout*" -or $_.Exception.Message -like "*timed out*") {
        Write-Host "  ⏱️  TIMEOUT - Servidor no responde en tiempo" -ForegroundColor Red
        Write-Host "     El servidor puede estar muy lento" -ForegroundColor Red
    } else {
        Write-Host "  ❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ ERROR: $($_.Exception.GetType().Name)" -ForegroundColor Red
    Write-Host "     $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Paso 5: Probar endpoints específicos
Write-Host "[5] Probando endpoints del servidor..." -ForegroundColor Yellow
Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray

$endpoints = @(
    @{url = "http://192.168.1.48:8001/"; nombre = "Raíz (/)"; metodo = "GET"},
    @{url = "http://192.168.1.48:8001/info"; nombre = "Info (/info)"; metodo = "POST"},
    @{url = "http://192.168.1.48:8001/download"; nombre = "Download (/download)"; metodo = "POST"}
)

foreach ($endpoint in $endpoints) {
    try {
        if ($endpoint.metodo -eq "GET") {
            $result = Invoke-WebRequest -Uri $endpoint.url -TimeoutSec 3 -ErrorAction Stop
            Write-Host "  ✅ $($endpoint.nombre) - HTTP $($result.StatusCode)" -ForegroundColor Green
        } else {
            Write-Host "  ℹ️  $($endpoint.nombre) - Requiere POST (no probado)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  ❌ $($endpoint.nombre) - Error" -ForegroundColor Red
    }
}
Write-Host ""

# Paso 6: Sugerencias
Write-Host "[6] RECOMENDACIONES" -ForegroundColor Yellow
Write-Host "────────────────────────────────────────────────────────" -ForegroundColor Gray
Write-Host ""
Write-Host "Si el servidor no responde:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1. Verifica que el backend está corriendo:" -ForegroundColor White
Write-Host "     netstat -ano | findstr :8001" -ForegroundColor Cyan
Write-Host ""
Write-Host "  2. Si está en puerto diferente, busca:" -ForegroundColor White
Write-Host "     netstat -ano | findstr LISTENING" -ForegroundColor Cyan
Write-Host ""
Write-Host "  3. Inicia el servidor backend en puerto 8001" -ForegroundColor White
Write-Host ""
Write-Host "  4. Desactiva firewall temporalmente:" -ForegroundColor White
Write-Host "     netsh advfirewall set allprofiles state off" -ForegroundColor Cyan
Write-Host ""
Write-Host "  5. Si la IP cambió, actualiza en Android:" -ForegroundColor White
Write-Host "     MainActivity.kt línea 35: BASE_URL = ...tu_nueva_ip..." -ForegroundColor Cyan
Write-Host ""

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         FIN DEL DIAGNÓSTICO                              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

