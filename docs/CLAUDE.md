# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Run with Docker Compose (recommended for development)
docker-compose up --build

# Local development with external database
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TaskServiceTest

# Generate test coverage report
mvn jacoco:report
```

### Database
```bash
# Initialize database schema
psql -d task_management -f init-db.sql

# Run with local Docker database
docker-compose -f local-docker-compose.yml up db redis
```

## Architecture Overview

### Technology Stack
- **Spring Boot 3.5.0** with Java 21
- **PostgreSQL** with JSONB support for flexible metadata storage
- **Redis** for caching and session management
- **JWT Authentication** using Auth0 library
- **MapStruct** for entity-DTO mapping
- **AOP (AspectJ)** for activity logging

### Package Structure
```
com/omori/taskmanagement/springboot/
├── annotations/         # Custom annotations (@LogActivity)
├── aspect/             # AOP aspects for cross-cutting concerns
├── config/             # Spring configuration classes
├── controller/         # REST API controllers
├── dto/                # Data Transfer Objects
│   ├── common/         # Shared DTOs (ApiResponse, RequestMetadata)
│   ├── project/        # Task-related DTOs
│   └── usermgmt/       # User management DTOs
├── exceptions/         # Custom exceptions and global handlers
├── model/              # JPA entities organized by domain
│   ├── audit/          # Activity logging entities
│   ├── collaboration/  # Sharing and collaboration entities
│   ├── notification/   # Notification system entities
│   ├── project/        # Core task management entities
│   └── usermgmt/       # User and authentication entities
├── repository/         # Spring Data JPA repositories
├── security/           # Security configuration and JWT handling
├── service/            # Business logic layer
├── utils/              # Utility classes and helpers
└── validation/         # Custom validators
```

### Key Design Patterns

**Domain-Driven Design**: Code is organized by business domains (user management, project management, audit, etc.) rather than technical layers.

**Activity Logging**: The `@LogActivity` annotation with AOP automatically logs user actions. The aspect captures request metadata, user context, and method results.

**Request Metadata Handling**: 
- `RequestMetadataInterceptor` captures HTTP request details
- `RequestMetadataHolder` provides thread-local access to metadata
- Used for activity logging and audit trails

**Caching Strategy**:
- Redis-based caching with 1-hour TTL
- Session management through Redis
- Cache keys prefixed with "task_management_"

### Database Schema Design
- **Multi-schema approach**: `user_mgmt`, `project`, `notification`, `collaboration`, `audit`
- **JSONB columns** for flexible metadata storage (task metadata, user preferences)
- **Soft delete support** for data recovery
- **Audit trail** with complete activity logging

### Security Implementation
- **JWT-based authentication** with configurable expiration
- **Stateless session management**
- **Method-level security** through Spring Security annotations
- **Request filtering** through `JwtAuthenticationFilter`

## Development Guidelines

### Environment Variables
Required environment variables (create `.env` file):
```bash
DB_URL=jdbc:postgresql://localhost:5432/task_management
DB_USERNAME=postgres
DB_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET_KEY=your-256-bit-secret-key
JWT_ISSUER=task-management-api
JWT_EXPIRATION_MINUTE=1440
```

### Key Configuration Files
- `application.yml` - Main Spring configuration with Redis, database, and logging settings
- `pom.xml` - Maven dependencies including MapStruct annotation processors
- `docker-compose.yml` - Production deployment configuration
- `init-db.sql` - Database schema initialization with multi-schema design

### Activity Logging Implementation
When adding new controller methods that should be logged:
1. Add `@LogActivity(ActionType.CREATE/UPDATE/DELETE)` annotation
2. The `ActivityLoggingAspect` will automatically capture:
   - User information (from JWT context or method arguments for auth endpoints)
   - Request metadata (IP, user agent)
   - Method results and parameters
   - Timestamp and action type

### Common Patterns
- **DTO Mapping**: Use MapStruct mappers in `security/mapper/` for entity-DTO conversions
- **Exception Handling**: Global exception handlers in `exceptions/` package
- **Validation**: Custom validators in `validation/` package
- **Repository Pattern**: Spring Data JPA repositories with custom query methods
- **Service Layer**: Business logic separated from controllers

### Testing Considerations
- No test files currently exist in the codebase
- Tests should be placed in `src/test/java/` following the same package structure
- Integration tests should use `@SpringBootTest` with test containers for database testing

### API Documentation
- Swagger UI available at `/swagger-ui.html`
- OpenAPI spec at `/v3/api-docs`
- Actuator endpoints at `/actuator/*` for health checks and metrics