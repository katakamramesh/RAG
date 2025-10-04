# RAG Chat Storage Service

A secure, production-ready microservice to store chat histories for Retrieval-Augmented Generation (RAG) based AI chatbots.

## Features

- ✅ Chat session and message storage (MongoDB)
- ✅ Session management (rename, favorite, delete)
- ✅ Add and retrieve chat messages with pagination
- ✅ API key authentication (`X-API-KEY` header)
- ✅ Rate limiting (configurable requests per minute)
- ✅ Centralized error handling
- ✅ CORS configuration
- ✅ Health check endpoint
- ✅ Dockerized setup (app, MongoDB, Mongo Express)
- ✅ Swagger/OpenAPI documentation

---

## Prerequisites

- **Docker** (v20.10+)
- **Docker Compose** (v2.0+)
- **Java 17** (if running locally without Docker)
- **Maven 3.8+** (if building locally)

---

## Quick Start

### 1. Clone the repository
```bash
git clone https://github.com/katakamramesh/RAG.git
cd RAG
```

### 2. Configure environment variables
Copy `.env.example` to `.env` and update values:
```bash
cp .env.example .env
```

Edit `.env`:
```properties
MONGODB_URI=mongodb://root:example@mongo:27017/ragchat?authSource=admin
API_KEYS=your-secret-api-key-here
RATE_LIMIT=100
CORS_ALLOWED_ORIGINS=*
```

### 3. Start with Docker Compose
```bash
docker-compose up --build -d
```

### 4. Verify services are running
```bash
docker-compose ps
```

You should see:
- `app` - Spring Boot application on port 8080
- `mongo` - MongoDB on port 27017
- `mongo-express` - Database UI on port 8081

---

## Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| API | http://localhost:8080 | API Key required |
| Swagger UI | http://localhost:8080/swagger-ui.html | No auth needed |
| Mongo Express | http://localhost:8081 | root / example |
| Health Check | http://localhost:8080/api/health | No auth needed |

---

## Environment Variables

| Variable               | Description | Default | Required |
|------------------------|-------------|---------|----------|
| `MONGODB_URI`          | MongoDB connection string | - | Yes |
| `API_KEYS`             | API key for authentication | - | Yes |
| `RATE_LIMIT`           | Max requests per minute per IP | 10 | No |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins (comma-separated) | * | No |

---

## API Endpoints

All endpoints (except `/api/health`) require the `X-API-KEY` header.

### Session Management

#### Create Session
```http
POST /api/sessions
Content-Type: application/json
X-API-KEY: your-api-key

{
  "userId": "user123",
  "name": "My Chat Session"
}
```

#### Rename Session
```http
PATCH /api/sessions/{sessionId}/rename?name=New%20Name
X-API-KEY: your-api-key
```

#### Mark/Unmark Favorite
```http
PATCH /api/sessions/{sessionId}/favorite?favorite=true
X-API-KEY: your-api-key
```

#### Delete Session
```http
DELETE /api/sessions/{sessionId}
X-API-KEY: your-api-key
```

### Message Management

#### Add Message
```http
POST /api/sessions/{sessionId}/messages
Content-Type: application/json
X-API-KEY: your-api-key

{
  "sender": "user",
  "content": "Hello, AI assistant!",
  "context": "Retrieved context from documents..."
}
```

#### Get Messages (with pagination)
```http
GET /api/sessions/{sessionId}/messages?skip=0&limit=20
X-API-KEY: your-api-key
```

Parameters:
- `skip`: Number of messages to skip (default: 0)
- `limit`: Maximum messages to return (default: 20)

### Health Check
```http
GET /api/health
```
No authentication required.

---

## Example Usage with cURL

### 1. Create a session
```bash
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: my-secret-key" \
  -d '{
    "userId": "user123",
    "name": "Technical Discussion"
  }'
```

Response:
```json
{
  "id": "68e0f9b7c7588742c6cc5e63",
  "userId": "user123",
  "name": "Technical Discussion",
  "favorite": false,
  "createdAt": "2025-10-04T10:40:55.617Z",
  "updatedAt": "2025-10-04T10:40:55.617Z"
}
```

### 2. Add a message
```bash
curl -X POST http://localhost:8080/api/sessions/68e0f9b7c7588742c6cc5e63/messages \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: my-secret-key" \
  -d '{
    "sender": "user",
    "content": "What is RAG?",
    "context": "Retrieval context from knowledge base..."
  }'
```

### 3. Get messages
```bash
curl -X GET "http://localhost:8080/api/sessions/68e0f9b7c7588742c6cc5e63/messages?skip=0&limit=20" \
  -H "X-API-KEY: my-secret-key"
```

---

## Docker Commands

### Start services
```bash
docker-compose up -d
```

### Stop services
```bash
docker-compose down
```

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
```

### Rebuild after code changes
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Check service status
```bash
docker-compose ps
```

---

## Local Development (without Docker)

### 1. Install MongoDB locally
Ensure MongoDB is running on `localhost:27017`

### 2. Set environment variables
```bash
export MONGODB_URI=mongodb://localhost:27017/ragchat
export API_KEYS=my-secret-key
export RATE_LIMIT=100
export CORS_ALLOWED_ORIGINS=*
```

### 3. Build and run
```bash
mvn clean package
mvn spring-boot:run
```

---

## Testing

### Using Swagger UI
1. Open http://localhost:8080/swagger-ui.html
2. Click "Authorize" and enter your API key
3. Try the endpoints interactively

### Using Postman
1. Import the API endpoints from Swagger
2. Add header: `X-API-KEY: your-api-key`
3. Test all endpoints

---

## Database Access

### Mongo Express (Web UI)
- URL: http://localhost:8081
- Username: `root`
- Password: `example`
- Database: `ragchat`
- Collections: `chat_sessions`, `chat_messages`

### MongoDB Compass (Desktop Client)
Connect with:
```
mongodb://root:example@localhost:27017/?authSource=admin
```

---

## Troubleshooting

### API returns "Invalid API Key"
- Verify `X-API-KEY` header is set correctly
- Check `.env` file for correct `API_KEYS` value
- Restart containers: `docker-compose restart`

### Cannot connect to MongoDB
- Check if MongoDB container is running: `docker-compose ps`
- View logs: `docker-compose logs mongo`
- Verify port 27017 is not in use by another service

### Rate limit errors (429 Too Many Requests)
- Increase `RATE_LIMIT` in `.env`
- Restart: `docker-compose restart`

### Port already in use
If ports 8080, 8081, or 27017 are already in use:
1. Stop conflicting services
2. Or modify ports in `docker-compose.yml`

### Application not starting
```bash
# Check logs
docker-compose logs -f app

# Rebuild from scratch
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

## Project Structure

```
RAG/
├── src/
│   └── main/
│       ├── java/com/example/ragchat/
│       │   ├── config/           # CORS, Rate limiting
│       │   ├── controller/       # REST endpoints
│       │   ├── dto/              # Data Transfer Objects
│       │   ├── exception/        # Error handling
│       │   ├── model/            # MongoDB entities
│       │   ├── repository/       # Data access layer
│       │   ├── service/          # Business logic
│       │   └── util/             # API key filter
│       └── resources/
│           └── application.properties
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── .env.example
└── README.md
```

---

## Technologies Used

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data MongoDB**
- **MongoDB 6**
- **Docker & Docker Compose**
- **Springdoc OpenAPI (Swagger)**
- **Lombok**
- **Maven**

---

## License

MIT

---

## Support

For issues or questions, please open an issue on GitHub.