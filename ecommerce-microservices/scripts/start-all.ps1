#!/usr/bin/env pwsh
# ============================================================================
# start-all.ps1 — Start all microservices in the correct order
# ============================================================================
# PREREQUISITES:
#   1. Kafka is already running on localhost:9092
#   2. MySQL is running on localhost:3306
#   3. RSA keys generated (run java scripts\GenerateRSAKeys.java first)
#   4. Databases initialized (run scripts\init-databases.sql first)
# ============================================================================

$ROOT = Split-Path -Parent $PSScriptRoot

function Start-Service {
    param([string]$Name, [string]$Path, [int]$Port)
    Write-Host "Starting $Name on port $Port..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-NoExit", "-Command",
        "cd '$Path'; mvn spring-boot:run; pause" -WindowStyle Normal
}

function Wait-ForHealth {
    param([string]$Name, [string]$Url, [int]$TimeoutSec = 60)
    Write-Host "Waiting for $Name to be healthy ($Url)..." -ForegroundColor Yellow
    $elapsed = 0
    while ($elapsed -lt $TimeoutSec) {
        try {
            $resp = Invoke-WebRequest -Uri $Url -TimeoutSec 3 -ErrorAction Stop
            if ($resp.StatusCode -eq 200) {
                Write-Host "[OK] $Name is UP!" -ForegroundColor Green
                return $true
            }
        } catch { }
        Start-Sleep -Seconds 3
        $elapsed += 3
    }
    Write-Host "[TIMEOUT] $Name did not start within $TimeoutSec seconds" -ForegroundColor Red
    return $false
}

Write-Host ""
Write-Host "============================================================"
Write-Host "   Enterprise E-Commerce Microservices — Starting Up"
Write-Host "============================================================"
Write-Host ""

# Step 1: Config Server
Start-Service "Config Server" "$ROOT\infrastructure\config-server" 8888
Wait-ForHealth "Config Server" "http://localhost:8888/actuator/health"
Start-Sleep -Seconds 3

# Step 2: Discovery Server (Eureka)
Start-Service "Discovery Server" "$ROOT\infrastructure\discovery-server" 8761
Wait-ForHealth "Discovery Server" "http://localhost:8761/actuator/health"
Start-Sleep -Seconds 5

# Step 3: API Gateway
Start-Service "API Gateway" "$ROOT\infrastructure\api-gateway" 8080
Start-Sleep -Seconds 10

# Step 4: Business Services (in parallel)
Write-Host "Starting all business services..." -ForegroundColor Cyan
Start-Service "Customer Service"     "$ROOT\microservices\customer-service"     8081
Start-Service "Product Service"      "$ROOT\microservices\product-service"      8082
Start-Service "Order Service"        "$ROOT\microservices\order-service"        8083
Start-Service "Notification Service" "$ROOT\microservices\notification-service" 8084
Start-Service "Admin Service"        "$ROOT\microservices\admin-service"        8085
Start-Service "Payment Service"      "$ROOT\microservices\payment-service"      8086

Write-Host ""
Write-Host "All services starting. Check Eureka dashboard at:"
Write-Host "  http://localhost:8761"
Write-Host ""
Write-Host "Frontend: cd frontend && npm run dev"
Write-Host ""
Write-Host "Check logs in each PowerShell window for startup status."
