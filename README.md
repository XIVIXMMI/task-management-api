# Task Management API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7+-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A comprehensive, enterprise-grade RESTful API for task and project management built with modern Spring Boot architecture. Features hierarchical task structures (Epic/Story/Task), JWT authentication, Redis caching, workspace collaboration, and complete audit trails.

## 📑 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Environment Configuration](#-environment-configuration)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Development](#-development)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### Core Task Management
- **Hierarchical Task Structure**: Epic → Story → Task with validation
- **CRUD Operations**: Create, read, update, delete tasks with soft delete support
- **Task Filtering & Search**: Advanced filtering, pagination, and keyword search
- **Task Progress Tracking**: Progress percentage and status management
- **Task Prioritization**: Priority levels (low, medium, high, urgent, critical)
- **Due Date Management**: Start dates, due dates, and overdue tracking
- **Bulk Operations**: Batch status updates and batch deletion
- **Task Assignment**: Assign tasks to users within workspaces

### Advanced Features
- **Workspace Collaboration**: Multi-user workspaces with member management
- **Category Organization**: Categorize tasks for better organization
- **Subtasks**: Break down tasks into manageable subtasks
- **Task Hierarchy Validation**: Prevent circular dependencies and invalid relationships
- **Recurring Tasks**: Support for recurring task patterns
- **Activity Logging**: Complete audit trail with AOP-based logging
- **Redis Caching**: High-performance caching for queries (1-hour TTL)

### Security & Authentication
- **JWT Authentication**: Secure token-based authentication (Auth0 JWT 4.4.0)
- **Role-based Authorization**: USER and ADMIN roles with method-level security
- **Password Encryption**: BCrypt password hashing
- **Session Management**: Redis-backed session storage
- **Access Control**: Task ownership and workspace access validation

### Architecture & Code Quality
- **SOLID Principles**: Clean, maintainable, and testable code
- **Layered Architecture**: Clear separation of Controller, Service, Repository layers
- **Service Segregation**: Specialized services (Creation, Query, Update, Delete, Validation)
- **DTO Pattern**: Separate API contracts from domain models
- **Global Exception Handling**: Centralized error handling with meaningful messages
- **Transaction Management**: ACID compliance with Spring transactions
- **MapStruct**: Type-safe DTO-Entity mapping

### Database & Performance
- **PostgreSQL**: Robust relational database with schema separation
- **Flyway Migrations**: Version-controlled database schema
- **HikariCP**: High-performance connection pooling
- **Query Optimization**: Efficient queries with proper indexing
- **Lazy Loading**: Optimized entity loading to prevent N+1 problems
- **Redis Caching**: Intelligent caching strategy for improved performance

### API & Documentation
- **RESTful API**: Standard REST conventions
- **OpenAPI 3.0**: Complete API documentation with Swagger UI
- **Validation**: Comprehensive input validation with Jakarta Bean Validation
- **HTTP/2**: Enhanced performance with HTTP/2 support
- **Spring Actuator**: Health checks and monitoring endpoints

## 🛠 Technology Stack

### Core Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.5.0 | Application framework |
| **Spring Data JPA** | 3.5.0 | Data persistence |
| **Spring Security** | 3.5.0 | Authentication & authorization |
| **Spring Validation** | 3.5.0 | Input validation |
| **Spring Cache** | 3.5.0 | Caching abstraction |
| **Spring Actuator** | 3.5.0 | Monitoring & health checks |

### Database & Caching
| Technology | Version | Purpose |
|------------|---------|---------|
| **PostgreSQL** | 42.7.7 | Primary database |
| **Hibernate** | 6.6.0.Final | ORM implementation |
| **Flyway** | 10.10.0 | Database migrations |
| **Redis** | Latest | Caching & session storage |
| **HikariCP** | Latest | Connection pooling |

### Security
| Technology | Version | Purpose |
|------------|---------|---------|
| **Auth0 JWT** | 4.4.0 | JWT token management |
| **Spring Security** | 3.5.0 | Security framework |
| **Spring Session Redis** | Latest | Session management |
| **BCrypt** | Built-in | Password hashing |

