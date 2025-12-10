# CoopCredit - Credit Application System

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#-overview)
- [Quick Start](#-quick-start)
- [Architecture](#️-architecture)
- [Test Users](#-test-users)
- [API Endpoints](#-api-endpoints)
- [cURL Examples](#-curl-examples)
- [Security & Authentication](#-security--authentication)
- [Business Rules](#-business-rules)
- [Technologies](#️-technologies)
- [Testing](#-testing)
- [Docker](#-docker)
- [Postman Collection](#-postman-collection)

---

## 🎯 Overview

**CoopCredit** is an enterprise-grade credit application management system built with **Hexagonal Architecture** (Ports and Adapters pattern). The system manages the complete lifecycle of credit applications from submission to evaluation and decision.

### Key Features

| Feature | Description |
|---------|-------------|
| 🏛️ **Hexagonal Architecture** | Pure domain with no framework dependencies |
| 🔐 **JWT + Roles** | Stateless authentication with role-based access control |
| 📊 **Risk Evaluation** | External service integration for credit scoring |
| ✅ **Robust Validations** | Business rules encapsulated in domain layer |
| 📝 **Documented API** | OpenAPI/Swagger available |

### What It Does

- Register affiliates with data validation
- Create and manage credit applications
- Automatically evaluate applications using external risk service
- Apply credit policies (payment-to-income ratio, seniority, risk level)
- Control access based on roles (Affiliate, Analyst, Admin)

---

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+

### Run Locally (H2 Database)

```bash
# Clone repository
git clone <repository-url>
cd credit-application-service

# Build
mvn clean install -DskipTests

# Run with local profile (H2 in-memory)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Application URLs

| Service | URL |
|---------|-----|
| API Base | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| Health Check | http://localhost:8080/actuator/health |

---

## 👥 Test Users

The system comes with pre-loaded users for testing:

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| `admin` | `password123` | ADMIN | Full access: CRUD affiliates, applications, evaluations |
| `analyst1` | `password123` | ANALYST | View affiliates, evaluate pending applications |
| `affiliate1` | `password123` | AFFILIATE | Create own applications, view status |

### Login Example

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "expiresIn": 86400000
}
```

---

## 🏛️ Architecture

### Hexagonal Architecture (Ports and Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                     PRIMARY ADAPTERS                         │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │ REST Controllers │  │ Frontend (HTML) │                   │
│  └────────┬────────┘  └────────┬────────┘                   │
└───────────┼─────────────────────┼───────────────────────────┘
            │                     │
            ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                USE CASES                             │    │
│  │  • EvaluateCreditApplicationUseCase                  │    │
│  │  • RegisterAffiliateUseCase                          │    │
│  │  • CreateCreditApplicationUseCase                    │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN (Core)                          │
│  ┌─────────────────┐  ┌─────────────────────────────────┐   │
│  │     MODELS      │  │            PORTS                │   │
│  │  • Affiliate    │  │  • AffiliateRepositoryPort      │   │
│  │  • CreditApp    │  │  • CreditApplicationRepoPort    │   │
│  │  • RiskEval     │  │  • RiskEvaluationPort           │   │
│  └─────────────────┘  └─────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   SECONDARY ADAPTERS                         │
│  ┌──────────────┐  ┌───────────────┐  ┌─────────────────┐   │
│  │ JPA/Hibernate │  │  REST Client  │  │  Security/JWT   │   │
│  │ (PostgreSQL)  │  │ (Risk Central)│  │     (Auth)      │   │
│  └──────────────┘  └───────────────┘  └─────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.coopcredit/
├── domain/                    # Pure domain (no framework annotations)
│   ├── model/                 # Entities: Affiliate, CreditApplication, RiskEvaluation
│   ├── repository/            # Ports: AffiliateRepositoryPort, CreditApplicationRepositoryPort
│   └── service/               # Ports: RiskEvaluationPort
├── application/
│   └── usecase/               # Use cases: EvaluateCreditApplicationUseCase
├── infrastructure/
│   ├── controller/            # REST Adapters (Input)
│   ├── persistence/           # JPA Adapters (Output)
│   │   ├── entity/            # JPA Entities
│   │   ├── repository/        # JPA Repositories
│   │   └── adapter/           # Port implementations
│   ├── client/                # REST Client Adapters
│   ├── config/                # Spring Configuration
│   │   └── security/          # JWT, Security Config
│   ├── mapper/                # MapStruct Mappers
│   └── exception/             # Global Exception Handling
```

---

## 📚 API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/login` | Get JWT token | Public |
| POST | `/api/auth/register` | Register user | Public |

### Affiliates

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/v1/affiliates` | List all affiliates | ADMIN, ANALYST |
| GET | `/api/v1/affiliates/{id}` | Get affiliate by ID | ADMIN, ANALYST |
| POST | `/api/v1/affiliates` | Create affiliate | ADMIN |
| PUT | `/api/v1/affiliates/{id}` | Update affiliate | ADMIN |
| DELETE | `/api/v1/affiliates/{id}` | Delete affiliate | ADMIN |

### Credit Applications

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/credit-applications` | Create application | ADMIN, ANALYST, AFFILIATE |
| GET | `/api/v1/credit-applications/pending` | List pending | ADMIN, ANALYST |
| GET | `/api/v1/credit-applications/{id}` | Get by ID | ADMIN, ANALYST |

### Evaluation

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/evaluations/{applicationId}` | Evaluate application | ADMIN, ANALYST |

---

## 💻 cURL Examples

### 1. Authentication

```bash
# Login and save token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' | \
  grep -o '"token" *: *"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"
```

### 2. List Affiliates

```bash
curl -X GET http://localhost:8080/api/v1/affiliates \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 3. Create Affiliate

```bash
curl -X POST http://localhost:8080/api/v1/affiliates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "document": "12345678",
    "name": "John Smith",
    "salary": 5000000,
    "affiliationDate": "2024-01-15"
  }' | jq
```

### 4. Create Credit Application

```bash
curl -X POST http://localhost:8080/api/v1/credit-applications \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "requestedAmount": 10000000,
    "termMonths": 24
  }' | jq
```

### 5. Get Pending Applications

```bash
curl -X GET http://localhost:8080/api/v1/credit-applications/pending \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 6. Evaluate Application

```bash
curl -X POST http://localhost:8080/api/evaluations/1 \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Successful Evaluation Response:**
```json
{
  "applicationId": 1,
  "affiliateDocument": "1017654321",
  "affiliateName": "Juan Perez",
  "requestedAmount": 15000000.00,
  "termMonths": 36,
  "monthlyPayment": 501807.26,
  "status": "APPROVED",
  "approved": true,
  "decisionReason": "Approved - Risk level: LOW, Score: 946, Payment ratio: 14.34%",
  "riskScore": 946,
  "riskLevel": "LOW",
  "paymentToIncomeRatio": 0.1434,
  "evaluatedAt": "2025-12-09T21:16:39"
}
```

---

## 🔐 Security & Authentication

### JWT (JSON Web Token)

- **Algorithm**: HS256
- **Expiration**: 24 hours
- **Required Header**: `Authorization: Bearer <token>`

### Roles and Permissions

```
┌─────────────────────────────────────────────────────────────┐
│                         ROLE_ADMIN                           │
│  ✅ Create/edit/delete affiliates                            │
│  ✅ Create credit applications                               │
│  ✅ Evaluate applications                                    │
│  ✅ Manage users                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                        ROLE_ANALYST                          │
│  ✅ View affiliates                                          │
│  ✅ View pending applications                                │
│  ✅ Evaluate applications                                    │
│  ❌ Create/delete affiliates                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       ROLE_AFFILIATE                         │
│  ✅ Create own applications                                  │
│  ✅ View own application status                              │
│  ❌ View other affiliates                                    │
│  ❌ Evaluate applications                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 💼 Business Rules

### Affiliate Eligibility

| Rule | Validation |
|------|------------|
| Status | Must be **ACTIVE** |
| Seniority | Minimum **6 months** since affiliation |
| Document | Unique in system |
| Salary | Greater than zero |

### Credit Evaluation

| Criteria | Rule | Result |
|----------|------|--------|
| **Payment-to-Income Ratio** | Monthly payment ≤ 40% of salary | Rejected if exceeded |
| **Maximum Amount** | Requested amount ≤ 10x monthly salary | Rejected if exceeded |
| **HIGH Risk** (score 300-500) | Any case | ❌ Auto-rejected |
| **MEDIUM Risk** (score 501-700) | Only if 100% policy compliance | ⚠️ Case-by-case |
| **LOW Risk** (score 701-950) | If basic requirements met | ✅ Approved |

### Monthly Payment Formula

```
Payment = P × [r(1+r)^n] / [(1+r)^n - 1]

Where:
  P = Loan amount
  r = Monthly interest rate (annual / 12)
  n = Number of payments (months)
```

---

## 🛠️ Technologies

| Category | Technology |
|----------|------------|
| **Core** | Java 21, Spring Boot 3.2.0, Maven |
| **Persistence** | Spring Data JPA, Hibernate, PostgreSQL/H2 |
| **Security** | Spring Security, JWT (jjwt) |
| **Migrations** | Flyway |
| **Mapping** | MapStruct, Lombok |
| **Documentation** | SpringDoc OpenAPI (Swagger) |
| **Testing** | JUnit 5, Mockito, Testcontainers |
| **Observability** | Spring Actuator, Micrometer |

---

## 🧪 Testing

### Run Tests

```bash
# All tests
mvn test

# Unit tests only (no Docker required)
mvn test -Dtest=*UseCase*,*Mapper*

# With coverage report (JaCoCo)
mvn test jacoco:report
# View report: target/site/jacoco/index.html
```

### Test Structure

```
src/test/java/com/coopcredit/
├── application/usecase/
│   └── EvaluateCreditApplicationUseCaseTest.java  # Unit tests
├── infrastructure/
│   ├── mapper/                                     # Mapper tests
│   └── persistence/adapter/                        # Integration tests
└── AbstractIntegrationTest.java                    # Testcontainers base
```

---

## 🐳 Docker

### Build Image

```bash
docker build -t coopcredit/credit-application-service:latest .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

**Services started:**
- `credit-application-service` → port 8080
- `postgres` → port 5432
- `risk-central-mock` → port 8081 (simulated risk service)

---

## 📮 Postman Collection

The project includes a Postman collection at: `postman_collection.json`

### Import to Postman

1. Open Postman
2. File → Import
3. Select `postman_collection.json`
4. Set variable `baseUrl` = `http://localhost:8080/api`

### Test Flow

1. **Login** → Get token
2. **Set token** → Configure `token` variable
3. **Create Affiliate** → Register affiliate
4. **Create Application** → Submit credit request
5. **Get Pending** → View pending applications
6. **Evaluate** → Process application

---

## 📁 Project Structure

```
credit-application-service/
├── src/
│   ├── main/
│   │   ├── java/com/coopcredit/
│   │   │   ├── domain/          # Pure business logic
│   │   │   ├── application/     # Use cases
│   │   │   └── infrastructure/  # Adapters (REST, JPA, JWT)
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── db/migration/    # Flyway scripts
│   └── test/                    # Unit & integration tests
├── frontend/                    # Simple HTML/JS/CSS frontend
├── postman_collection.json      # Postman collection
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## 👤 Author

**Samuel Rosero Alvarez**  
Clan: Berners Lee

---

## 📄 License

This project is licensed under the MIT License.

---

**Happy Coding! 🚀**