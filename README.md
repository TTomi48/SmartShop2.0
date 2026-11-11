# SmartShop

A shared shopping list that organizes items according to each store's category layout.

Built with React, TypeScript, Spring Boot and PostgreSQL.

## Running the application

**Requirements:** Docker and Docker Compose (Docker Desktop on Windows/macOS).

1. Copy `.env.example` to `.env`.
2. Set `SMARTSHOP_DB_PASSWORD` in `.env`.
3. Run the following command from the repository root:

```bash
docker compose up --build -d --wait --wait-timeout 180
```

Open **http://localhost:8080**.

The command starts the frontend, backend and PostgreSQL database. No local Java, Maven, Node.js or PostgreSQL installation is required.

If port 8080 is unavailable, change `SMARTSHOP_PORT` in `.env` and use the configured port.

## Docker commands

```bash
docker compose stop
docker compose up -d
docker compose ps
docker compose logs --tail 100
docker compose down
```

Database data is stored in the `postgres_data` Docker volume and is preserved between restarts and container recreation.

**Warning:** `docker compose down --volumes` deletes the database volume. Changing the database credentials in `.env` does not update an already initialized database.

Keep `.env` out of Git.

## Development and testing

Backend tests (requires Java 25 and Maven locally):

```bash
cd backend
mvn test
```

Frontend development and testing instructions: [frontend/README.md](frontend/README.md).

The Docker backend image is built with tests skipped. Run the tests separately when validating changes.

## Documentation

- [Specification](docs/specification.md)
- [AI decisions](docs/ai-decisions)

## Docker verification

The Docker setup was checked on Windows with Docker Desktop:

- All three containers started successfully.
- The frontend communicated with the backend through `/api/`.
- Database data survived container recreation.
- Missing database password validation worked.