# RAG Chat Storage Service with LLM Integration

A secure, production-ready microservice to store chat histories for Retrieval-Augmented Generation (RAG) based AI chatbots with integrated LLM support.

## Features

- ✅ Chat session and message storage (MongoDB)
- ✅ Session management (rename, favorite, delete)
- ✅ Add and retrieve chat messages with pagination
- ✅ **🆕 LLM Integration (OpenAI GPT)**
- ✅ **🆕 Conversational AI with context and history**
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
- **OpenAI API Key** (for LLM integration)

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

# LLM Configuration
LLM_API_KEY=your-openai-api-key-here
LLM_API_URL=https://api.openai.com/v1/chat/completions
LLM_MODEL=gpt-3.5-turbo
LLM_MAX_TOKENS=1000
LLM_TEMPERATURE=0.7
```

### 3. Get OpenAI API Key
1. Visit https://platform.openai.com/api-keys
2. Create a new API key
3. Add it to your `.env` file as `LLM_API_KEY`

### 4. Start with Docker Compose
```bash
docker-compose up --build -d
```

### 5. Verify services are running
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
| `LLM_API_KEY`          | OpenAI API key | - | Yes |
| `LLM_API_URL`          | LLM API endpoint | OpenAI URL | No |
| `LLM_MODEL`            | Model name | gpt-3.5-turbo | No |
| `LLM_MAX_TOKENS`       | Max response tokens | 1000 | No |
| `LLM_TEMPERATURE`      | Creativity (0-2) | 0.7 | No |

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

#### Add Message (without LLM)
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

### 🆕 LLM Integration Endpoints

#### Chat with LLM (Save to Session)
Query the LLM and automatically save both user query and AI response to the session.

```http
POST /api/sessions/{sessionId}/chat
Content-Type: application/json
X-API-KEY: your-api-key

{
  "query": "What is RAG in AI?",
  "context": "Optional context from knowledge base...",
  "includeHistory": true
}
```

**Response:**
```json
{
  "userMessage": {
    "id": "msg1",
    "sessionId": "session123",
    "sender": "user",
    "content": "What is RAG in AI?",
    "context": "Optional context...",
    "timestamp": "2025-10-06T10:00:00.000Z"
  },
  "assistantMessage": {
    "id": "msg2",
    "sessionId": "session123",
    "sender": "assistant",
    "content": "RAG stands for Retrieval-Augmented Generation...",
    "timestamp": "2025-10-06T10:00:01.000Z"
  },
  "response": "RAG stands for Retrieval-Augmented Generation..."
}
```

#### Direct LLM Query (No Save)
Query the LLM without saving to database - useful for testing.

```http
POST /api/llm/query
Content-Type: application/json
X-API-KEY: your-api-key

{
  "query": "Explain machine learning in simple terms",
  "context": "Optional context..."
}
```

**Response:**
```json
{
  "query": "Explain machine learning in simple terms",
  "response": "Machine learning is a type of artificial intelligence..."
}
```

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

### 2. Chat with LLM (with context and history)
```bash
curl -X POST http://localhost:8080/api/sessions/68e0f9b7c7588742c6cc5e63/chat \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: my-secret-key" \
  -d '{
    "query": "What is RAG?",
    "context": "RAG is a technique that combines retrieval and generation...",
    "includeHistory": true
  }'
```

### 3. Direct LLM query (testing)
```bash
curl -X POST http://localhost:8080/api/llm/query \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: my-secret-key" \
  -d '{
    "query": "What are the benefits of microservices?",
    "context": ""
  }'
```

### 4. Get conversation history
```bash
curl -X GET "http://localhost:8080/api/sessions/68e0f9b7c7588742c6cc5e63/messages?skip=0&limit=20" \
  -H "X-API-KEY: my-secret-key"