### Development Tools
| Technology | Version | Purpose |
|------------|---------|---------|
| **Lombok** | 1.18.32 | Boilerplate reduction |
| **MapStruct** | 1.6.3 | DTO-Entity mapping |
| **SpringDoc OpenAPI** | 2.8.8 | API documentation |
| **dotenv-java** | 3.0.0 | Environment variables |

### Build & DevOps
| Technology | Version | Purpose |
|------------|---------|---------|
| **Maven** | 3.11.0 | Build automation |
| **Docker** | Latest | Containerization |
| **Docker Compose** | Latest | Multi-container orchestration |

## 🏗 Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (REST API, Request/Response Handling)  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Service Layer                   │
│  (Business Logic, Validation, Caching)  │
│                                         │
│  ├─ Creation Services                  │
│  ├─ Query Services                     │
│  ├─ Update Services                    │
│  ├─ Deletion Services                  │
│  ├─ Validation Services                │
│  ├─ Access Control Services            │
│  └─ Hierarchy Services                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Repository Layer                │
│  (Data Access, JPA Repositories)        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         PostgreSQL Database             │
│  (project, usermgmt, audit schemas)     │
└─────────────────────────────────────────┘
```

### Design Principles

✅ **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion

✅ **Separation of Concerns**: Clear boundaries between layers

✅ **Dependency Injection**: Constructor injection via Lombok `@RequiredArgsConstructor`

✅ **Fail Fast**: Early validation with meaningful error messages

✅ **Service Segregation**: Specialized services for each operation type

### Key Design Patterns

- **Repository Pattern**: Data access abstraction with Spring Data JPA
- **DTO Pattern**: Separate API contracts from domain entities
- **Builder Pattern**: Fluent object construction with Lombok `@Builder`
- **Facade Pattern**: Simplify complex subsystem interactions
- **Strategy Pattern**: Task type-specific behavior (Epic/Story/Task)
- **Chain of Responsibility**: Validation chains and exception handling

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 21+**: [Download OpenJDK](https://openjdk.org/projects/jdk/21/)
- **Maven 3.8+**: [Download Maven](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose**: [Download Docker](https://www.docker.com/get-started) (recommended)
- **PostgreSQL 15+**: If not using Docker
- **Redis 7+**: If not using Docker
- **Git**: For version control

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended)

The easiest way to get started is using Docker Compose:

```bash
# 1. Clone the repository
git clone https://github.com/XIVIXMMI/task-management.git
cd task-management

# 2. Create environment file
cp .env.example .env
# Edit .env with your configurations

# 3. Start all services (PostgreSQL, Redis, Application)
docker-compose up --build -d

# 4. View logs
docker-compose logs -f app

# 5. Access the application
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Health Check: http://localhost:8080/actuator/health
```

### Option 2: Local Development

For local development without Docker:

```bash
# 1. Clone the repository
git clone https://github.com/XIVIXMMI/task-management.git
cd task-management

# 2. Start PostgreSQL (ensure it's running on port 5432)
# Create database
createdb taskmanagement

# 3. Start Redis (ensure it's running on port 6379)
redis-server

# 4. Configure environment variables
cp .env.example .env
# Edit .env with your local database credentials

# 5. Build the application
mvn clean install -DskipTests

# 6. Run Flyway migrations (if needed)
mvn flyway:migrate

# 7. Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/task-management.jar

# 8. Access the application
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Quick Health Check

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

## ⚙️ Environment Configuration

### Required Environment Variables

Create a `.env` file in the project root with the following variables:

