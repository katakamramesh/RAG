# RAG Chat Storage Microservice

A production-ready backend microservice to store and manage chat histories for Retrieval-Augmented Generation (RAG) AI chatbots. Supports session management, LLM integration, and Postgres persistence.

---

## Features

- ✅ Chat session and message storage in **PostgreSQL**
- ✅ Session management: rename, mark as favorite, delete
- ✅ Save messages with sender, content, optional context
- ✅ Retrieve messages with **pagination**
- ✅ LLM Integration for chat sessions (OpenAI GPT)
- ✅ API key authentication (supports **multiple keys** via environment variables)
- ✅ Rate limiting per API key
- ✅ Centralized logging to files
- ✅ Standardized error codes
- ✅ DTO ↔ Model mapping via **Auto-Mapper**
- ✅ Health check endpoint
- ✅ CORS enabled
- ✅ Swagger/OpenAPI documentation
- ✅ Dockerized setup:
    - Spring Boot app
    - PostgreSQL database
    - pgAdmin for DB browsing

---

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- Java 17+
- Maven 3.8+
- OpenAI API Key (for LLM integration)

---

## Setup Instructions

1. **Clone the repository:**

```bash
git clone https://github.com/katakamramesh/RAG.git
cd RAG
```

2. **Copy environment variables template:**

```bash
cp .env.example .env
```

Update `.env` with your configuration:

```env
# Server
SERVER_PORT=8080

# Database
POSTGRES_USER=rag_user
POSTGRES_PASSWORD=rag_pass
POSTGRES_DB=rag_db
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

# LLM
OPENAI_API_KEY=your_openai_api_key

# API Keys (comma-separated)
API_KEYS=key1,key2

# Rate limiting
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_DURATION_MINUTES=1

# Logging
LOG_FILE_PATH=logs/application.log
```

---

3. **Start services using Docker Compose:**

```bash
docker-compose up --build -d
```

Services included:

| Service       | Port | Description                         |
|---------------|------|-------------------------------------|
| app           | 8080 | Spring Boot API                     |
| postgres      | 5432 | PostgreSQL database                 |
| pgadmin       | 8081 | DB management UI (user/pass below) |

- **pgAdmin login:**
    - Email: `admin@rag.local`
    - Password: `admin`
    - Add server connection: Host `postgres`, DB `rag_db`, User `rag_user`, Password `rag_pass`

---

## API Endpoints

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/sessions` | POST | Create new chat session | API Key |
| `/api/sessions/{id}` | GET | Get session details | API Key |
| `/api/sessions/{id}` | PUT | Rename session | API Key |
| `/api/sessions/{id}/favorite` | POST/DELETE | Mark/unmark favorite | API Key |
| `/api/sessions/{id}` | DELETE | Delete session and messages | API Key |
| `/api/sessions/{id}/messages` | POST | Add message to session | API Key |
| `/api/sessions/{id}/messages` | GET | Retrieve messages (supports pagination) | API Key |
| `/api/health` | GET | Health check | No Auth |
| `/swagger-ui.html` | GET | Swagger API documentation | No Auth |

---

## Logging

- Application logs are written to the path defined in `.env` (`LOG_FILE_PATH`)
- Error handling returns standardized error codes for:

| Scenario | HTTP Code |
|----------|-----------|
| Unauthorized | 401 |
| Forbidden | 403 |
| Not Found | 404 |
| Validation Error | 400 |
| Server Error | 500 |

---

## Notes / Pending Items

- ✅ Postgres database fully integrated with Dockerized pgAdmin
- ✅ LLM integration implemented
- ⚠️ Unit tests for services/business logic still need to be written
- ⚠️ Complete multiple API key verification across all endpoints
- ⚠️ Confirm pagination is applied to all message retrieval endpoints

---

## Running Locally (Optional)

1. Build and run using Maven:

```bash
mvn clean install
mvn spring-boot:run
```

2. Verify API is running at `http://localhost:8080`

