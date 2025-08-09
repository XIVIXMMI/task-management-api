# Task Management API

A comprehensive, enterprise-grade task management platform built with Spring Boot. This full-featured application supports individual productivity, team collaboration, project management, and organizational workflow optimization with advanced analytics and audit capabilities.

## 🚀 Features

- **Framework**: Spring Boot (v3.5.0) with Spring Security, Spring Data JPA, Spring Validation
- **Database**: PostgreSQL with JSONB support and advanced schema design
- **Security**: JWT token-based authentication with role-based access control
- **Documentation**: Swagger/OpenAPI 3.0 with interactive testing
- **Mapping**: MapStruct for DTO transformations
- **Utilities**: Lombok for clean code, Spring Boot Actuator for monitoring
- **Containerization**: Docker & Docker Compose support

## Features Overview

This task management system provides a complete solution for personal and team productivity with the following major feature categories:

### 🔐 Authentication & Security
- **User Registration & Login** with comprehensive profile management
- **JWT Token Authentication** with 10-minute expiration and refresh capability
- **Role-Based Access Control** (USER, ADMIN, MANAGER roles)
- **Account Security** with lockout protection and session management
- **Multi-Provider Authentication** support (ready for external providers)

### 👥 User Management
- **Complete User Profiles** with avatars, timezones, and personal preferences
- **Account Status Management** with activation, verification, and soft delete
- **User Activity Tracking** with online status, last login, and session management
- **Profile Updates** with validation and self-service capabilities
- **Multi-Language Support** with timezone-aware operations

### ✅ Task Management
- **Comprehensive Task Operations** with full CRUD functionality
- **Task Hierarchy** with parent-child relationships and subtask support
- **Priority Management** (LOW, MEDIUM, HIGH, URGENT) with visual indicators
- **Status Tracking** (PENDING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD)
- **Due Date Management** with start dates and completion tracking
- **Progress Monitoring** with percentage completion and time tracking
- **Recurring Tasks** with flexible recurrence patterns
- **Task Categories** with color coding and icon support
- **Tagging System** with color-coded tags and usage analytics
- **Custom Metadata** support for extensible task properties

### 🏢 Workspace & Project Management
- **Workspace Creation** with team and personal workspace support
- **Multi-User Collaboration** with granular role permissions
- **Team Invitations** with email-based invitation system
- **Project Organization** with workspace-specific settings
- **Member Management** with role-based access (owner, admin, member, viewer)

### 💬 Communication & Collaboration
- **Threaded Comments** with user mentions and rich content support
- **File Attachments** with support for multiple file types and size tracking
- **Real-time Collaboration** with task sharing and collaborative editing
- **User Mentions** with notification integration
- **Activity Streams** for team awareness and project updates

### 🔔 Notifications & Reminders
- **Intelligent Notification System** with multiple delivery types
- **Custom Notification Templates** for consistent messaging
- **Smart Reminders** with flexible repeat intervals (daily, weekly, monthly, yearly)
- **Multi-Channel Delivery** (email, push, SMS support)
- **Notification History** with read/unread status tracking

### 📊 Analytics & Reporting
- **User Productivity Analytics** with daily metrics and productivity scoring
- **Workspace Statistics** with team performance insights
- **Task Completion Analytics** with time tracking and efficiency metrics
- **Activity Reports** with detailed user and project analytics
- **Custom Dashboards** with configurable metrics and visualizations

### 🔍 Audit & Compliance
- **Comprehensive Audit Trail** with before/after value tracking
- **Activity Logging** for all user actions and system events
- **Error Tracking** with stack traces and contextual information
- **Data Integrity** monitoring with validation and consistency checks
- **Compliance Reporting** with exportable audit logs

### 🛠️ Advanced Features
- **Soft Delete** across all entities with data recovery capabilities
- **Flexible JSON Storage** for custom fields and extensible data models
- **Advanced Search** capabilities across tasks, projects, and users
- **Bulk Operations** for efficient data management
- **Data Export/Import** functionality for backup and migration
- **Custom Validation Rules** with internationalized error messages

