# Enterprise E-Commerce Microservices Platform — Implementation Walkthrough

## ✅ Implementation Complete

The full platform has been scaffolded and implemented. Here's what was built and how to get it running.

---

## Architecture Overview

```
Browser (React + Vite)  ─→  API Gateway :8080  ─→  Microservices
                                   │
                          JWT RS256 Validation
                          Route → X-User-Id header
                                   │
              ┌─────────────────────┼──────────────────────────┐
              ↓                     ↓                          ↓
    customer-service:8081   product-service:8082   order-service:8083
                                                         ↓
                                              Kafka (order.created)
                                              ↙               ↘
                                 payment-service:8086   notification-service:8084
                                         ↓
                              Kafka (payment.completed / payment.failed)
                                    ↙             ↘
                         order-service           notification-service
                      (status → CONFIRMED)      (email stub logged)
```

---

## Port Map

| Service                | Port  |
|------------------------|-------|
| Config Server          | 8888  |
| Eureka Discovery       | 8761  |
| API Gateway            | 8080  |
| Customer Service       | 8081  |
| Product Service        | 8082  |
| Order Service          | 8083  |
| Notification Service   | 8084  |
| Admin Service          | 8085  |
| Payment Service        | 8086  |
| React Frontend (Vite)  | 5173  |

---

## Startup Guide (Step-by-Step)

### Prerequisites
- Java 21 (check: `java -version`)
- Maven 3.9+ (check: `mvn -version`)
- MySQL 8.0 running on port 3306
- Apache Kafka running in KRaft mode on port 9092
- Node.js 18+ (check: `node -version`)

### Step 1 — Initialize MySQL Databases
```powershell
mysql -u root -p < scripts\init-databases.sql
```
This creates 6 databases (`ecommerce_customers`, `ecommerce_products`, etc.) and the `ecommerce` user.

### Step 2 — Generate RSA Keys (ONE-TIME ONLY)
```powershell
java scripts\GenerateRSAKeys.java
```
Writes:
- `microservices/customer-service/src/main/resources/keys/private.pem`
- `infrastructure/api-gateway/src/main/resources/keys/public.pem`

### Step 3 — Create Kafka Topics
```powershell
scripts\create-topics.bat
```
Creates: `order.created`, `payment.completed`, `payment.failed` + DLT variants.

### Step 4 — Start All Services (Ordered)
**Option A — Automated:**
```powershell
powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1
```

**Option B — Manual (in order):**
```powershell
# Terminal 1 — Config Server (start first, wait 15s)
cd infrastructure\config-server && mvn spring-boot:run

# Terminal 2 — Eureka (wait for config server, then wait 10s)
cd infrastructure\discovery-server && mvn spring-boot:run

# Terminal 3 — API Gateway
cd infrastructure\api-gateway && mvn spring-boot:run

# Terminals 4-9 — Business services (all in parallel)
cd microservices\customer-service && mvn spring-boot:run
cd microservices\product-service  && mvn spring-boot:run
cd microservices\order-service    && mvn spring-boot:run
cd microservices\payment-service  && mvn spring-boot:run
cd microservices\notification-service && mvn spring-boot:run
cd microservices\admin-service    && mvn spring-boot:run
```

### Step 5 — Start Frontend
```powershell
cd frontend
npm install
npm run dev
```
Open: **http://localhost:5173**

---

## Key API Endpoints (via Gateway at :8080)

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/customers/register` | Register new user |
| POST | `/api/customers/login` | Login → returns JWT |
| GET  | `/api/customers/profile` | Get profile (JWT required) |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products?search=&categoryId=&page=0&size=12` | Search + paginate |
| GET | `/api/products/{id}` | Get single product |
| POST | `/api/products` | Create product (Admin) |
| PUT  | `/api/products/{id}` | Update product (Admin) |
| DELETE | `/api/products/{id}` | Soft delete (Admin) |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Place order → publishes to Kafka |
| GET  | `/api/orders` | My order history |
| GET  | `/api/orders/{id}` | Order detail |
| PUT  | `/api/orders/{id}/cancel` | Cancel order |
| GET  | `/api/orders/admin/all` | All orders (Admin) |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard` | Stats (Admin) |
| GET | `/api/admin/reports/sales?from=&to=` | Sales report |
| GET | `/api/admin/reports/export` | Download Excel |

---

## Kafka Event Flow

```
order-service       ──[order.created]──→  payment-service
                                          └─ processes payment (90% success sim)
                                          └─ publishes [payment.completed] or [payment.failed]

order-service       ←─[payment.completed]── payment-service
                        (status → CONFIRMED)

notification-service ←─[order.created]────── order-service
                     ←─[payment.completed]── payment-service
                     ←─[payment.failed]───── payment-service
                        (emails logged to console in stub mode)
```

**All topics have DLT (Dead Letter Topic)** with 3 retry attempts at 1s intervals.

---

## Security Model

- **JWT RS256** (asymmetric): customer-service signs with **private key**, api-gateway verifies with **public key**
- **Token claims**: `sub` (customerId), `role` (ROLE_USER / ROLE_ADMIN), `email`, `firstName`
- **Gateway** validates JWT → extracts `X-User-Id` and `X-User-Role` headers → forwards to downstream
- **Downstream services** trust these headers (no re-validation needed)
- **BCrypt strength 12** for password hashing

---

## Frontend Pages

| Route | Page | Auth |
|-------|------|------|
| `/` | Home (hero, featured products) | Public |
| `/products` | Product listing (search, filter, paginate) | Public |
| `/products/:id` | Product detail + add to cart | Public |
| `/cart` | Cart + checkout (calls order API) | Public |
| `/login` | Login | Public |
| `/register` | Registration | Public |
| `/orders` | My order history | Auth |
| `/orders/:id` | Order detail + cancel | Auth |
| `/admin` | Admin dashboard + Excel export | Admin only |

---

## Environment Variables (Optional Overrides)

```bash
# Notification Service — Real email sending
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_STUB_MODE=false

# Payment Service — Adjust success rate
payment.simulation.success-rate=0.9   # (in config YAML)
```

---

## Dashboards & Monitoring

| URL | Description |
|-----|-------------|
| http://localhost:8761 | Eureka — registered services |
| http://localhost:8888/customer-service/default | Config Server — view configs |
| http://localhost:8080/actuator/health | API Gateway health |
| http://localhost:5173 | React Frontend |

---

## Clean Restart

```powershell
# Kill all service ports
powershell -ExecutionPolicy Bypass -File scripts\kill-ports.ps1

# Then restart services as normal
```
