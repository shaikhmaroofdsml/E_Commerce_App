@echo off
:: ============================================================================
:: E-Commerce Microservices — Docker Build & Run Helper (Windows)
:: ============================================================================
:: Usage:
::   scripts\docker-start.bat         -> Build all images and start all containers
::   scripts\docker-start.bat --no-build -> Start without rebuilding images
:: ============================================================================

SET COMPOSE_FILE=%~dp0..\docker-compose.yml
SET ENV_FILE=%~dp0..\.env

echo.
echo  ╔══════════════════════════════════════════════════════════╗
echo  ║   Enterprise E-Commerce Microservices — Docker Setup    ║
echo  ╚══════════════════════════════════════════════════════════╝
echo.

if not exist "%ENV_FILE%" (
    echo [WARN] .env file not found. Using default credentials.
    echo        Copy .env from the project root and customize it.
    echo.
)

if "%1"=="--no-build" (
    echo [INFO] Starting containers (no rebuild)...
    docker compose -f "%COMPOSE_FILE%" up -d
) else (
    echo [INFO] Building all Docker images (this takes a few minutes on first run)...
    docker compose -f "%COMPOSE_FILE%" build --parallel
    echo.
    echo [INFO] Starting all containers...
    docker compose -f "%COMPOSE_FILE%" up -d
)

echo.
echo [INFO] Waiting for services to become healthy...
timeout /t 15 /nobreak > nul

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║   Service URLs                                          ║
echo ╠══════════════════════════════════════════════════════════╣
echo ║   Frontend         →  http://localhost:3000             ║
echo ║   API Gateway      →  http://localhost:8080             ║
echo ║   Swagger UI       →  http://localhost:8080/swagger-ui.html
echo ║   Eureka Dashboard →  http://localhost:8761             ║
echo ║   Config Server    →  http://localhost:8888             ║
echo ╚══════════════════════════════════════════════════════════╝
echo.
echo [INFO] Run  docker compose ps  to check container health.
echo [INFO] Run  docker compose logs -f ^<service^>  to stream logs.
echo.