```bash
# ============================================
# Database Configuration
# ============================================
DB_URL=jdbc:postgresql://localhost:5432/taskmanagement
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password

# ============================================
# Redis Configuration
# ============================================
REDIS_HOST=localhost
REDIS_PORT=6379

# ============================================
# JWT Configuration
# ============================================
# Generate a secure 256-bit key: openssl rand -base64 32
JWT_SECRET_KEY=your-256-bit-secret-key-minimum-32-characters
JWT_ISSUER=task-management-api
JWT_EXPIRATION_MINUTE=1440

# ============================================
# Flyway Configuration
# ============================================
FLYWAY_ENABLED=true
```

### Application Profiles

The application supports multiple profiles:

- **dev** (default): Development mode with detailed logging
  - SQL logging enabled
  - Debug logging for application packages
  - Redis debug logging

- **prod**: Production mode with optimized settings
  - Minimal logging
  - Production-ready caching
  - Optimized connection pooling

Set the active profile:
```bash
# In .env file
SPRING_PROFILES_ACTIVE=dev

# Or via command line
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Database Schema Organization

The application uses multiple PostgreSQL schemas:

| Schema | Purpose | Tables |
|--------|---------|--------|
| **project** | Task management | tasks, workspaces, categories, subtasks, tags |
| **usermgmt** | User management | users, profiles, roles, sessions |
| **audit** | Audit trails | activity_logs, error_logs |
| **notification** | Notifications | notifications, reminders, templates |
| **collaboration** | Collaboration | comments, attachments, invitations |

## 📚 API Documentation

### Interactive Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **OpenAPI YAML**: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

### Authentication Flow

All API endpoints (except registration and login) require JWT authentication:

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "confirmPassword": "SecurePass123!"
  }'

# 2. Login and get JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "SecurePass123!"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "type": "Bearer",
#   "expiresIn": 86400000
# }

# 3. Use token in subsequent requests
curl -X GET http://localhost:8080/api/v1/task \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Key API Endpoints

#### Authentication & User Management

```http
POST   /api/v1/auth/register              # Register new user
POST   /api/v1/auth/login                 # Login and get JWT token
GET    /api/v1/user/profile               # Get user profile
PUT    /api/v1/user/profile               # Update user profile
PUT    /api/v1/user/password              # Change password
PUT    /api/v1/user/email                 # Update email
PUT    /api/v1/user/avatar                # Update avatar
```

#### Task Management

```http
# Task CRUD Operations
POST   /api/v1/task/create                # Create new task
GET    /api/v1/task/{taskId}              # Get task by ID
GET    /api/v1/task/uuid/{uuid}           # Get task by UUID
GET    /api/v1/task                       # Get tasks with filters & pagination
PUT    /api/v1/task/{taskId}              # Update task
DELETE /api/v1/task/{taskId}              # Hard delete task
DELETE /api/v1/task/{taskId}/soft         # Soft delete task

# Task Status & Progress
PATCH  /api/v1/task/{taskId}/status       # Update task status
PATCH  /api/v1/task/{taskId}/progress     # Update task progress

# Bulk Operations
PATCH  /api/v1/task/batch/status          # Update multiple tasks status
DELETE /api/v1/task/batch                 # Delete multiple tasks