## 🛠 Technology Stack

### Backend Framework
- **Spring Boot 3.5.0** - Modern Java enterprise framework
- **Spring Data JPA** - Database abstraction and ORM
- **Spring Security** - Authentication and authorization
- **Spring Validation** - Request validation and error handling
- **Spring Cache** - Caching abstraction layer

### Database & Caching
- **PostgreSQL** - Primary relational database with JSONB support
- **Redis** - High-performance caching and session storage

### Development Tools
- **MapStruct** - Type-safe bean mapping
- **Lombok** - Boilerplate code reduction
- **Swagger/OpenAPI** - API documentation and testing
- **JWT (Auth0)** - JSON Web Token implementation

### DevOps & Deployment
- **Docker & Docker Compose** - Containerization and orchestration
- **Maven** - Build automation and dependency management
- **Java 21** - Modern JVM with enhanced performance

## 📋 Prerequisites

- **Java 21+** - OpenJDK or Oracle JDK
- **Maven 3.8+** - Build tool
- **Docker & Docker Compose** - Container runtime (optional)
- **PostgreSQL 15+** - Database (if not using Docker)
- **Redis 7+** - Cache server (if not using Docker)

## ⚙️ Environment Setup

### Environment Variables

Create a `.env` file in the project root:

```bash
# Database Configuration
POSTGRES_DB_SERVER_ADDRESS=localhost
POSTGRES_DB_SERVER_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
POSTGRES_DB_NAME=task_management

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET_KEY=your-256-bit-secret-key-here
JWT_ISSUER=task-management-api
JWT_EXPIRATION_MINUTE=1440

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
```

### Database Schema

The application uses a modular database schema with separate schemas for different domains:
- `user_mgmt` - User management and authentication
- `project` - Tasks, categories, and workspaces
- `collaboration` - Comments, attachments, and sharing
- `notification` - Alerts and reminders
- `audit` - Activity logs and error tracking

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repository-url>
cd task-management

# Start all services
docker-compose up --build

# Access the application
open http://localhost:8080/swagger-ui.html
```

### Option 2: Local Development

```bash
# 1. Setup PostgreSQL database
createdb task_management
psql -d task_management -f init-db.sql

# 2. Start Redis server
redis-server

# 3. Configure environment variables
cp .env.template .env
# Edit .env with your configurations

# 4. Build and run the application
mvn clean install
mvn spring-boot:run

# 5. Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

## 📚 API Documentation

### Interactive Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### API Endpoints

#### Currently Implemented
| Endpoint | Method | Description | Authentication |
|----------|--------|-------------|----------------|
| `/api/v1/auth/register` | POST | User registration with full profile | None |
| `/api/v1/auth/login` | POST | User authentication (JWT issued) | None |
| `/api/v1/user/update-profile/{username}` | PATCH | Update user profile | JWT Required |
| `/hello` | GET | Health check endpoint | None |

#### Full API Reference
- **Authentication API**: Complete user authentication and registration
- **User Management API**: Profile management, account settings, status tracking
- **Task Management API**: Full CRUD operations for tasks, categories, tags
- **Workspace API**: Team collaboration, project management, member invitations
- **Notification API**: Notification management, templates, delivery tracking
- **Analytics API**: Productivity metrics, reporting, dashboard data
- **Audit API**: Activity logs, error tracking, compliance reports

*See Swagger UI for complete API documentation with interactive testing*

#### Task Management
```http
GET    /api/v1/task           # Get tasks with filters
POST   /api/v1/task/create    # Create new task
GET    /api/v1/task/{id}      # Get task by ID
PUT    /api/v1/task/{id}      # Update task
DELETE /api/v1/task/{id}      # Delete task (hard delete)
DELETE /api/v1/task/{id}/soft # Soft delete task
```

## 🏗 Project Architecture

