# SmartShop

A shared shopping list that organizes items according to each store's category layout.

Built with React, TypeScript, Spring Boot and PostgreSQL.

## Running the application

**Requirements:** Docker Engine and Docker Compose v2 with `--wait` support (Docker Desktop on Windows/macOS). Verified with Engine 29.1.3 and Compose 2.40.3.

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

Backend tests (requires JDK 25; the Maven wrapper downloads Maven 3.10.0):

```bash
cd backend
./mvnw test
```

On Windows PowerShell use `.\mvnw.cmd test` instead. Spring Boot is 4.1.2 (see `backend/pom.xml`).

For local backend development, start PostgreSQL 17 with an empty `smartshop` database and set `SPRING_DATASOURCE_URL` (for example `jdbc:postgresql://localhost:5432/smartshop`), `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` and `SPRING_JPA_HIBERNATE_DDL_AUTO=update` in the backend process environment. Then run `./mvnw spring-boot:run` (`.\mvnw.cmd spring-boot:run` on Windows) from `backend/`. The API listens on port 8080. Compose's database is internal to Docker; local development requires a separately reachable database.

Frontend development and testing instructions (Node.js 24, npm, Vite on port 5173): [frontend/README.md](frontend/README.md). Run `npm ci`, `npm test`, `npm run lint` and `npm run build` there. In PowerShell with restricted script execution, use `npm.cmd`.

The Docker backend image is built with tests skipped. Run the tests separately when validating changes.

## Documentation

- [Specification](docs/specification.md)
- [AI decisions index](docs/ai-decisions/README.md)
- [Milestone 2 – First working version](docs/milestone-2.md)

## Try the main flow

On a fresh database, two sample stores and six shared categories are created with opposite layouts. Existing databases are left untouched. Select a store, add Milk and Bread using different categories, then change the layout under **Stores → Layout**. The shopping list follows that store's order. Check an item, switch stores and refresh: the purchase mark and selected store remain saved. **Remaining** hides purchased items; **New shopping trip** resets marks, while **Delete purchased** deletes only checked items. A category created under **Categories** must be assigned to each store explicitly. Items in unassigned categories remain purchasable and show a warning.

Quantity is optional and positive when provided; a custom unit and up to 250 characters of notes are independent optional fields. PostgreSQL uses an unrestricted `numeric` column to preserve decimal quantities. Values already rounded by older versions cannot be recovered automatically.

## Docker verification

The Docker setup was checked on Windows with Docker Desktop:

- All three containers started successfully.
- The frontend communicated with the backend through `/api/`.
- Database data survived container recreation.
- Missing database password validation worked.