# Search & Filtering
GET    /api/v1/task/search                # Search tasks by keyword
GET    /api/v1/task/overdue               # Get overdue tasks
```

#### Task Hierarchy

```http
GET    /api/v1/hierarchy/parent/{taskId}  # Get parent task
GET    /api/v1/hierarchy/children/{taskId}# Get child tasks
POST   /api/v1/hierarchy/move             # Move task to new parent
GET    /api/v1/hierarchy/validate         # Validate task hierarchy
```

### Sample Request/Response

#### Create Task Request

```json
POST /api/v1/task/create
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "title": "Implement user authentication",
  "description": "Implement JWT-based authentication with Spring Security",
  "priority": "high",
  "taskType": "TASK",
  "dueDate": "2025-10-15T17:00:00",
  "startDate": "2025-10-01T09:00:00",
  "estimatedHours": 16.0,
  "categoryId": 1,
  "workspaceId": 1,
  "assignedToId": 2,
  "parentTaskId": null,
  "isRecurring": false,
  "sortOrder": 0
}
```

#### Success Response (201 Created)

```json
{
  "success": true,
  "data": {
    "id": 123,
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Implement user authentication",
    "description": "Implement JWT-based authentication with Spring Security",
    "status": "pending",
    "priority": "high",
    "taskType": "TASK",
    "progress": 0,
    "estimatedHours": 16.0,
    "actualHours": 0.0,
    "dueDate": "2025-10-15T17:00:00",
    "startDate": "2025-10-01T09:00:00",
    "categoryName": "Development",
    "workspaceName": "Backend Team",
    "assignedToName": "Jane Smith",
    "createdAt": "2025-09-30T10:30:00",
    "updatedAt": "2025-09-30T10:30:00"
  },
  "timestamp": "2025-09-30T10:30:00"
}
```

#### Error Response (400 Bad Request)

```json
{
  "timestamp": "2025-09-30T10:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Title must be between 3 and 255 characters",
  "path": "/api/v1/task/create"
}
```

### Task Filtering & Pagination

```bash
# Get tasks with filtering and pagination
GET /api/v1/task?page=0&size=10&status=pending&priority=high&sortBy=dueDate&direction=ASC

# Search tasks by keyword
GET /api/v1/task/search?keyword=authentication&page=0&size=20
```

## 📁 Project Structure

```
task-management/
├── src/
│   └── main/
│       ├── java/com/omori/taskmanagement/
│       │   ├── annotations/              # Custom annotations (@LogActivity)
│       │   ├── aspect/                   # AOP aspects
│       │   ├── config/                   # Configuration classes
│       │   │   ├── SecurityConfiguration.java
│       │   │   ├── SwaggerConfiguration.java
│       │   │   ├── MessageConfiguration.java
│       │   │   └── WebConfig.java
│       │   ├── controller/               # REST Controllers
│       │   │   ├── task/
│       │   │   │   ├── TaskBulkController.java
│       │   │   │   ├── TaskCommandController.java
│       │   │   │   └── TaskQueryController.java
│       │   │   ├── TaskController.java
│       │   │   ├── TaskHierarchyController.java
│       │   │   └── UserUpdateController.java
│       │   ├── dto/                      # Data Transfer Objects
│       │   │   ├── common/               # ApiResult, RequestMetadata
│       │   │   ├── project/              # Task-related DTOs
│       │   │   │   ├── task/
│       │   │   │   │   ├── creation/
│       │   │   │   │   ├── update/
│       │   │   │   │   ├── TaskResponse.java
│       │   │   │   │   └── TaskFilterRequest.java
│       │   │   │   └── subtask/
│       │   │   └── usermgmt/             # User-related DTOs
│       │   ├── exceptions/               # Custom exceptions
│       │   │   ├── task/
│       │   │   │   ├── TaskNotFoundException.java
│       │   │   │   ├── TaskValidationException.java
│       │   │   │   └── TaskAccessDeniedException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── model/                    # JPA Entities
│       │   │   ├── audit/                # ActivityLog, ErrorLog
│       │   │   ├── collaboration/        # Comment, Attachment
│       │   │   ├── notification/         # Notification, Reminder
│       │   │   ├── project/              # Task, Workspace, Category
│       │   │   └── usermgmt/             # User, Profile, Role
│       │   ├── repository/               # Spring Data JPA Repositories
│       │   │   ├── audit/
│       │   │   ├── project/              # TaskRepository, WorkspaceRepository
│       │   │   └── usermgmt/             # UserRepository, RoleRepository
│       │   ├── security/                 # Security components
│       │   │   ├── jwt/                  # JWT implementation
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   ├── JwtTokenManager.java
│       │   │   │   └── JwtProperties.java
│       │   │   └── service/
│       │   │       ├── AuthService.java
│       │   │       ├── CustomUserDetails.java
│       │   │       └── UserDetailsServiceImpl.java
│       │   ├── service/                  # Business Logic
│       │   │   ├── task/
│       │   │   │   ├── creation/         # Task creation services
│       │   │   │   ├── query/            # Task query services
│       │   │   │   ├── update/           # Task update services
│       │   │   │   ├── delete/           # Task deletion services
│       │   │   │   ├── hierarchy/        # Task hierarchy services
│       │   │   │   ├── utils/            # Validation, access control
│       │   │   │   ├── TaskService.java
│       │   │   │   └── TaskServiceImpl.java
│       │   │   ├── subtask/
│       │   │   └── user/
│       │   ├── utils/                    # Utility classes
│       │   └── validation/               # Custom validators
│       └── resources/
│           ├── db/migration/             # Flyway migration scripts
│           ├── messages/                 # i18n message bundles
│           ├── application.yml           # Main configuration
│           └── banner.txt                # Application banner
├── target/                               # Build output (gitignored)
├── .env                                  # Environment variables (gitignored)
├── .env.example                          # Environment template
├── docker-compose.yml                    # Docker compose (development)
├── docker-compose.prod.yml               # Docker compose (production)
├── Dockerfile                            # Application Docker image
├── pom.xml                               # Maven configuration
├── PROJECT_SPEC.md                       # Detailed project specification
├── AI_MANIFEST.yaml                      # AI assistant configuration
├── README.md                             # This file
└── LICENSE                               # Apache 2.0 License
```

For detailed package organization and architectural decisions, see [PROJECT_SPEC.md](PROJECT_SPEC.md).

## 🔧 Development

### Building the Application

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application (skip tests)
mvn clean package -DskipTests

# Package with tests
mvn clean package

# Install to local Maven repository
mvn clean install
```

