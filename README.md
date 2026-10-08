# SplitApp

Split bills with friends: groups, shared expenses, and receipt scanning.

```
SplitApp/
  backend/             Kotlin + Ktor server (REST API)
  android/             Android app (coming next)
  docker-compose.yml   PostgreSQL database for local development
```

## Running the backend locally

1. Start the database (needs Docker Desktop running):
   ```
   docker compose up -d
   ```
2. Open the `backend` folder in IntelliJ IDEA and run `main` in `Application.kt`,
   or from a terminal:
   ```
   cd backend
   ./gradlew run
   ```
3. The server runs at http://localhost:8080. Check http://localhost:8080/health.

On first start, Flyway creates the tables from `src/main/resources/db/migration`.

## API

| Method | Path             | Auth needed | Body                                |
|--------|------------------|-------------|-------------------------------------|
| GET    | `/health`        | no          |                                     |
| POST   | `/auth/register` | no          | `{"name", "email", "password"}`     |
| POST   | `/auth/login`    | no          | `{"email", "password"}`             |
| GET    | `/me`            | yes         |                                     |

Register and login return `{"token": "...", "user": {...}}`.
For routes that need auth, send the header `Authorization: Bearer <token>`.

Errors always look like `{"error": "message"}`.

## Trying it with Postman

1. `POST http://localhost:8080/auth/register`, Body → raw → JSON:
   ```json
   { "name": "Ceyda", "email": "ceyda@example.com", "password": "supersecret" }
   ```
2. Copy the `token` from the response.
3. `GET http://localhost:8080/me`, Authorization tab → Bearer Token → paste it.
