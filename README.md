# 🔗 Distributed URL Shortener & Analytics Service

> Production-ready, high-throughput URL shortening service built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **Redis Cache**, and **React**. Features Base62 short-code generation, custom alias reservation, TTL expiration, sub-millisecond Redis redirects, and real-time click analytics.

![Status: Development](https://img.shields.io/badge/Status-DEVELOPMENT-amber.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)
![Redis](https://img.shields.io/badge/Redis-7-red.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![React](https://img.shields.io/badge/React-18%2B-cyan.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)

---

## 📌 Project Overview

The **Distributed URL Shortener** transforms long, unwieldy URLs into compact, high-performance short links with sub-millisecond redirect latencies. The service implements an enterprise-grade **Cache-Aside pattern** utilizing **Redis** in front of a **PostgreSQL** relational database.

When a redirect request arrives at `GET /{shortCode}`:
1. The service checks Redis memory for the cached long URL.
2. On cache hit, the redirection executes immediately, reducing database read operations by up to 99%.
3. In parallel, click event metadata (IP address, User Agent, Referrer, Timestamp) is logged to the relational database for analytical tracking.
4. On cache miss, the target is resolved from PostgreSQL, warmed into Redis with an auto-expiring TTL, and redirected.

---

## 🚀 Key Features

- **Base62 Unique Encoding**: Generates collision-resistant 7-character alphanumeric short codes.
- **Custom Vanity Aliases**: Users can claim custom memorable aliases (e.g. `lnk.pro/springBootDocs`).
- **Configurable Expiration (TTL)**: Optional automatic expiration intervals (7, 30, 90, 365 days). Expired URLs return standard `410 GONE`.
- **Redis Cache-Aside Layer**: In-memory caching for frequently visited URLs with automatic invalidation.
- **HTTP 302 Redirection**: Sends proper `302 Found` redirection headers with original target destination.
- **Click Tracking & Analytics**: Collects metrics on total clicks, device/browser distribution, and referrer sources.
- **Interactive React UI**: Clean, developer-oriented interface with one-click copy, QR/live links, and metrics inspection.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Data JPA, Spring Data Redis, Hibernate |
| **Caching** | Redis 7 (In-Memory Key-Value Cache with TTL) |
| **Database** | PostgreSQL 16 (Primary Persistence), H2 (In-Memory Test/Development Profile) |
| **Encoding** | Custom Base62 Bijective Mapping & Secure Randomization |
| **Build Tool** | Apache Maven 3.9+, Lombok |
| **API Docs** | OpenAPI 3.0 / Swagger UI |
| **Testing** | JUnit 5, Mockito, Spring Boot Test |
| **Frontend** | React 18, Vite, Modern Dark Theme CSS, Lucide Icons |
| **Containerization** | Docker, Docker Compose (PostgreSQL + Redis + Spring Boot) |

---

## 🗄️ Relational Database Schema

```
+------------------------------------+
|            short_urls              |
+------------------------------------+
| id (PK, BIGINT)                    |
| long_url (VARCHAR 2048, NOT NULL)  |
| short_code (VARCHAR 32, UNIQUE)    |
| custom_alias (VARCHAR 64)          |
| click_count (BIGINT, DEFAULT 0)    |
| status (ACTIVE, EXPIRED, DISABLED) |
| created_at (TIMESTAMP)             |
| expires_at (TIMESTAMP)             |
+------------------------------------+
                  | 1
                  |
                  | *
+------------------------------------+
|        url_click_analytics         |
+------------------------------------+
| id (PK, BIGINT)                    |
| short_url_id (FK, NOT NULL)        |
| clicked_at (TIMESTAMP)             |
| ip_address (VARCHAR 64)            |
| user_agent (VARCHAR 255)           |
| referrer (VARCHAR 255)             |
+------------------------------------+
```

---

## 📡 REST API Endpoints

| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `POST` | `/api/urls` | Create shortened URL (supports custom alias & TTL) | `201 CREATED` |
| `GET` | `/{shortCode}` | Resolve and redirect to destination URL | `302 FOUND` |
| `GET` | `/api/urls/{shortCode}` | Retrieve short URL metadata | `200 OK` |
| `GET` | `/api/urls/{shortCode}/analytics` | Click count, timestamps, and access log | `200 OK` |
| `GET` | `/api/urls` | Paginated listing of recent URLs | `200 OK` |
| `DELETE`| `/api/urls/{shortCode}` | Deactivate URL (returns `404` upon subsequent access) | `204 NO CONTENT` |

---

## 💻 Local Setup & Execution

### 1. Backend Setup
```bash
cd backend

# Run automated tests
mvn test

# Run Spring Boot backend (port 8082)
mvn spring-boot:run
```

### 2. Frontend Setup
```bash
cd frontend

npm install
npm run dev
```
Access the client dashboard at `http://localhost:5173`.

---

## 🐳 Docker Deployment

The provided `docker-compose.yml` configures **PostgreSQL 16**, **Redis 7**, and the **Spring Boot application**:

```bash
docker-compose up --build
```
- Shortener API / Redirects: `http://localhost:8082`
- Redis Cache: port `6379`
- PostgreSQL: port `5434`

---

## 🧪 Automated Test Suite

Run JUnit 5 tests covering business logic, Redis cache coordination, and collision handling:

```bash
cd backend
mvn test
```

Tests verified:
- `testCreateShortUrl_Success`: Random Base62 code generation & Redis cache warm-up.
- `testCreateShortUrl_CustomAlias_Success`: Custom alias reservation.
- `testCreateShortUrl_CustomAlias_AlreadyExists`: Conflict detection on duplicate alias.
- `testResolveAndRecordClick_Success`: Valid redirect return & metric increments.
- `testResolveAndRecordClick_NotFound_ThrowsException`: Non-existent code handling (`404`).
- `testResolveAndRecordClick_Expired_ThrowsException`: Expiration timestamp enforcement (`410`).
- `testShortCode_UniquenessCheck`: Automatic retry on hash collisions.

---

## 📖 Swagger / OpenAPI Documentation
Interactive API docs available at:
```
http://localhost:8082/swagger-ui.html
```

---

## 👨‍💻 Author
**Praveenkumar T**  
- Portfolio: Coming Soon
- GitHub: [githubUrl: ""](Coming Soon)
- Live Demo: [liveUrl: ""](Coming Soon)