### Running the Application

```bash
# Run with Maven
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Run the JAR file
java -jar target/task-management.jar

# Run with custom port
java -jar target/task-management.jar --server.port=9090

# Run in debug mode
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar target/task-management.jar
```

### Code Generation

The project uses annotation processors:

```bash
# Lombok generates: getters, setters, constructors, builders, etc.
# MapStruct generates: mapper implementations

# Generated files location:
# target/generated-sources/annotations/
```

### Code Style & Conventions

This project follows strict coding conventions. See [PROJECT_SPEC.md](PROJECT_SPEC.md) for:

- Naming conventions (packages, classes, methods, variables)
- Code structure (controllers, services, entities)
- API design principles
- Error handling patterns
- Logging conventions
- Transaction management
- Database conventions

### Git Commit Conventions

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```bash
# Format
<type>(<scope>): <subject>

# Examples
feat(task-service): add bulk task update functionality
fix(task-query): enhance query robustness
refactor(controllers): rename ApiResponse to ApiResult
perf(task-services): enhance query performance and caching
docs(readme): update API documentation

# Types: feat, fix, hotfix, refactor, perf, style, docs, test, build, ci, chore
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TaskServiceTest

# Run specific test method
mvn test -Dtest=TaskServiceTest#testCreateTask

# Run tests with coverage
mvn test jacoco:report

# Skip tests during build
mvn package -DskipTests
```

### Test Structure

```
src/test/java/com/omori/taskmanagement/
├── controller/           # Controller integration tests
├── service/              # Service unit tests
├── repository/           # Repository tests
└── integration/          # Full integration tests
```

### Testing Best Practices

- Follow AAA pattern: Arrange, Act, Assert
- Use meaningful test names
- Mock external dependencies
- Test both success and failure scenarios
- Keep tests independent and isolated

## 🐳 Deployment

### Docker Deployment

#### Development Environment

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Remove volumes
docker-compose down -v
```

#### Production Environment

```bash
# Build production image
docker build -t omori/taskmanagement:latest .

# Run production compose
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f app

# Stop services
docker-compose -f docker-compose.prod.yml down
```

### Manual Deployment

```bash
# 1. Build the application
mvn clean package -DskipTests

# 2. Copy JAR to server
scp target/task-management.jar user@server:/opt/taskmanagement/

# 3. Create systemd service
sudo nano /etc/systemd/system/taskmanagement.service

