#!/usr/bin/env pwsh
# ============================================================================
# kill-ports.ps1 — Kill all E-Commerce microservice ports
# Run when you need to restart all services cleanly.
# ============================================================================

$ports = @(8888, 8761, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 5173)

foreach ($port in $ports) {
    $pids = netstat -ano | Select-String ":$port " | ForEach-Object {
        ($_ -split '\s+')[-1]
    } | Sort-Object -Unique

    foreach ($pid in $pids) {
        if ($pid -match '^\d+$' -and $pid -ne '0') {
            try {
                $proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
                if ($proc) {
                    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                    Write-Host "[KILLED] Port $port — PID $pid ($($proc.ProcessName))" -ForegroundColor Green
                }
            } catch {
                Write-Host "[SKIP]  Port $port — PID $pid (already stopped)" -ForegroundColor Yellow
            }
        }
    }
}

Write-Host ""
Write-Host "All service ports cleared. Ready to restart." -ForegroundColor Cyan
