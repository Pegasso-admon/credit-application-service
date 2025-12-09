# CoopCredit - Credit Application System

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Security & Authentication](#security--authentication)
- [Testing](#testing)
- [Docker Deployment](#docker-deployment)
- [Observability](#observability)
- [Business Rules](#business-rules)

---

## 🎯 Overview

**CoopCredit** is an enterprise-grade credit application management system built with **Hexagonal Architecture** (Ports and Adapters pattern). The system manages the complete lifecycle of credit applications from submission to evaluation and decision.

### Key Features

✅ **Hexagonal Architecture** - Clean separation of concerns, framework-independent domain  
✅ **JWT Authentication** - Stateless security with role-based access control  
✅ **Microservices Integration** - External risk evaluation service  
✅ **Advanced JPA** - Optimized queries, EntityGraph, transaction management  
✅ **Comprehensive Testing** - Unit, integration, and container tests with Testcontainers  
✅ **API Documentation** - OpenAPI 3.0 (Swagger)  
✅ **Observability** - Actuator + Micrometer + Prometheus metrics  
✅ **Docker Ready** - Multi-stage Dockerfile and docker-compose  
✅ **Database Migrations** - Flyway for version control  

### Business Context

CoopCredit cooperative needed to modernize their manual credit process. This system:
- Eliminates spreadsheet-based management
- Provides consistent credit evaluation
- Ensures audit trail and traceability
- Enforces security and access control
- Enables distributed operations

---

## 🏛️ Architecture

### Hexagonal Architecture (Ports and Adapters)

```
┌─────────────────┐
│   Controllers   │  ◄── PRIMARY (Input) Adapters
│   (REST API)    │
└────────┬────────┘
         │
    ┌────▼──────┐
    │ Use Cases │  ◄── Application Layer
    └────┬──────┘
         │
    ┌────▼──────┐
    │  Domain   │  ◄── Business Logic (Pure)
    │  (Core)   │
    └────┬──────┘
         │
    ┌────▼──────┐
    │   Ports   │  ◄── Interfaces (Contracts)
    └────┬──────┘
         │
┌────────┴────────────┐
│  JPA  │  REST  │ JWT│  ◄── SECONDARY (Output) Adapters
└─────────────────────┘
```

**Key Principles:**
- **Dependency Inversion**: Domain depends on abstractions (ports), not implementations
- **Framework Independence**: Domain has no Spring/JPA annotations
- **Testability**: Each layer can be tested in isolation
- **Flexibility**: Easy to swap implementations (JPA → MongoDB, REST → GraphQL)

### Package Structure

```
com.coopcredit/
├── domain/
│   ├── model/              # Pure business entities (no annotations)
│   │   ├── Affiliate.java
│   │   ├── CreditApplication.java
│   │   ├── RiskEvaluation.java
│   │   ├── User.java
│   │   └── enums/
│   ├── repository/         # Repository ports (interfaces)
│   │   ├── AffiliateRepositoryPort.java
│   │   └── CreditApplicationRepositoryPort.java
│   └── service/            # Service ports (interfaces)
│       ├── RiskEvaluationPort.java
│       └── AuthPort.java
├── application/
│   ├── usecase/            # Business use cases
│   │   ├── RegisterAffiliateUseCase.java
│   │   ├── RegisterCreditApplicationUseCase.java
│   │   └── EvaluateCreditApplicationUseCase.java
│   └── dto/                # Data transfer objects
├── infrastructure/
│   ├── controller/         # REST controllers (input adapters)
│   ├── persistence/        # JPA adapters (output adapters)
│   │   ├── entity/
│   │   └── repository/
│   ├── client/             # REST client adapters
│   ├── config/             # Spring configurations
│   ├── exception/          # Global exception handling
│   └── mapper/             # MapStruct mappers
```

---

## 🛠️ Technologies

### Core Stack
- **Java 21** - Latest LTS with modern features
- **Spring Boot 3.2.0** - Application framework
- **Maven** - Dependency management

### Domain & Application
- **Lombok** - Boilerplate reduction
- **MapStruct** - Object mapping
- **Bean Validation** - Input validation

### Infrastructure
- **Spring Data JPA** - Persistence layer
- **PostgreSQL** - Production database
- **H2** - Development database
- **Flyway** - Database migrations
- **Spring Security + JWT** - Authentication/Authorization
- **SpringDoc OpenAPI** - API documentation

### Microservices
- **RestTemplate/WebClient** - Service communication
- **Jackson** - JSON serialization

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking
- **Testcontainers** - Integration testing
- **Spring Security Test** - Security testing

### Observability
- **Spring Boot Actuator** - Metrics and monitoring
- **Micrometer** - Metrics collection
- **Prometheus** - Metrics storage

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

---

## 📦 Prerequisites

- **JDK 21** or higher
- **Maven 3.8+**
- **Docker** (for containerized deployment)
- **PostgreSQL 15** (for production) or H2 (for development)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/credit-application-service.git
cd credit-application-service
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run with H2 (Development)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

### 4. Access Services

- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Actuator**: http://localhost:8080/actuator

---

## 📖 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "analyst1",
  "password": "password123",
  "email": "analyst@coopcredit.com",
  "role": "ROLE_ANALYST"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "analyst1",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "analyst1",
  "email": "analyst@coopcredit.com",
  "role": "ROLE_ANALYST",
  "expiresIn": 86400000
}
```

### Affiliate Endpoints

#### Register Affiliate
```http
POST /api/affiliates
Authorization: Bearer {token}
Content-Type: application/json

{
  "document": "1234567890",
  "name": "John Doe",
  "salary": 3000000.00,
  "affiliationDate": "2023-01-15"
}
```

#### Get Affiliate by Document
```http
GET /api/affiliates/document/1234567890
Authorization: Bearer {token}
```

### Credit Application Endpoints

#### Submit Application
```http
POST /api/applications
Authorization: Bearer {token}
Content-Type: application/json

{
  "affiliateId": 1,
  "requestedAmount": 5000000.00,
  "termMonths": 36,
  "interestRate": 12.5
}
```

#### Get Pending Applications (Analyst/Admin only)
```http
GET /api/applications/status/PENDING
Authorization: Bearer {token}
```

### Evaluation Endpoints

#### Evaluate Application (Analyst/Admin only)
```http
POST /api/evaluations/1
Authorization: Bearer {token}

Response:
{
  "applicationId": 1,
  "affiliateDocument": "1234567890",
  "affiliateName": "John Doe",
  "requestedAmount": 5000000.00,
  "termMonths": 36,
  "monthlyPayment": 166374.62,
  "status": "APPROVED",
  "approved": true,
  "decisionReason": "Approved - Risk level: MEDIUM, Score: 642...",
  "riskScore": 642,
  "riskLevel": "MEDIUM",
  "riskDetail": "Moderate credit history",
  "paymentToIncomeRatio": 0.0555,
  "evaluatedAt": "2024-12-09T10:30:00"
}
```

---

## 🔐 Security & Authentication

### JWT Configuration

The system uses **JWT (JSON Web Tokens)** for stateless authentication.

**Token Configuration** (application.yml):
```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-min-256-bits}
  expiration: ${JWT_EXPIRATION:86400000}  # 24 hours
```

### Role-Based Access Control

| Role | Permissions |
|------|-------------|
| **ROLE_AFFILIATE** | Submit applications, view own applications |
| **ROLE_ANALYST** | View pending applications, evaluate applications, register affiliates |
| **ROLE_ADMIN** | Full system access, user management, all CRUD operations |

### Protected Endpoints

Endpoints are protected using `@PreAuthorize` annotations:

```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> deleteAffiliate(@PathVariable Long id) {
    // Only admins can delete
}

@PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
public ResponseEntity<EvaluationResponse> evaluateApplication(@PathVariable Long id) {
    // Analysts and admins can evaluate
}
```

### Password Encoding

Passwords are hashed using **BCrypt** before storage:
```java
String hashedPassword = passwordEncoder.encode(plainPassword);
```

---

## 🧪 Testing

### Unit Tests

Test domain logic in isolation:

```bash
mvn test -Dtest=*UseCase*
```

Example:
```java
@Test
void shouldRejectHighRiskApplication() {
    // Given
    RiskEvaluationPort mockRiskPort = mock(RiskEvaluationPort.class);
    when(mockRiskPort.evaluateRisk(any(), any(), any()))
        .thenReturn(new RiskEvaluationResponse("123", 350, "HIGH", "Poor history"));
    
    // When
    EvaluationResult result = useCase.execute(applicationId);
    
    // Then
    assertFalse(result.approved());
    assertEquals("REJECTED", result.application().getStatus());
}
```

### Integration Tests

Test with real database using Testcontainers:

```bash
mvn verify
```

Example:
```java
@SpringBootTest
@Testcontainers
class CreditApplicationIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void shouldCreateAndEvaluateApplication() {
        // Test complete flow with real database
    }
}
```

### Test Coverage

```bash
mvn jacoco:report
```

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t coopcredit/credit-application-service:latest .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

**Services Started:**
- credit-application-service (port 8080)
- risk-central-mock-service (port 8081)
- postgres (port 5432)

### docker-compose.yml

```yaml
version: '3.8'

services:
  credit-application-service:
    image: coopcredit/credit-application-service:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://db:5432/coopcreditdb
      DATABASE_USERNAME: postgres
      DATABASE_PASSWORD: postgres
      RISK_CENTRAL_URL: http://risk-central-mock-service:8081
      JWT_SECRET: your-production-secret-key-min-256-bits
    depends_on:
      - db
      - risk-central-mock-service

  risk-central-mock-service:
    image: coopcredit/risk-central-mock-service:latest
    ports:
      - "8081:8081"

  db:
    image: postgres:15-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: coopcreditdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

---

## 📊 Observability

### Actuator Endpoints

Available at `/actuator`:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Key Metrics

- `http.server.requests` - Request count and duration
- `jvm.memory.used` - Memory usage
- `jdbc.connections.active` - Database connections
- `application_evaluated_applications_total` - Business metric

### Prometheus Configuration

Add to `prometheus.yml`:
```yaml
scrape_configs:
  - job_name: 'credit-application-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

---

## 💼 Business Rules

### Affiliate Eligibility

- ✅ Must be in ACTIVE status
- ✅ Minimum 6 months seniority required
- ✅ Document number must be unique
- ✅ Salary must be greater than zero

### Credit Application Rules

| Rule | Validation |
|------|------------|
| **Payment-to-Income Ratio** | Monthly payment ≤ 40% of salary |
| **Maximum Amount** | Requested amount ≤ 10x monthly salary |
| **Term Range** | 1-360 months |
| **Interest Rate** | 0-100% annual |

### Risk Evaluation

| Score Range | Risk Level | Decision |
|-------------|-----------|----------|
| 300-500 | HIGH | ❌ Auto-reject |
| 501-700 | MEDIUM | ⚠️ Requires perfect compliance |
| 701-950 | LOW | ✅ Approved if basics met |

### Evaluation Process

1. **Retrieve Application** (must be PENDING)
2. **Call Risk-Central Service** (external microservice)
3. **Create Risk Evaluation** (score + level)
4. **Apply Internal Policies**:
   - Check payment-to-income ratio
   - Check amount vs salary limit
   - Check minimum seniority
   - Check risk level
5. **Make Decision** (APPROVED/REJECTED with reason)
6. **Update Application** (transactionally)

---

## 📝 Database Schema

### Main Tables

```sql
-- Affiliates
CREATE TABLE affiliates (
    id BIGSERIAL PRIMARY KEY,
    document VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    salary DECIMAL(15,2) NOT NULL CHECK (salary > 0),
    affiliation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- Credit Applications
CREATE TABLE credit_applications (
    id BIGSERIAL PRIMARY KEY,
    affiliate_id BIGINT REFERENCES affiliates(id),
    requested_amount DECIMAL(15,2) NOT NULL,
    term_months INTEGER NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    application_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    decision_reason TEXT
);

-- Risk Evaluations
CREATE TABLE risk_evaluations (
    id BIGSERIAL PRIMARY KEY,
    credit_application_id BIGINT REFERENCES credit_applications(id),
    score INTEGER NOT NULL CHECK (score BETWEEN 300 AND 950),
    risk_level VARCHAR(20) NOT NULL,
    detail TEXT,
    evaluated_at TIMESTAMP NOT NULL,
    approved BOOLEAN NOT NULL
);
```

---

## 👥 Author

- Samuel Rosero Alvarez
- Clan: Berners Lee

## 📄 License

This project is licensed under the MIT License.

---

**Happy Coding! 🚀**