# Service file content:
[Unit]
Description=Task Management API
After=postgresql.service redis.service

[Service]
Type=simple
User=taskmanagement
WorkingDirectory=/opt/taskmanagement
ExecStart=/usr/bin/java -jar task-management.jar
Restart=always
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target

# 4. Start service
sudo systemctl daemon-reload
sudo systemctl enable taskmanagement
sudo systemctl start taskmanagement

# 5. Check status
sudo systemctl status taskmanagement
```

### Environment-Specific Configuration

Create different `.env` files:
- `.env.dev` - Development
- `.env.staging` - Staging
- `.env.prod` - Production

Load the appropriate file before starting:
```bash
export $(cat .env.prod | xargs)
java -jar target/task-management.jar
```

## 📊 Monitoring & Health Checks

### Spring Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Detailed health (requires authentication)
curl http://localhost:8080/actuator/health -H "Authorization: Bearer <token>"

# Application info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### Available Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/env` | Environment properties |
| `/actuator/loggers` | Logger configuration |
| `/actuator/threaddump` | Thread dump |
| `/actuator/heapdump` | Heap dump (download) |

### Logging

Application logs can be found in:
- Console output (default)
- Custom log file (if configured)

Log levels are configured in `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.omori.taskmanagement: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: OFF
```

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Workflow

1. **Fork the repository**
   ```bash
   # Fork on GitHub, then clone your fork
   git clone https://github.com/YOUR-USERNAME/task-management.git
   cd task-management
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **Make your changes**
   - Follow the [coding conventions](PROJECT_SPEC.md#coding-conventions--standards)
   - Write tests for new functionality
   - Update documentation as needed

4. **Run tests**
   ```bash
   mvn test
   ```

5. **Commit your changes**
   ```bash
   # Follow Conventional Commits format
   git commit -m "feat(task-service): add bulk export functionality"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/amazing-feature
   ```

7. **Create Pull Request**
   - Target the `dev` branch
   - Provide clear description of changes
   - Reference related issues

### Code Review Checklist

Before submitting a PR, ensure:

- ✅ Code follows SOLID principles
- ✅ All tests pass
- ✅ New features have tests
- ✅ Code is properly documented
- ✅ Follows naming conventions
- ✅ No security vulnerabilities
- ✅ API documentation updated (if applicable)
- ✅ Commit messages follow conventional format

### Reporting Issues

When reporting bugs, please include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Environment details (Java version, OS, etc.)
- Error messages or stack traces

## 📄 License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

```
Copyright 2025 Omori

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 👤 Author

**Omori**
- Email: [nguyen.le.programmer@gmail.com](mailto:nguyen.le.programmer@gmail.com)
- GitHub: [@XIVIXMMI](https://github.com/XIVIXMMI)

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Excellent application framework
- [PostgreSQL](https://www.postgresql.org/) - Robust and reliable database
- [Redis](https://redis.io/) - High-performance caching solution
- [MapStruct](https://mapstruct.org/) - Type-safe mapping framework
- [Lombok](https://projectlombok.org/) - Reducing boilerplate code
- [Auth0](https://github.com/auth0/java-jwt) - JWT library
- [SpringDoc](https://springdoc.org/) - OpenAPI documentation

## 📚 Additional Documentation

- **[PROJECT_SPEC.md](PROJECT_SPEC.md)** - Comprehensive project specification
- **[AI_MANIFEST.yaml](AI_MANIFEST.yaml)** - AI assistant configuration
- **[Swagger UI](http://localhost:8080/swagger-ui.html)** - Interactive API documentation (when running)

## 🔗 Useful Links

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**Note**: This is an active development project. Features and APIs may change. For the latest updates and roadmap, please check the [issues](https://github.com/XIVIXMMI/task-management/issues) and [project board](https://github.com/XIVIXMMI/task-management/projects).

For questions or support, please [open an issue](https://github.com/XIVIXMMI/task-management/issues/new) or contact the maintainer.