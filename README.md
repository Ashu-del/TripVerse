<div align="center">

# ✈️ TripVerse
### A Production-Grade Microservices Travel Booking Platform

*Distributed systems, done the way real banking-grade backends are built.*

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Gateway%20%7C%20Eureka-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/Auth-JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

[Architecture](#-system-architecture) •
[Services](#-microservices-breakdown) •
[Booking Flow](#-the-booking-saga-in-action) •
[Run Locally](#-getting-started) •
[Roadmap](#-roadmap)

</div>

---

## 🧭 Why TripVerse Exists

Most portfolio projects are CRUD apps with a login page bolted on. **TripVerse isn't that.**

It's a simulation of the hardest part of backend engineering: what happens when a single business action — *booking a trip* — has to succeed or fail **atomically across five independent services that don't share a database.**

Built around a real-world scenario inspired by platforms like MakeMyTrip, TripVerse exists to answer one question I kept getting asked in interviews:

> *"Have you actually built something with distributed transactions, or just read about the Saga pattern?"*

This is my answer.

---

## 🏗️ System Architecture

```mermaid
flowchart TB
    Client([Client / Postman])

    subgraph Edge["Edge Layer"]
        GW["API Gateway<br/>JWT validation • Routing • Correlation ID"]
    end

    subgraph Discovery["Service Discovery"]
        Eureka["Eureka Server"]
    end

    subgraph Core["Core Domain Services"]
        Auth["Auth Service<br/>Login • JWT issuance"]
        Inventory["Inventory Service<br/>Destinations • Seat inventory"]
        Booking["Booking Service<br/>🎭 Saga Orchestrator"]
        Payment["Payment Service<br/>Payment simulation • Callbacks"]
    end

    DB[(PostgreSQL<br/>per service)]

    Client --> GW
    GW -->|authenticate| Auth
    GW -->|routes via| Eureka
    Booking -.->|register/discover| Eureka
    Auth -.-> Eureka
    Inventory -.-> Eureka
    Payment -.-> Eureka

    GW --> Booking
    Booking -->|1. reserve seat<br/>Feign| Inventory
    Booking -->|2. initiate payment<br/>Feign| Payment
    Payment -.->|3. async callback| Booking
    Booking -->|4. compensate on failure| Inventory

    Auth --> DB
    Inventory --> DB
    Booking --> DB
    Payment --> DB

    style Booking fill:#6DB33F,color:#fff
    style GW fill:#ED8B00,color:#fff
    style Eureka fill:#333,color:#fff
```

**Design principles enforced throughout:**
- 🔐 **Stateless security** — JWT validated once at the Gateway, propagated via internal service tokens
- 🧩 **Zero shared database** — every service owns its data; no service reaches into another's tables
- 🎭 **Orchestration over choreography** — Booking Service explicitly drives the transaction instead of relying on implicit event chains, so the failure path is always traceable
- 🔁 **Idempotent APIs** — safe retries on network blips, a non-negotiable in payment flows

---

## 🔍 Microservices Breakdown

| Service | Responsibility | Talks To |
|---|---|---|
| 🧭 **Eureka Server** | Central service registry & discovery | All services |
| 🚪 **API Gateway** | Routing, JWT auth, correlation-ID injection | Auth, Booking |
| 🔑 **Auth Service** | User authentication & JWT issuance | — |
| 📦 **Inventory Service** | Destination catalog & seat availability | Booking |
| 🎭 **Booking Service** | **Saga orchestrator** — drives the entire booking lifecycle | Inventory, Payment |
| 💳 **Payment Service** | Payment simulation & async callbacks | Booking |

---

## 🎭 The Booking Saga in Action

This is the centerpiece of the project — a real orchestration-based Saga with a **compensating transaction** on failure.

```mermaid
sequenceDiagram
    actor U as User
    participant GW as API Gateway
    participant B as Booking Service
    participant I as Inventory Service
    participant P as Payment Service

    U->>GW: POST /bookings (JWT)
    GW->>GW: Validate JWT
    GW->>B: Forward request
    B->>I: Reserve seat (Feign)
    I-->>B: Seat reserved ✅
    B->>P: Initiate payment
    P-->>B: Payment pending

    alt Payment succeeds
        P->>B: Callback: payment-success
        B->>B: Confirm booking
        B-->>U: 200 Booking Confirmed 🎉
    else Payment fails
        P->>B: Callback: payment-failed
        B->>I: Release seat (compensation)
        I-->>B: Seat released
        B-->>U: 400 Booking Failed, seat freed
    end
```

> 💡 **Why this matters:** In a monolith, this is one `@Transactional` annotation. Across five independently-deployable services with separate databases, there's no distributed rollback to lean on — the compensating action (releasing the seat) has to be **explicitly coded and idempotent**. That's the actual engineering problem the Saga pattern solves, and it's implemented end-to-end here.

---

## 🛠️ Tech Stack

<table>
<tr>
<td valign="top" width="33%">

**Core**
- Java 17
- Spring Boot 3
- Maven

</td>
<td valign="top" width="33%">

**Distributed Systems**
- Spring Cloud Gateway
- Netflix Eureka
- OpenFeign

</td>
<td valign="top" width="33%">

**Security & Data**
- JWT Authentication
- PostgreSQL
- Internal service tokens

</td>
</tr>
</table>

---

## 📡 Sample API Flow

```http
### 1. Authenticate
POST /auth/login
Content-Type: application/json

{ "username": "traveler01", "password": "••••••" }

### 2. Browse inventory
POST /inventory/destinations
Authorization: Bearer <JWT>

### 3. Create a booking (triggers the Saga)
POST /bookings
Authorization: Bearer <JWT>

### 4. Payment gateway callback (internal)
POST /internal/bookings/{ref}/payment-success
```

---

## 🚀 Getting Started

### Prerequisites
`Java 17` · `Maven` · `PostgreSQL` · `Git`

### Run the services — order matters (service discovery bootstraps first)

```bash
# 1. Clone
git clone https://github.com/Ashu-del/TripVerse.git
cd TripVerse

# 2. Start in this exact order
cd discovery-server && mvn spring-boot:run      # 1️⃣ Eureka Server
cd auth-service      && mvn spring-boot:run     # 2️⃣ Auth Service
cd inventory-service  && mvn spring-boot:run    # 3️⃣ Inventory Service
cd payment-service    && mvn spring-boot:run    # 4️⃣ Payment Service
cd booking-service    && mvn spring-boot:run    # 5️⃣ Booking Service (orchestrator)
cd api-gateway         && mvn spring-boot:run   # 6️⃣ API Gateway
```

Once all six services register with Eureka, hit the Gateway on its configured port and follow the [Sample API Flow](#-sample-api-flow) above.

---

## 📂 Project Structure

```
TripVerse/
├── discovery-server/     # Eureka service registry
├── api-gateway/          # Routing + JWT validation + correlation IDs
├── auth-service/         # Authentication & JWT issuance
├── inventory-service/    # Destinations & seat management
├── booking-service/      # 🎭 Saga orchestrator — the heart of the system
└── payment-service/      # Payment simulation & callbacks
```

---

## 🧠 Key Design Concepts Demonstrated

| Concept | Where It Lives |
|---|---|
| API Gateway Pattern | `api-gateway/` |
| Saga Pattern (Orchestration-based) | `booking-service/` |
| Service Discovery | `discovery-server/` + Eureka clients |
| Idempotent APIs | Payment callback handlers |
| Compensating Transactions | Booking failure → seat release |
| Public vs. Internal API separation | Gateway routing rules |

---

## 🗺️ Roadmap

- [ ] Docker & Docker Compose for one-command spin-up
- [ ] Circuit breakers via Resilience4j on inter-service calls
- [ ] Centralized logging (ELK / correlation-ID tracing)
- [ ] Notification Service (email/SMS on booking confirmation)
- [ ] Kubernetes deployment manifests

---

## 👤 Author

**Ashutosh Pandey**
Backend Developer · Java · Spring Boot · Microservices

Building distributed systems that mirror real production banking and travel platforms — not tutorial-grade demos.

[![GitHub](https://img.shields.io/badge/GitHub-Ashu--del-181717?style=flat-square&logo=github)](https://github.com/Ashu-del)

---

<div align="center">

*If this project helped you understand the Saga pattern or distributed booking flows, consider ⭐ starring the repo.*

</div>
