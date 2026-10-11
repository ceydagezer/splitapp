# SplitApp

Split bills with friends: groups, shared expenses and balances — a Splitwise-style app.

> **Status:** The backend is being rebuilt with **Kotlin + Spring Boot** on the `spring-boot-migration`
> branch. The first version (Ktor) is kept at the `ktor-final` tag for reference only.

```
SplitApp/
  backend/              Kotlin + Spring Boot REST API
  docker-compose.yml    PostgreSQL database for local development
  PROJECT_CONTEXT.md    Architecture, tech stack, database design and decisions (Turkish)
  LEARNING_NOTES.md     Step-by-step learning notes (Turkish)
```

The Android app will be added later.

## Tech stack

Kotlin 2.3 · Java 21 · Spring Boot 4.1 · Spring Web MVC · Spring Data JPA (Hibernate) ·
Flyway · PostgreSQL 17 · Gradle (Kotlin DSL) · JUnit

## Running the backend locally

Requirements: JDK 21 and Docker Desktop.

1. Start the database (from the project root):
   ```
   docker compose up -d
   ```
2. Start the server:
   ```
   cd backend
   ./gradlew bootRun
   ```
   Or open the `backend` folder in IntelliJ IDEA and run `main` in `SplitAppApplication.kt`.
3. Check http://localhost:8080/health → `{"status":"ok"}`

On first start, Flyway creates the tables from `backend/src/main/resources/db/migration`.

## Running the tests

Tests use a separate database so they never touch development data. Create it once:

```
docker exec splitapp-db-1 createdb -U splitapp splitapp_test
```

Then:

```
cd backend
./gradlew test
```

## API

| Method | Path      | Auth needed | Status                   |
|--------|-----------|-------------|--------------------------|
| GET    | `/health` | no          | ✅ available              |
| POST   | `/auth/register` | no   | 🚧 planned                |
| POST   | `/auth/login`    | no   | 🚧 planned                |
| GET    | `/me`     | yes (JWT)   | 🚧 planned                |

Errors will always look like `{"error": "message"}`.

## Configuration

Every setting can be overridden with an environment variable (defaults are for local development only):

| Variable            | Default                                     |
|---------------------|---------------------------------------------|
| `PORT`              | `8080`                                      |
| `DATABASE_URL`      | `jdbc:postgresql://localhost:5432/splitapp` |
| `DATABASE_USER`     | `splitapp`                                  |
| `DATABASE_PASSWORD` | `splitapp_dev_password`                     |
