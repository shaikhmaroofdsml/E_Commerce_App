# 🛒 Enterprise E-Commerce Microservices Platform

> **Java 21 · Spring Boot 3.4 · Spring Cloud · Apache Kafka · React.js · MySQL**
> 
> A production-grade, fully local microservices e-commerce platform built over 15 days.

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Service Inventory](#service-inventory)
3. [Prerequisites](#prerequisites)
4. [Quick Start](#quick-start)
5. [RSA Key Generation](#rsa-key-generation)
6. [Database Setup](#database-setup)
7. [Kafka Setup](#kafka-setup)
8. [API Reference](#api-reference)
9. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                 REACT FRONTEND (:5173)                  │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────────┐
│            API GATEWAY (:8080) - Spring Cloud           │
│      JWT Filter │ Circuit Breaker │ Load Balancer        │
└──┬────────┬───────────┬────────────┬──────────────┬─────┘
   │        │           │            │              │
 :8081    :8082       :8083        :8086          :8085
CUSTOMER PRODUCT    ORDER        PAYMENT        ADMIN

                        │ Kafka          │ Kafka
           ┌────────────┴────────────────┴──────────┐
           │          APACHE KAFKA (:9092)           │
           └──────────────────┬──────────────────────┘
                              │
                   ┌──────────▼───────────┐
                   │ NOTIFICATION SERVICE │
                   │       :8084          │
                   └──────────────────────┘

INFRASTRUCTURE:
  Config Server :8888    Discovery Server (Eureka) :8761
```

---

## Service Inventory

| Service | Port | Description |
|---------|------|-------------|
| config-server | 8888 | Centralized configuration (Spring Cloud Config) |
| discovery-server | 8761 | Service registry (Netflix Eureka) |
| api-gateway | 8080 | JWT auth, routing, circuit breaking |
| customer-service | 8081 | Registration, login, profiles, addresses |
| product-service | 8082 | Products, categories, inventory |
| order-service | 8083 | Cart, checkout, order management |
| notification-service | 8084 | Email/SMS via Kafka consumers |
| admin-service | 8085 | Dashboard, reports, Excel export |
| payment-service | 8086 | Payment simulation via Kafka |
| **frontend** | **5173** | React + Material UI |

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 21 (LTS) | [adoptium.net](https://adoptium.net) |
| Apache Maven | 3.9+ | [maven.apache.org](https://maven.apache.org) |
| MySQL | 8.0+ | [dev.mysql.com](https://dev.mysql.com/downloads/) |
| Apache Kafka | 3.7+ (KRaft) | [kafka.apache.org](https://kafka.apache.org/downloads) |
| Node.js | 20 LTS | [nodejs.org](https://nodejs.org) |
| Git | Latest | [git-scm.com](https://git-scm.com) |
| OpenSSL | Any | (for key generation) |

---

## Quick Start

### Step 1: Generate RSA Keys (ONE-TIME SETUP)

```powershell
# Option A — Using OpenSSL (recommended)
cd microservices\customer-service\src\main\resources\keys
openssl genrsa -out private.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private.pem -out private_pkcs8.pem
Move-Item private_pkcs8.pem private.pem
openssl rsa -in private.pem -pubout -out public.pem
Copy-Item public.pem ..\..\..\..\..\..\infrastructure\api-gateway\src\main\resources\keys\public.pem

# Option B — Java utility (no OpenSSL needed)
java scripts\GenerateRSAKeys.java
```

### Step 2: Database Setup

```sql
-- Run as MySQL root
SOURCE scripts/init-databases.sql
```

### Step 3: Kafka Setup (KRaft Mode)

```powershell
# From your Kafka install directory (e.g., C:\kafka)
.\bin\windows\kafka-storage.bat random-uuid        # Copy the UUID
.\bin\windows\kafka-storage.bat format -t <UUID> -c .\config\server.properties
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

### Step 4: Create Kafka Topics

```powershell
.\scripts\create-topics.bat
```

### Step 5: Add Hosts File Entries (as Administrator)

Edit `C:\Windows\System32\drivers\etc\hosts`:

```
127.0.0.1   config-server
127.0.0.1   discovery-server
127.0.0.1   kafka
127.0.0.1   mysql
```

### Step 6: Start All Services

```powershell
# Option A — Automated startup
.\scripts\start-all.ps1

# Option B — Manual (in order!)
# Terminal 1: Config Server
cd infrastructure\config-server
mvn spring-boot:run

# Terminal 2: Discovery Server (after config-server is UP)
cd infrastructure\discovery-server
mvn spring-boot:run

# Terminal 3: API Gateway
cd infrastructure\api-gateway
mvn spring-boot:run

# Terminals 4-9: Business services (any order)
cd microservices\customer-service && mvn spring-boot:run
cd microservices\product-service && mvn spring-boot:run
cd microservices\order-service && mvn spring-boot:run
cd microservices\payment-service && mvn spring-boot:run
cd microservices\notification-service && mvn spring-boot:run
cd microservices\admin-service && mvn spring-boot:run

# Terminal 10: Frontend
cd frontend
npm install
npm run dev
```

### Step 7: Verify

| URL | Expected |
|-----|----------|
| http://localhost:8761 | Eureka dashboard — all services UP |
| http://localhost:8888/actuator/health | `{"status":"UP"}` |
| http://localhost:8080/actuator/health | `{"status":"UP"}` |
| http://localhost:5173 | React app loads |

---

## API Reference

### Authentication

```
POST /api/customers/register    — Register (public)
POST /api/customers/login       — Login → returns JWT (public)
GET  /api/customers/profile     — Get profile (JWT required)
PUT  /api/customers/profile     — Update profile (JWT required)
POST /api/customers/addresses   — Add address (JWT required)
GET  /api/customers/addresses   — List addresses (JWT required)
```

### Products

```
GET    /api/products            — List with pagination (public)
GET    /api/products/{id}       — Get by ID (public)
POST   /api/products            — Create (ADMIN)
PUT    /api/products/{id}       — Update (ADMIN)
DELETE /api/products/{id}       — Soft delete (ADMIN)
GET    /api/categories          — List categories (public)
POST   /api/categories          — Create category (ADMIN)
```

### Orders

```
POST   /api/orders              — Place order (USER)
GET    /api/orders              — My orders (USER)
GET    /api/orders/{id}         — Order detail (USER)
PUT    /api/orders/{id}/cancel  — Cancel order (USER)
GET    /api/orders/admin/all    — All orders (ADMIN)
PUT    /api/orders/admin/{id}/status — Update status (ADMIN)
```

### Admin

```
GET /api/admin/dashboard        — Stats summary (ADMIN)
GET /api/admin/reports/sales    — Sales report (ADMIN)
GET /api/admin/reports/export   — Excel export (ADMIN)
```

---

## Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `Could not resolve placeholder 'spring.security.user.name'` | Config Server unreachable | Start config-server first, check hosts file |
| `Cannot execute request on any known server` | Eureka hostname unresolvable | Add `discovery-server` to hosts file |
| `No resolvable bootstrap urls given` | Kafka hostname wrong | Set `bootstrap-servers: localhost:9092` |
| `FileSystemException` in Kafka | Corrupted KRaft logs | Delete `C:\tmp\kraft-combined-logs` and re-format |
| `CORS error` in browser | Gateway origin not allowed | Gateway allows `http://localhost:5173` by default |
| Port already in use | Previous service running | Run `.\scripts\kill-ports.ps1` |
| `BeanCreationException on @Value` | Missing default value | Add `:defaultValue` to `@Value` annotation |

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready only |
| `develop` | Integration branch |
| `feature/xxx` | Feature development |
| `hotfix/xxx` | Critical fixes |

---

## License

This project is for educational/enterprise learning purposes.
