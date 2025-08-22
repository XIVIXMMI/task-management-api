# Task Management API

A comprehensive, enterprise-grade RESTful API for task and project management with advanced features including user collaboration, real-time notifications, and robust security. Built with modern Spring Boot architecture and optimized for scalability and performance.

## 🚀 Features

### Core Functionality
- **Task Management**: Create, update, delete, and organize tasks with priorities, due dates, and progress tracking
- **User Management**: Complete user authentication, authorization, and profile management
- **Workspace Collaboration**: Multi-user workspaces with role-based access control
- **Category & Tagging**: Organize tasks with custom categories and tags
- **Advanced Search**: Full-text search across tasks with filtering and pagination
- **Subtasks**: Break down complex tasks into manageable subtasks
- **Recurring Tasks**: Automated task creation with flexible recurrence patterns

### Advanced Features
- **Redis Caching**: High-performance caching for improved response times
- **Soft Delete**: Recoverable task deletion with audit trails
- **JSONB Support**: Store flexible metadata and recurrence patterns
- **Batch Operations**: Efficiently update multiple tasks simultaneously
- **Activity Logging**: Complete audit trail of all system activities
- **Real-time Notifications**: Email, push, and SMS notifications
- **File Attachments**: Support for task-related file uploads

### Security & Performance
- **JWT Authentication**: Secure token-based authentication
- **Role-based Authorization**: Granular permission management
- **Data Validation**: Comprehensive input validation with custom messages
- **Transaction Management**: ACID compliance for data integrity
- **Lazy Loading Optimization**: Efficient database queries to prevent N+1 problems
- **API Documentation**: Complete Swagger/OpenAPI documentation

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
psql -d task_management -f src/main/resources/db/migration/V1_Initial_schema.sql

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

### Key Endpoints

#### Authentication
```http
POST /api/v1/auth/register    # User registration
POST /api/v1/auth/login       # User login (returns JWT)
```

#### Task Management
```http
GET    /api/v1/task           # Get tasks with filters
POST   /api/v1/task/create    # Create new task
GET    /api/v1/task/{id}      # Get task by ID
PUT    /api/v1/task/{id}      # Update task
DELETE /api/v1/task/{id}      # Delete task (hard delete)
DELETE /api/v1/task/{id}/soft # Soft delete task

# Advanced Operations
PUT    /api/v1/task/{id}/status   # Update task status
PUT    /api/v1/task/{id}/progress # Update task progress
PATCH  /api/v1/task/batch/status  # Bulk status update
GET    /api/v1/task/overdue       # Get overdue tasks
GET    /api/v1/task/search        # Search tasks by keyword
```

#### User Management
```http
GET    /api/v1/user/profile       # Get user profile
PUT    /api/v1/user/profile       # Update profile
PUT    /api/v1/user/password      # Change password
PUT    /api/v1/user/email         # Update email
PUT    /api/v1/user/avatar        # Update avatar
```

### Sample Request/Response

#### Create Task
```json
POST /api/v1/task/create
{
  "title": "Complete project documentation",
  "description": "Write comprehensive API documentation",
  "priority": "high",
  "dueDate": "2024-01-15T17:00:00",
  "estimatedHours": 8.0,
  "category": {"id": 1},
  "workspace": {"id": 1},
  "metadata": {
    "complexity": "medium",
    "reviewRequired": true
  }
}
```

#### Response
```json
{
  "id": 123,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Complete project documentation",
  "status": "pending",
  "priority": "high",
  "progress": 0,
  "categoryName": "Development",
  "workspaceName": "Main Project",
  "createdAt": "2024-01-10T10:30:00",
  "updatedAt": "2024-01-10T10:30:00"
}
```

## 🏗 Project Architecture

### Package Structure
```
src/main/java/com/omori/taskmanagement/springboot/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
│   ├── project/         # Task-related DTOs
│   └── usermgmt/        # User-related DTOs
├── exceptions/          # Custom exceptions and handlers
├── model/               # JPA entities
│   ├── audit/           # Audit and logging entities
│   ├── collaboration/   # Collaboration entities
│   ├── notification/    # Notification entities
│   ├── project/         # Task and project entities
│   └── usermgmt/        # User management entities
├── repository/          # Data access layer
├── security/            # Security configuration and services
├── service/             # Business logic layer
├── utils/               # Utility classes
└── validation/          # Custom validators
```

### Key Design Patterns
- **Repository Pattern**: Clean separation of data access logic
- **Service Layer Pattern**: Business logic encapsulation
- **DTO Pattern**: Data transfer and validation
- **Builder Pattern**: Complex object construction
- **Factory Pattern**: Object creation abstraction

## 🔧 Configuration

### Application Profiles
- `dev` - Development with debug logging
- `prod` - Production optimizations
- `test` - Testing configuration

### Caching Strategy
- **Task Details**: 1-hour TTL for frequently accessed tasks
- **User Sessions**: Redis-based session management
- **Search Results**: 30-minute cache for search queries

### Security Configuration
- **JWT Expiration**: 24 hours (configurable)
- **Password Encryption**: BCrypt with strength 12
- **CORS**: Configurable origins and methods
- **Rate Limiting**: Redis-based request throttling

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

## 🚀 Performance Optimizations

### Database Optimizations
- **Query Optimization**: JOIN FETCH for related entities
- **Indexing Strategy**: Optimized indexes for common queries
- **Connection Pooling**: HikariCP for database connections
- **Lazy Loading**: Configured to prevent N+1 query problems

### Caching Strategy
- **Redis Caching**: Frequently accessed data cached
- **Query Result Caching**: Database query results cached
- **Session Caching**: User sessions stored in Redis

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

1. **Fork the repository** and create your feature branch
   ```bash
   git checkout -b feature/amazing-feature
   ```

2. **Follow coding standards**
   - Java naming conventions
   - Comprehensive JavaDoc documentation
   - Unit tests for new features
   - Integration tests for endpoints

3. **Commit your changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```

4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```

5. **Open a Pull Request** against the `dev` branch

### Code Quality Guidelines
- **Code Coverage**: Maintain >80% test coverage
- **Documentation**: Update API documentation for changes
- **Performance**: Consider performance impact of changes
- **Security**: Follow security best practices

## 🐛 Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check PostgreSQL status
systemctl status postgresql

# Test database connection
psql -h localhost -U postgres -d task_management
```

#### Redis Connection Issues
```bash
# Check Redis status
redis-cli ping

# Test Redis connection
redis-cli -h localhost -p 6379 info
```

#### JWT Token Issues
- Verify JWT_SECRET_KEY is properly configured
- Check token expiration settings
- Ensure system clock synchronization

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

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

For detailed API documentation, visit the Swagger UI at http://localhost:8080/swagger-ui.html after starting the application.