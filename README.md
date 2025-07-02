# Task Management API

A robust, modular, and secure RESTful API for managing tasks, users, projects, and collaborations. Built with Spring Boot, PostgreSQL, JWT authentication, and follows best practices for clean architecture and validation.

## Technologies

- Spring Boot (v3.5.0)
- Spring Data JPA
- Spring Validation
- Spring Security + JWT Token
- PostgreSQL
- MapStruct
- Lombok
- Swagger (Open API)

## Features

- **User Management**: Registration, login, roles, and session management.
- **Task & Project Management**: Entities for tasks, projects, collaboration, and notifications.
- **Authentication & Authorization**: JWT-based security, role-based access control.
- **Validation**: Strong validation with custom messages.
- **API Documentation**: Swagger/OpenAPI integration.
- **Docker Support**: Dockerfile and docker-compose for easy setup.

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for containerized setup)
- PostgreSQL (if not using Docker)

### Environment Variables

Configure your `.env` file (see `.env` in the project root) for database and JWT secrets.

### Running with Docker

```sh
docker-compose up --build
```

- This will start the API and a PostgreSQL database using the provided `docker-compose.yml`.

### Running Locally

1. Create and configure your database (see `init-db.sql`).
2. Update `src/main/resources/application.yml` with your DB credentials.
3. Build and run:

```sh
mvn clean install
mvn spring-boot:run
```

### API Documentation

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI docs: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Main Endpoints

| Endpoint                 | Description                |
|--------------------------|----------------------------|
| POST `/api/v1/auth/register` | User registration         |
| POST `/api/v1/auth/login`    | User login (JWT issued)   |
| ...                      | (See Swagger for more)     |

### Project Structure

- `src/main/java/com/omori/taskmanagement/springboot/model` - Domain models (User, Role, Task, Project, etc.)
- `src/main/java/com/omori/taskmanagement/springboot/controller` - REST controllers (authentication, etc.)
- `src/main/java/com/omori/taskmanagement/springboot/security` - Security config, JWT, DTOs, mappers
- `src/main/resources/messages/validation` - Custom validation messages
- `init-db.sql` - Database schema and seed data

### Contribution

1. Fork this repo and create your branch from `dev`.
2. Make your changes.
3. Open a PR to `dev` with a clear description.

### License

See [LICENSE](LICENSE) for details.