```

---

## LLM Integration Details

### Supported LLM Providers

#### OpenAI (Default)
```properties
LLM_API_KEY=sk-...
LLM_API_URL=https://api.openai.com/v1/chat/completions
LLM_MODEL=gpt-3.5-turbo
```

#### Azure OpenAI
```properties
LLM_API_KEY=your-azure-key
LLM_API_URL=https://your-resource.openai.azure.com/openai/deployments/your-deployment/chat/completions?api-version=2023-05-15
LLM_MODEL=gpt-35-turbo
```

#### Other OpenAI-Compatible APIs
Any API that follows OpenAI's chat completion format will work.

### Features

- **Context Injection**: Automatically includes retrieved context in the prompt
- **Conversation History**: Maintains chat history for contextual responses
- **Error Handling**: Graceful fallback on LLM failures
- **Token Management**: Configurable max tokens and temperature
- **Logging**: Comprehensive logging of all LLM interactions

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
export LLM_API_KEY=your-openai-api-key
export LLM_MODEL=gpt-3.5-turbo
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
4. Test the new `/api/sessions/{id}/chat` endpoint
5. Test the `/api/llm/query` endpoint

### Using Postman
1. Import the API endpoints from Swagger
2. Add header: `X-API-KEY: your-api-key`
3. Test all endpoints including LLM integration

---

## Project Structure

```
RAG/
├── src/
│   └── main/
│       ├── java/com/example/ragchat/
│       │   ├── config/           # CORS, Rate limiting, Logging
│       │   ├── controller/       # REST endpoints (with LLM chat)
│       │   ├── dto/              # Data Transfer Objects (including LLM DTOs)
│       │   ├── exception/        # Error handling (including LLMException)
│       │   ├── model/            # MongoDB entities
│       │   ├── repository/       # Data access layer
│       │   ├── service/          # Business logic + LLMService
│       │   └── util/             # API key filter
│       └── resources/
│           ├── application.properties
│           └── logback-spring.xml
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
- **OpenAI GPT API** (LLM Integration)
- **RestTemplate** (HTTP Client)

---

## Troubleshooting

### LLM Integration Issues

#### "Invalid API Key" or 401 Unauthorized
- Verify your OpenAI API key is correct in `.env`
- Check if the key is active at https://platform.openai.com/api-keys
- Restart containers: `docker-compose restart`

#### "Model not found" or 404 Error
- Verify the model name is correct (e.g., `gpt-3.5-turbo`, `gpt-4`)
- Check your OpenAI account has access to the model
- Update `LLM_MODEL` in `.env`

#### Timeout or Slow Responses
- Increase `LLM_MAX_TOKENS` if responses are cut off
- Reduce `LLM_MAX_TOKENS` for faster responses
- Check your internet connection
- Verify OpenAI service status

#### Rate Limit Errors from OpenAI
- Check your OpenAI usage limits
- Implement request queuing if needed
- Consider upgrading your OpenAI plan

### General Issues

#### API returns "Invalid API Key"
- Verify `X-API-KEY` header is set correctly
- Check `.env` file for correct `API_KEYS` value
- Restart containers: `docker-compose restart`

#### Cannot connect to MongoDB
- Check if MongoDB container is running: `docker-compose ps`
- View logs: `docker-compose logs mongo`
- Verify port 27017 is not in use by another service

#### Rate limit errors (429 Too Many Requests)
- Increase `RATE_LIMIT` in `.env`
- Restart: `docker-compose restart`

#### Application not starting
```bash
# Check logs
docker-compose logs -f app

# Rebuild from scratch
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

## Security Best Practices

1. **Never commit `.env` file** - It contains sensitive keys
2. **Use strong API keys** - Generate secure random keys
3. **Rotate API keys regularly** - Update keys periodically
4. **Secure OpenAI API key** - Keep it confidential
5. **Use HTTPS in production** - Enable SSL/TLS
6. **Implement rate limiting** - Prevent abuse
7. **Monitor API usage** - Track OpenAI costs

---

## Cost Optimization

### OpenAI Usage
- Use `gpt-3.5-turbo` for cost-effective responses
- Set reasonable `LLM_MAX_TOKENS` to control costs
- Implement caching for common queries
- Monitor token usage in logs

---

## License

MIT

---

## Support

For issues or questions:
- Open an issue on GitHub
- Check OpenAI documentation: https://platform.openai.com/docs
- Review Swagger UI for API details

---

## Changelog

### v2.0.0 - LLM Integration
- ✅ Added LLM service with OpenAI integration
- ✅ New `/api/sessions/{id}/chat` endpoint
- ✅ New `/api/llm/query` endpoint for testing
- ✅ Conversation history support
- ✅ Context injection for RAG
- ✅ Comprehensive error handling for LLM failures
- ✅ Configuration via environment variables