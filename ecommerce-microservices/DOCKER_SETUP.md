# 🐳 Docker Setup & Run Guide

> **Enterprise E-Commerce Microservices Platform**
>
> This guide walks you through installing Docker Desktop on Windows and running all 13 containers of the platform with a single command.

---

## 📋 Table of Contents

1. [System Requirements](#1-system-requirements)
2. [Step 1 — Enable Virtualisation in BIOS](#step-1--enable-virtualisation-in-bios)
3. [Step 2 — Install WSL2](#step-2--install-wsl2)
4. [Step 3 — Install Docker Desktop](#step-3--install-docker-desktop)
5. [Step 4 — Configure Docker Desktop](#step-4--configure-docker-desktop)
6. [Step 5 — Verify Docker Installation](#step-5--verify-docker-installation)
7. [Step 6 — Install Git](#step-6--install-git)
8. [Step 7 — Clone the Repository](#step-7--clone-the-repository)
9. [Step 8 — Configure Environment Variables](#step-8--configure-environment-variables)
10. [Step 9 — Build Docker Images](#step-9--build-docker-images)
11. [Step 10 — Start All Services](#step-10--start-all-services)
12. [Step 11 — Monitor Startup Progress](#step-11--monitor-startup-progress)
13. [Step 12 — Verify Everything Works](#step-12--verify-everything-works)
14. [Day-to-Day Commands](#day-to-day-commands)
15. [Service URLs & Ports](#service-urls--ports)
16. [Troubleshooting](#troubleshooting)

---

## 1. System Requirements

Before you begin, make sure your PC meets these minimum requirements:

| Requirement | Minimum | Recommended |
|---|---|---|
| **OS** | Windows 10 64-bit (Build 19041+) | Windows 11 |
| **RAM** | 8 GB | 16 GB |
| **Disk Space** | 20 GB free | 40 GB free |
| **CPU** | 64-bit with VT-x / AMD-V | 8+ cores |
| **BIOS** | Virtualisation enabled | — |

> 💡 **Why so much RAM?** This platform runs **13 containers simultaneously** (9 Spring Boot services + MySQL + Kafka + Zookeeper + Nginx). Each JVM takes ~300–500 MB.

---

## Step 1 — Enable Virtualisation in BIOS

> ⏭️ **Skip this step** if you can already run Hyper-V, VirtualBox, or WSL2 without issues on your machine.

Docker Desktop requires hardware virtualisation to be enabled in your BIOS/UEFI.

### How to enter BIOS

1. **Restart** your PC
2. Press the BIOS key repeatedly as soon as the screen turns on:
   | Manufacturer | Key |
   |---|---|
   | Dell | **F2** or **F12** |
   | HP | **F10** or **ESC** |
   | Lenovo | **F1**, **F2**, or **Enter** then F1 |
   | ASUS | **DEL** or **F2** |
   | MSI | **DEL** |
   | Acer | **F2** or **DEL** |

### Enable virtualisation

3. Navigate to **Advanced** → **CPU Configuration**  
   *(exact menu name varies by motherboard)*
4. Find the setting:
   - **Intel CPUs:** `Intel Virtualization Technology (VT-x)` → Set to **Enabled**
   - **AMD CPUs:** `AMD-V` or `SVM Mode` → Set to **Enabled**
5. Press **F10** to Save and Exit

### Verify in Windows

After booting, open **Task Manager** (`Ctrl + Shift + Esc`) → **Performance** tab → **CPU**:

```
Virtualisation: Enabled   ✅
```

---

## Step 2 — Install WSL2

WSL2 (Windows Subsystem for Linux 2) is required by Docker Desktop for the best performance on Windows.

### Install WSL2

1. Open **PowerShell as Administrator**
   - Right-click the **Start** button → **Windows PowerShell (Admin)**

2. Run this single command:
   ```powershell
   wsl --install
   ```
   This automatically installs WSL2 + Ubuntu.

3. **Restart your PC** when prompted.

4. After restart, an **Ubuntu** terminal opens automatically.
   - Create a **username** (e.g., `ubuntu`)
   - Create a **password** (this is your Linux user password — remember it)

5. Verify WSL2 is the default version:
   ```powershell
   wsl --status
   ```
   Expected output:
   ```
   Default Version: 2
   ```

### If `wsl --install` fails

Run these commands instead (as Administrator):

```powershell
# Enable WSL feature
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart

# Enable Virtual Machine Platform
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

# Restart PC, then set WSL2 as default
wsl --set-default-version 2

# Install Ubuntu manually from Microsoft Store
# Search "Ubuntu" in the Microsoft Store app and click Install
```

---

## Step 3 — Install Docker Desktop

1. Open your browser and go to:
   **https://www.docker.com/products/docker-desktop/**

2. Click **Download Docker Desktop for Windows**

3. Run the downloaded installer: `Docker Desktop Installer.exe`

4. On the **Configuration** screen during installation:
   - ✅ **Use WSL 2 instead of Hyper-V** — **keep this checked**
   - ✅ **Add shortcut to desktop** — optional

5. Click **OK** and wait for installation to complete (~2–5 minutes)

6. Click **Close and restart** when the installer finishes

---

## Step 4 — Configure Docker Desktop

After your PC restarts, Docker Desktop launches automatically.

### Initial setup

1. Accept the **Docker Subscription Service Agreement**
2. You may sign in or click **Continue without signing in** — signing in is optional for local use

### Allocate enough resources

Docker Desktop → click the **gear icon ⚙️** (Settings) in the top-right corner:

**Resources → Memory:**
```
Move the slider to at least 6 GB
(8 GB or more is strongly recommended)
```

**Resources → CPU:**
```
Set to at least 4 CPUs
```

**Resources → Disk image size:**
```
Set to at least 60 GB
(Docker images + build cache can get large)
```

**General:**
- ✅ **Start Docker Desktop when you log in** — recommended so Docker is always available

**WSL Integration** (Settings → Resources → WSL Integration):
- ✅ Turn on **Enable integration with my default WSL distro**
- ✅ Also enable it for **Ubuntu** if listed

Click **Apply & Restart** to save all settings.

---

## Step 5 — Verify Docker Installation

Open a **new PowerShell** window (after Docker Desktop restarts) and run:

```powershell
# Check Docker version
docker --version
```
✅ Expected: `Docker version 27.x.x, build ...`

```powershell
# Check Docker Compose version
docker compose version
```
✅ Expected: `Docker Compose version v2.x.x`

```powershell
# Run the test container
docker run hello-world
```
✅ Expected output includes: `Hello from Docker!`

> ⚠️ If you see `Cannot connect to the Docker daemon`, make sure Docker Desktop is **running** (check the system tray — the whale icon 🐳 should be visible and steady, not animated).

---

## Step 6 — Install Git

If you don't have Git installed:

1. Go to **https://git-scm.com/download/win**
2. Click **Download for Windows** (the 64-bit installer)
3. Run the installer with **default settings** (click Next through all screens)
4. Verify:
   ```powershell
   git --version
   ```
   ✅ Expected: `git version 2.x.x`

---

## Step 7 — Clone the Repository

Open **PowerShell** and navigate to the folder where you want the project:

```powershell
# Example: go to your Desktop
cd C:\Users\YourName\Desktop

# Clone the repository
git clone <your-repo-url>

# Enter the project folder
cd ecommerce-microservices
```

After cloning, verify the project structure:

```
ecommerce-microservices/
├── docker-compose.yml          ← Orchestrates all 13 containers
├── .env                        ← Environment variable configuration
├── .dockerignore               ← Excludes unnecessary files from builds
├── infrastructure/
│   ├── config-server/
│   │   └── Dockerfile
│   ├── discovery-server/
│   │   └── Dockerfile
│   └── api-gateway/
│       └── Dockerfile
├── microservices/
│   ├── customer-service/Dockerfile
│   ├── product-service/Dockerfile
│   ├── order-service/Dockerfile
│   ├── payment-service/Dockerfile
│   ├── notification-service/Dockerfile
│   └── admin-service/Dockerfile
├── frontend/
│   ├── Dockerfile
│   └── nginx.conf
└── scripts/
    ├── init-databases.sql
    └── docker-start.bat        ← Windows helper script
```

---

## Step 8 — Configure Environment Variables

The project comes with a `.env` file that contains all configuration. The **defaults work out of the box** — no changes needed for a first run.

Open `.env` in Notepad or VS Code to review:

```env
# ── MySQL ────────────────────────────────────────────────
# Root password for the MySQL container
MYSQL_ROOT_PASSWORD=root_password_change_me

# ── Config Server ────────────────────────────────────────
CONFIG_SERVER_USER=config-admin
CONFIG_SERVER_PASSWORD=config-secret

# ── Eureka Discovery ─────────────────────────────────────
EUREKA_USER=eureka
EUREKA_PASSWORD=eureka-secret

# ── Email (Notification Service) ─────────────────────────
# MAIL_STUB_MODE=true  → emails are logged to console (no real sending)
# MAIL_STUB_MODE=false → set MAIL_USERNAME and MAIL_PASSWORD below
MAIL_USERNAME=stub@example.com
MAIL_PASSWORD=stub-password
MAIL_STUB_MODE=true
```

### To enable real email sending (optional)

```env
MAIL_STUB_MODE=false
MAIL_USERNAME=your.email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

> 💡 For Gmail: generate an **App Password** at https://myaccount.google.com/apppasswords (requires 2FA to be enabled)

---

## Step 9 — Build Docker Images

This step compiles all 9 Spring Boot services and the React frontend into Docker images.

> ⏱️ **First run takes 15–25 minutes** — Maven downloads ~500 MB of dependencies and compiles all Java code.  
> **Subsequent builds take ~1–2 minutes** because Docker caches the dependency layer.

### Option A — Build all at once (simple)

```powershell
docker compose build
```

### Option B — Build in parallel (faster on multi-core CPUs)

```powershell
docker compose build --parallel
```

### Option C — Build with full output visible

```powershell
docker compose build --progress=plain
```

### Watch what's being built

During the build you'll see output like:

```
 => [config-server build 1/6] FROM maven:3.9-eclipse-temurin-21        ✓
 => [config-server build 2/6] WORKDIR /workspace                       ✓
 => [config-server build 3/6] COPY pom.xml                             ✓
 => [config-server build 4/6] RUN mvn dependency:go-offline            ✓  (slow on first run)
 => [config-server build 5/6] COPY src                                 ✓
 => [config-server build 6/6] RUN mvn package -DskipTests              ✓
 => [config-server] COPY --from=build *.jar app.jar                    ✓
```

---

## Step 10 — Start All Services

Once images are built, start all 13 containers:

```powershell
docker compose up -d
```

The `-d` flag runs everything in **detached mode** (background), so your terminal stays free.

### Combine build + start in one command

```powershell
docker compose up -d --build
```

### Use the Windows helper script

```bat
scripts\docker-start.bat
```

This script builds, starts, waits 15 seconds, then prints all the service URLs for you.

---

## Step 11 — Monitor Startup Progress

Services start in a controlled order using health checks. **Full startup takes 3–5 minutes** after the containers launch.

### Check container status

```powershell
docker compose ps
```

Wait until you see output like this — all containers should show `Up` or `Up (healthy)`:

```
NAME                            IMAGE                           STATUS
ecommerce-mysql                 mysql:8.0                       Up (healthy)
ecommerce-zookeeper             bitnami/zookeeper:3.9           Up (healthy)
ecommerce-kafka                 bitnami/kafka:3.7               Up (healthy)
ecommerce-config-server         ecommerce/config-server         Up (healthy)
ecommerce-discovery-server      ecommerce/discovery-server      Up (healthy)
ecommerce-api-gateway           ecommerce/api-gateway           Up (healthy)
ecommerce-customer-service      ecommerce/customer-service      Up
ecommerce-product-service       ecommerce/product-service       Up
ecommerce-order-service         ecommerce/order-service         Up
ecommerce-payment-service       ecommerce/payment-service       Up
ecommerce-notification-service  ecommerce/notification-service  Up
ecommerce-admin-service         ecommerce/admin-service         Up
ecommerce-frontend              ecommerce/frontend              Up
```

### Watch live logs

```powershell
# Stream logs from all containers
docker compose logs -f

# Stream logs from one specific service
docker compose logs -f config-server
docker compose logs -f api-gateway
docker compose logs -f customer-service
```

Press `Ctrl + C` to stop following logs (containers keep running).

### Container startup order (automatic)

Docker Compose enforces this order using health checks — you don't need to do anything manually:

```
MySQL ─────────────┐
Zookeeper ─────────┤──→ Kafka
                   │
                   └──→ config-server ──→ discovery-server ──→ api-gateway
                                                │
                             ┌──────────────────┼──────────────────────┐
                             ▼                  ▼                      ▼
                      customer-service    order-service          admin-service
                      product-service     payment-service
                                          notification-service
                                                │
                                           frontend (nginx)
```

---

## Step 12 — Verify Everything Works

Open these URLs in your browser **after Step 11 shows all containers as Up**:

| # | URL | What You Should See |
|---|-----|---------------------|
| 1 | **http://localhost:3000** | ✅ React frontend loads |
| 2 | **http://localhost:8080/swagger-ui.html** | ✅ Swagger UI — all 5 service APIs listed |
| 3 | **http://localhost:8761** | ✅ Eureka dashboard — all services registered |
| 4 | http://localhost:8080/actuator/health | ✅ `{"status":"UP"}` |
| 5 | http://localhost:8888/actuator/health | ✅ `{"status":"UP"}` |

### Quick API smoke test

Register a new customer through the API Gateway:

```powershell
curl -X POST http://localhost:8080/api/customers/register `
  -H "Content-Type: application/json" `
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","password":"Password1!"}'
```

✅ Expected response: `201 Created` with a customer object.

### Viewing containers in Docker Desktop

Open **Docker Desktop** → click **Containers** in the left sidebar.

You will see all 13 containers grouped under `ecommerce-microservices`. From the Docker Desktop UI you can:

| Action | How |
|---|---|
| View logs | Click a container → **Logs** tab |
| Inspect env vars | Click a container → **Inspect** tab |
| Start / Stop | Use the ▶ / ⏹ buttons |
| Monitor CPU + RAM | Click a container → **Stats** tab |
| Open terminal inside | Click a container → **Terminal** tab |

---

## Day-to-Day Commands

### Starting & stopping

```powershell
# Start all containers (no rebuild needed after first run)
docker compose up -d

# Stop all containers (your data in MySQL/Kafka is preserved)
docker compose down

# Stop all containers AND delete all data volumes (fresh slate)
docker compose down -v
```

### After making code changes

```powershell
# Rebuild and restart just the changed service
docker compose up -d --build customer-service

# Force rebuild without any cache (if dependencies changed)
docker compose build --no-cache customer-service
docker compose up -d customer-service
```

### Monitoring

```powershell
# See live CPU and memory usage of all containers
docker stats

# Check health status of all containers
docker compose ps

# See logs for a service (last 100 lines + follow)
docker compose logs -f --tail=100 order-service
```

### Accessing container internals

```powershell
# Open MySQL command line
docker exec -it ecommerce-mysql mysql -u root -p
# Enter password: root_password_change_me

# Open a shell inside any container
docker exec -it ecommerce-kafka bash
docker exec -it ecommerce-customer-service sh

# List Kafka topics
docker exec -it ecommerce-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Housekeeping

```powershell
# List all project images
docker images | findstr ecommerce

# Remove unused images to free disk space
docker image prune -f

# Full cleanup (removes stopped containers, unused images, networks)
docker system prune -f
```

---

## Service URLs & Ports

| Service | URL | Notes |
|---|---|---|
| **Frontend** | http://localhost:3000 | React app served by Nginx |
| **API Gateway** | http://localhost:8080 | Entry point for all API calls |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Aggregated docs for all services |
| **Eureka Dashboard** | http://localhost:8761 | Login: `eureka` / `eureka-secret` |
| **Config Server** | http://localhost:8888 | Login: `config-admin` / `config-secret` |
| Customer Service | http://localhost:8081 | Direct (bypass gateway) |
| Product Service | http://localhost:8082 | Direct (bypass gateway) |
| Order Service | http://localhost:8083 | Direct (bypass gateway) |
| Notification Service | http://localhost:8084 | Direct (bypass gateway) |
| Admin Service | http://localhost:8085 | Direct (bypass gateway) |
| Payment Service | http://localhost:8086 | Direct (bypass gateway) |
| MySQL | localhost:3306 | User: `root` / `root_password_change_me` |
| Kafka | localhost:9092 | Accessible from host machine |

---

## Troubleshooting

### Container exits right after starting

```
CAUSE:  A dependency service wasn't healthy yet
FIX:    Re-run docker compose up -d
        Health checks will retry automatically
```

### `Access denied for user 'ecommerce'`

```
CAUSE:  Old MySQL data volume has the user set to @'localhost' (pre-Docker)
FIX:    docker compose down -v
        docker compose up -d --build
```

### `Connection refused` to Kafka

```
CAUSE:  Kafka takes 30–60 seconds to become healthy after startup
FIX:    Wait 60 seconds, then run: docker compose ps
        If Kafka shows "Up (healthy)", the services will reconnect automatically
```

### Port already in use

```
CAUSE:  Another process on your host is using the same port
FIX:    Find the process:   netstat -aon | findstr :8080
        Kill it:            taskkill /PID <pid> /F

        Or change the host port in docker-compose.yml:
        ports:
          - "8090:8080"   ← change 8090 to any free port
```

### Images not rebuilding after code changes

```
FIX:    docker compose build --no-cache <service-name>
        docker compose up -d <service-name>
```

### CORS error in browser

```
CAUSE:  You are accessing the frontend at :5173 instead of :3000
FIX:    Always use http://localhost:3000 when running with Docker
        The Nginx container proxies /api/* to the gateway automatically
```

### Docker Desktop not starting

```
FIX 1:  Right-click system tray icon → Restart Docker Desktop
FIX 2:  Make sure WSL2 is running: wsl --status
FIX 3:  Restart WSL: wsl --shutdown, then reopen Docker Desktop
FIX 4:  Reinstall Docker Desktop if the issue persists
```

### Not enough memory — containers crashing

```
SYMPTOM: Containers keep restarting, OOMKilled errors in logs
FIX:     Docker Desktop Settings → Resources → Memory → increase to 8 GB+
         Then: Apply & Restart
```

---

> 🔒 **Security note for production:** Change `MYSQL_ROOT_PASSWORD` in `.env`, rotate the JWT RSA keys, and never commit `.env` to source control.