### Package Structure
```
src/main/java/com/omori/taskmanagement/springboot/
├── controller/          # REST API controllers
│   ├── AuthController   # Authentication endpoints
│   └── UserController   # User management endpoints
├── model/              # JPA entities and domain models
│   ├── User, Role, Task # Core business entities
│   └── Workspace, ...   # Collaboration entities
├── security/           # Security configuration
│   ├── JwtService      # JWT token management
│   ├── SecurityConfig  # Spring Security setup
│   └── dto/            # Data transfer objects
├── service/            # Business logic layer
├── repository/         # Data access layer
└── mapper/             # MapStruct mappers

src/main/resources/
├── application.yml     # Application configuration
├── messages/           # Internationalization
│   └── validation/     # Custom validation messages
└── static/             # Static resources

Database:
├── init-db.sql         # Complete database schema
└── docker-compose.yml  # PostgreSQL container setup
```

### Database Schema

The application uses a comprehensive PostgreSQL schema with 6 main categories:

- **user_mgmt**: User accounts, roles, sessions, authentication
- **project**: Tasks, workspaces, categories, tags, collaboration
- **notification**: Notifications, reminders, templates
- **collaboration**: Comments, attachments, invitations
- **audit**: Activity logs, error tracking
- **analytics**: User and workspace statistics

## Implementation Status

### ✅ Fully Implemented
- User registration and authentication with JWT
- Comprehensive user profile management
- Complete database schema with all relationships
- Security configuration with role-based access control
- Swagger/OpenAPI documentation
- Global exception handling and validation
- Docker containerization support

### 🚧 Database-Ready (Schema Complete)
- Full task management system with categories, tags, and hierarchy
- Workspace and team collaboration features  
- Notification and reminder system with templates
- File attachment and comment system
- Comprehensive audit trail and analytics
- Advanced search and reporting capabilities

*The extensive database schema indicates this platform is designed for enterprise-level task management with full team collaboration and analytics capabilities.*

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TaskServiceTest

# Generate test coverage report
mvn jacoco:report
```

## 📊 Monitoring & Health Checks

### Actuator Endpoints
- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information
- `/actuator/prometheus` - Prometheus metrics (if enabled)

### Health Check URLs
- Application: http://localhost:8080/actuator/health
- Database: http://localhost:8080/actuator/health/db
- Redis: http://localhost:8080/actuator/health/redis

## 🔒 Security Best Practices

- **Input Validation**: All endpoints validate input data
- **SQL Injection Prevention**: Parameterized queries only
- **XSS Protection**: Output encoding and sanitization
- **CSRF Protection**: Token-based protection for state-changing operations
- **Secure Headers**: Security headers configured
- **Audit Logging**: All security events logged

## 🐳 Docker Deployment

### Production Docker Compose
```yaml
version: '3.8'
services:
  app:
    image: omori/taskmanagement:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - db
      - redis
  
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: task_management
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

## 🤝 Contributing

1. Fork this repository and create your feature branch from `dev`
2. Follow the existing code style and conventions
3. Add appropriate tests for new functionality
4. Ensure all tests pass and code is properly validated
5. Submit a pull request to `dev` with a clear description

## 📄 License

See [LICENSE](LICENSE) for details.

## 👤 Author

**Omori**
- Email: nguyen.le.programmer@gmail.com
- GitHub: [@XIVIXMMI](https://github.com/XIVIXMMI)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- PostgreSQL community for the robust database
- Redis team for high-performance caching
- All contributors who helped improve this project

---

## 📈 Roadmap

### Upcoming Features
- [ ] Real-time notifications with WebSocket
- [ ] Advanced reporting and analytics
- [ ] Mobile API optimization
- [ ] GraphQL API support
- [ ] Microservices architecture migration
- [ ] Kubernetes deployment configurations

### Performance Improvements
- [ ] Database query optimization
- [ ] Caching strategy enhancement
- [ ] API response time optimization
- [ ] Memory usage optimization

---

*This task management platform combines the simplicity of personal task tracking with the power of enterprise collaboration tools, providing a scalable solution for teams of any size.*

For detailed API documentation, visit the Swagger UI at http://localhost:8080/swagger-ui.html after starting the application.