# RAG Chat Storage Service

A secure, production-ready microservice to store chat histories for Retrieval-Augmented Generation (RAG) based AI chatbots.

## Features

- Chat session and message storage (MongoDB)
- Session rename, favorite, delete
- Add and retrieve chat messages (with pagination)
- API key authentication (`X-API-KEY` header)
- Centralized logging and error handling
- CORS configuration
- Health check endpoint `/api/health`
- Dockerized (app, MongoDB, Mongo Express)
- Swagger UI at `/swagger-ui.html`

## Setup

1. Copy `.env.example` to `.env` and set your secrets.
2. Build and run with Docker Compose:
   ```bash
   docker-compose up --build
   ```
3. Access API at `http://localhost:8080` and Mongo Express at `http://localhost:8081`.

## API Endpoints

- `POST /api/sessions` - Start new session
- `PATCH /api/sessions/{id}/rename?name={newName}` - Rename session
- `PATCH /api/sessions/{id}/favorite?favorite=true|false` - Mark/unmark favorite
- `DELETE /api/sessions/{id}` - Delete session
- `POST /api/sessions/{id}/messages` - Add message
- `GET /api/sessions/{id}/messages?skip=0&limit=20` - Get messages (pagination)
- `GET /api/health` - Health check

All endpoints require `X-API-KEY` header.

## API Documentation

- OpenAPI/Swagger at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

**You can now:**
- Build the project: `mvn clean package`
- Run with Docker Compose: `docker-compose up --build`
- Test the API with Swagger UI or Postman