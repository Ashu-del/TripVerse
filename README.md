# TripVerse – Microservices-based Travel Booking Platform

TripVerse is a backend-focused microservices project inspired by real-world travel platforms
like MakeMyTrip. The project demonstrates production-style backend architecture,
service orchestration, and distributed system design.

---

## Tech Stack
- Java 17
- Spring Boot
- Spring Cloud Gateway
- Eureka Service Discovery
- OpenFeign
- JWT Authentication
- PostgreSQL
- Maven

---

## Microservices Overview

| Service | Responsibility |
|------|---------------|
| Eureka Server | Service discovery |
| API Gateway | Routing, JWT authentication, correlation ID |
| Auth Service | User authentication & JWT issuance |
| Inventory Service | Destination & seat management |
| Booking Service | Booking orchestration (Saga pattern) |
| Payment Service | Payment simulation & callbacks |

---

## System Architecture

- Centralized authentication at API Gateway
- Stateless JWT-based security
- Service-to-service communication via OpenFeign
- Internal APIs protected using internal service tokens
- Booking service acts as **orchestrator** using Saga pattern

---

## Booking Flow (High Level)

1. User logs in via API Gateway
2. JWT is validated at Gateway
3. User creates booking
4. Booking Service:
   - Reserves seats via Inventory Service
   - Initiates payment via Payment Service
   - Confirms booking on payment success
   - Releases seats on failure (compensation)

---

## Key Design Concepts Used

- API Gateway Pattern
- Saga (Orchestration-based)
- Service Discovery
- Idempotent APIs
- Distributed transaction handling
- Separation of public vs internal APIs

---

## How to Run Locally

### Prerequisites
- Java 17
- Maven
- PostgreSQL
- Git

### Start Services (in order)

1. Eureka Server
2. Auth Service
3. Inventory Service
4. Payment Service
5. Booking Service
6. API Gateway


---

## Sample API Flow (Postman)

1. **Login**
POST /auth/login

2. **Create Destination**
POST /inventory/destinations

3. **Create Booking**
POST /bookings

4. **Payment Callback**
POST /internal/bookings/{ref}/payment-success

---

## Future Improvements
- Docker & Docker Compose
- Circuit Breakers (Resilience4j)
- Centralized logging
- Notification Service
- Kubernetes deployment

---

## Author
Ashutosh Pandey  
Backend Developer | Java | Spring Boot | Microservices
