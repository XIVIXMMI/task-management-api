# Ubuntu Server Deployment Guide for Beginners

## 🎯 Overview
This guide helps you deploy the Task Management application on your own Ubuntu server, perfect for beginners who want to learn CI/CD concepts hands-on.

## 📋 Prerequisites

### Server Requirements
- **Ubuntu 20.04 LTS** or newer
- **Minimum**: 2GB RAM, 1 CPU, 20GB storage
- **Recommended**: 4GB RAM, 2 CPU, 40GB storage
- **SSH access** to the server

### Local Development Environment
```bash
# Your current setup
Dev DB: PostgreSQL 16 on port 5432
Container: postgres_container

# We'll add test DB on different port
Test DB: PostgreSQL 16 on port 5433
```

## 🚀 Step-by-Step Setup

### Step 1: Prepare Your Ubuntu Server

#### 1.1 Update system
```bash
sudo apt update && sudo apt upgrade -y
```

#### 1.2 Install required packages
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo apt install docker-compose -y

# Install Java 21 (for local builds)
sudo apt install openjdk-21-jdk -y

# Install Git
sudo apt install git -y

# Install PostgreSQL client (for database operations)
sudo apt install postgresql-client -y
```

#### 1.3 Reboot to apply Docker permissions
```bash
sudo reboot
```

### Step 2: Setup Test Database Locally

#### 2.1 Create test database container
```bash
# Stop any existing test containers
docker stop postgres_test 2>/dev/null || true
docker rm postgres_test 2>/dev/null || true

# Start test database on port 5433
docker run -d \
  --name postgres_test \
  --restart unless-stopped \
  -e POSTGRES_DB=task_management_test \
  -e POSTGRES_USER=test_user \
  -e POSTGRES_PASSWORD=test_password \
  -p 5433:5432 \
  -v postgres_test_data:/var/lib/postgresql/data \
  postgres:16

# Wait for database to start
sleep 10

# Initialize test database
PGPASSWORD=test_password psql -h localhost -p 5433 -U test_user -d task_management_test -f init-db.sql
```

#### 2.2 Update your application configuration

Create `src/main/resources/application-test.yml`:
```yaml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:postgresql://localhost:5433/task_management_test
    username: test_user
    password: test_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms

jwt:
  secret-key: test-secret-key-for-local-that-is-256-bits-long-and-secure
  issuer: task-management-test
  expiration-minute: 60

logging:
  level:
    com.omori.taskmanagement: DEBUG
    org.springframework.security: DEBUG
```

### Step 3: Create Server Deployment Scripts

#### 3.1 Create deployment directory structure on server
```bash
# On your server
mkdir -p /opt/task-management/{config,logs,data}
cd /opt/task-management

# Create environment file
cat > .env << EOF
# Database Configuration
DB_URL=jdbc:postgresql://db:5432/task_management
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password_here

# Redis Configuration  
REDIS_HOST=redis
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET_KEY=your-production-256-bit-secret-key-here
JWT_ISSUER=task-management-prod
JWT_EXPIRATION_MINUTE=1440

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
EOF
```

#### 3.2 Create production docker-compose.yml on server
```bash
# On your server
cat > docker-compose.prod.yml << EOF
version: '3.8'

services:
  app:
    image: task-management:latest
    container_name: task-management-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:postgresql://db:5432/task_management
      - DB_USERNAME=postgres
      - DB_PASSWORD=\${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - JWT_SECRET_KEY=\${JWT_SECRET_KEY}
      - JWT_ISSUER=\${JWT_ISSUER}
      - JWT_EXPIRATION_MINUTE=\${JWT_EXPIRATION_MINUTE}
    depends_on:
      - db
      - redis
    networks:
      - app-network
    volumes:
      - ./logs:/app/logs

  db:
    image: postgres:16
    container_name: task-management-db
    restart: unless-stopped
    environment:
      - POSTGRES_DB=task_management
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=\${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    networks:
      - app-network

  redis:
    image: redis:7-alpine
    container_name: task-management-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - app-network

volumes:
  postgres_data:
  redis_data:

networks:
  app-network:
    driver: bridge
EOF
```

### Step 4: Manual Deployment Process (Before CI/CD)

#### 4.1 Create deployment script
```bash
# On your local machine, create deploy.sh
cat > deploy.sh << 'EOF'
#!/bin/bash
set -e

echo "🚀 Starting deployment..."

# Build application
echo "📦 Building application..."
mvn clean package -DskipTests -Dspring.profiles.active=prod

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t task-management:latest .

# Save image to tar file
echo "💾 Exporting Docker image..."
docker save task-management:latest > task-management.tar

# Copy to server
echo "📤 Uploading to server..."
scp task-management.tar your-username@your-server-ip:/opt/task-management/
scp init-db.sql your-username@your-server-ip:/opt/task-management/

# Deploy on server
echo "🚀 Deploying on server..."
ssh your-username@your-server-ip << 'REMOTE_SCRIPT'
cd /opt/task-management

# Load Docker image
docker load < task-management.tar

# Stop existing containers
docker-compose -f docker-compose.prod.yml down

# Start new containers
docker-compose -f docker-compose.prod.yml up -d

# Wait for health check
sleep 30

# Check if application is running
if curl -f http://localhost:8080/actuator/health; then
    echo "✅ Deployment successful!"
else
    echo "❌ Deployment failed!"
    exit 1
fi
REMOTE_SCRIPT

echo "🎉 Deployment completed!"
EOF

chmod +x deploy.sh
```

#### 4.2 Manual deployment
```bash
# Update server details in deploy.sh, then run:
./deploy.sh
```

### Step 5: Setup GitHub Actions for Automatic Deployment

#### 5.1 Generate SSH key for GitHub Actions
```bash
# On your local machine
ssh-keygen -t rsa -b 4096 -C "github-actions-deploy" -f ~/.ssh/github-actions
cat ~/.ssh/github-actions.pub

# Copy public key to server
ssh-copy-id -i ~/.ssh/github-actions your-username@your-server-ip

# Copy private key content for GitHub secrets
cat ~/.ssh/github-actions
```

#### 5.2 Configure GitHub repository secrets
Go to GitHub repository → Settings → Secrets → Add:

- `SERVER_HOST`: your-server-ip
- `SERVER_USER`: your-username
- `SERVER_SSH_KEY`: (private key content from step 5.1)
- `DB_PASSWORD`: your_secure_password
- `JWT_SECRET_KEY`: your-production-256-bit-secret-key

#### 5.3 Create simple deployment workflow
```yaml
# .github/workflows/deploy.yml
name: Deploy to Ubuntu Server

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Build application
      run: |
        mvn clean package -DskipTests -Dspring.profiles.active=prod
    
    - name: Build Docker image
      run: |
        docker build -t task-management:latest .
        docker save task-management:latest > task-management.tar
    
    - name: Deploy to server
      uses: appleboy/ssh-action@v1.0.0
      with:
        host: ${{ secrets.SERVER_HOST }}
        username: ${{ secrets.SERVER_USER }}
        key: ${{ secrets.SERVER_SSH_KEY }}
        script: |
          cd /opt/task-management
          
          # Load new image
          docker load < /tmp/task-management.tar
          
          # Update environment variables
          export DB_PASSWORD="${{ secrets.DB_PASSWORD }}"
          export JWT_SECRET_KEY="${{ secrets.JWT_SECRET_KEY }}"
          
          # Deploy
          docker-compose -f docker-compose.prod.yml down
          docker-compose -f docker-compose.prod.yml up -d
          
          # Health check
          sleep 30
          curl -f http://localhost:8080/actuator/health
    
    - name: Upload image to server
      uses: appleboy/scp-action@v0.1.4
      with:
        host: ${{ secrets.SERVER_HOST }}
        username: ${{ secrets.SERVER_USER }}
        key: ${{ secrets.SERVER_SSH_KEY }}
        source: "task-management.tar"
        target: "/tmp/"
```

### Step 6: Monitoring and Troubleshooting

#### 6.1 Basic monitoring commands
```bash
# Check application logs
docker logs task-management-app -f

# Check database logs
docker logs task-management-db -f

# Check container status
docker ps

# Check system resources
docker stats
```

#### 6.2 Health check endpoints
```bash
# Application health
curl http://your-server-ip:8080/actuator/health

# Application info
curl http://your-server-ip:8080/actuator/info

# Metrics
curl http://your-server-ip:8080/actuator/metrics
```

## 🛠️ Beginner-Friendly Tools

### 1. **Portainer** (Docker Management UI)
```bash
# Install Portainer for easy Docker management
docker run -d \
  --name portainer \
  --restart unless-stopped \
  -p 9000:9000 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v portainer_data:/data \
  portainer/portainer-ce

# Access at http://your-server-ip:9000
```

### 2. **GitHub Desktop** (If you prefer GUI)
- Download from: https://desktop.github.com/
- Visual interface for Git operations

### 3. **VS Code Extensions**
```bash
# Recommended extensions:
- Docker
- Remote - SSH
- PostgreSQL
- Spring Boot Tools
- GitHub Actions
```

## 🎯 Learning Progression

### Week 1-2: Manual Deployment
- ✅ Setup server environment
- ✅ Manual build and deploy process
- ✅ Understanding Docker containers
- ✅ Basic monitoring

### Week 3-4: Basic CI/CD
- ✅ GitHub Actions setup
- ✅ Automated build process
- ✅ Simple deployment pipeline
- ✅ Error handling

### Week 5-6: Advanced Features
- ✅ Multiple environments (staging/prod)
- ✅ Database migrations
- ✅ Advanced monitoring
- ✅ Rollback strategies

## 🆘 Common Issues and Solutions

### Issue 1: Port Already in Use
```bash
# Find process using port
sudo lsof -i :5433
# Kill process if needed
sudo kill -9 <PID>
```

### Issue 2: Docker Permission Denied
```bash
# Add user to docker group
sudo usermod -aG docker $USER
# Logout and login again
```

### Issue 3: Database Connection Failed
```bash
# Check if container is running
docker ps | grep postgres
# Check logs
docker logs postgres_test
```

### Issue 4: Memory Issues
```bash
# Check system memory
free -h
# Monitor Docker containers
docker stats
```

## 📚 Learning Resources

### Free Resources:
- **GitHub Actions Docs**: https://docs.github.com/en/actions
- **Docker Tutorial**: https://www.docker.com/101-tutorial
- **PostgreSQL Tutorial**: https://www.postgresqltutorial.com/
- **Spring Boot Guides**: https://spring.io/guides

### Hands-on Practice:
- Start with manual deployment
- Gradually automate each step
- Experiment with different configurations
- Monitor and troubleshoot issues

---

*This guide provides a gentle introduction to CI/CD concepts using your own server. Take it step by step and don't hesitate to ask questions!*