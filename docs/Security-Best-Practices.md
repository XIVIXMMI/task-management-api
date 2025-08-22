# Security Best Practices for CI/CD

## 🚨 Critical Security Fix

**ISSUE IDENTIFIED**: Multiple configuration files contained hardcoded sensitive information including:
- Database passwords
- JWT secret keys  
- Server credentials
- API keys

**STATUS**: ✅ **FIXED** - All credentials moved to secure environment variables.

## 🔒 Secrets Management

### 1. GitHub Repository Secrets

Go to your GitHub repository → **Settings** → **Secrets and Variables** → **Actions**

#### Required Secrets:
```bash
# Database Secrets
TEST_DB_USER=test_user
TEST_DB_PASSWORD=generate_secure_password_here

# JWT Configuration
JWT_SECRET_KEY=use_256_bit_key_generator_online
JWT_ISSUER=task-management-prod

# Server Deployment  
SERVER_HOST=your.server.ip
SERVER_USER=deploy_user
SERVER_SSH_KEY=paste_private_key_content_here

# Docker Registry (if using private)
DOCKER_USERNAME=your_docker_username
DOCKER_PASSWORD=your_docker_password

# Notifications
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/YOUR/WEBHOOK
```

### 2. Environment Variables (.env)

#### Local Development:
```bash
# Copy .env.example to .env
cp .env.example .env

# Fill in your actual values (NEVER commit .env!)
nano .env
```

#### Production Server:
```bash
# On your server, create secure .env
sudo nano /opt/task-management/.env

# Set proper permissions
sudo chown root:docker /opt/task-management/.env
sudo chmod 640 /opt/task-management/.env
```

### 3. Application Properties Security

#### Create secure test configuration:
```yaml
# src/main/resources/application-test.yml
spring:
  datasource:
    url: ${TEST_DB_URL:jdbc:postgresql://localhost:5433/task_management_test}
    username: ${TEST_DB_USERNAME:test_user}
    password: ${TEST_DB_PASSWORD}
    
jwt:
  secret-key: ${JWT_SECRET_KEY}
  issuer: ${JWT_ISSUER:task-management-test}
  expiration-minute: ${JWT_EXPIRATION_MINUTE:60}
```

## 🛡️ Security Checklist

### ✅ Files Protected:
- [x] `.env` files in .gitignore
- [x] `application-prod.yml` in .gitignore  
- [x] SSH keys and certificates excluded
- [x] Database dumps excluded
- [x] Log files with sensitive data excluded

### ✅ Credentials Secured:
- [x] Database passwords → GitHub Secrets
- [x] JWT secret keys → Environment variables
- [x] SSH keys → Repository secrets
- [x] API keys → Secure storage

### ✅ CI/CD Pipeline Security:
- [x] No hardcoded passwords in workflows
- [x] Environment variables properly referenced
- [x] Secrets masked in logs
- [x] Minimal permissions configured

## 🔐 How to Generate Secure Secrets

### 1. Database Passwords
```bash
# Generate 32-character secure password
openssl rand -base64 32
```

### 2. JWT Secret Keys (256-bit)
```bash
# Generate 256-bit JWT secret
openssl rand -base64 64
```

### 3. SSH Key Pair
```bash
# Generate SSH key for deployment
ssh-keygen -t rsa -b 4096 -c "deploy-key" -f ~/.ssh/deploy_key

# Public key (add to server authorized_keys)
cat ~/.ssh/deploy_key.pub

# Private key (add to GitHub secrets)
cat ~/.ssh/deploy_key
```

## 🚫 What NEVER to Commit

### ❌ Never commit these files:
```bash
.env*                    # Environment variables
application-prod.yml     # Production config
docker-compose.override.yml
secrets.yml
*.key, *.pem            # SSH keys, certificates
database_backup*        # Database dumps
*.log                   # Log files
config/secrets.*        # Any secrets config
```

### ❌ Never hardcode in source code:
```java
// BAD - Never do this!
String password = "mypassword123";
String apiKey = "sk-1234567890abcdef";
String dbUrl = "postgresql://user:password@host:5432/db";

// GOOD - Use environment variables
@Value("${DB_PASSWORD}")
private String password;

@Value("${API_KEY}")  
private String apiKey;

@Value("${DB_URL}")
private String dbUrl;
```

## 🔍 Security Scanning

### 1. Pre-commit Hooks
```bash
# Install git-secrets to prevent committing secrets
git clone https://github.com/awslabs/git-secrets.git
cd git-secrets && make install

# Setup in your repo
git secrets --install
git secrets --register-aws
```

### 2. Regular Security Audits
```bash
# Check for hardcoded secrets
grep -r "password\|secret\|key" --include="*.java" --include="*.yml" src/

# Maven security audit  
mvn org.owasp:dependency-check-maven:check
```

### 3. Docker Image Scanning
```bash
# Scan Docker images for vulnerabilities
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  -v $HOME/Library/Caches:/root/.cache/ \
  aquasec/trivy task-management:latest
```

## 🛠️ Secure Configuration Examples

### 1. Production Docker Compose
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    image: task-management:latest
    environment:
      # Use environment variables, not hardcoded values
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
      - DB_URL=${DB_URL}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET_KEY=${JWT_SECRET_KEY}
    env_file:
      - .env  # Load from secure .env file
```

### 2. Kubernetes Secrets
```yaml
# k8s-secret.yml
apiVersion: v1
kind: Secret
metadata:
  name: task-management-secrets
type: Opaque
stringData:
  db-password: ${DB_PASSWORD}
  jwt-secret: ${JWT_SECRET_KEY}
```

### 3. GitHub Actions Security
```yaml
# .github/workflows/secure-deploy.yml
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - name: Deploy with secrets
      env:
        DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
        JWT_SECRET: ${{ secrets.JWT_SECRET_KEY }}
      run: |
        # Secrets are automatically masked in logs
        echo "Deploying with secure configuration..."
```

## 🔄 Secret Rotation Schedule

### Monthly:
- [ ] Rotate database passwords
- [ ] Update JWT secret keys
- [ ] Review access permissions

### Quarterly: 
- [ ] Rotate SSH keys
- [ ] Audit user access
- [ ] Update SSL certificates

### Annually:
- [ ] Complete security audit
- [ ] Update encryption standards
- [ ] Review backup encryption

## 🆘 Security Incident Response

### If secrets are accidentally committed:

1. **Immediate Actions:**
   ```bash
   # Change all affected passwords immediately
   # Revoke and regenerate API keys
   # Rotate JWT secrets
   ```

2. **Clean Git History:**
   ```bash
   # Remove secrets from git history
   git filter-branch --force --index-filter \
   'git rm --cached --ignore-unmatch path/to/sensitive/file' \
   --prune-empty --tag-name-filter cat -- --all
   
   # Force push (⚠️ dangerous - coordinate with team)
   git push origin --force --all
   ```

3. **Notify Team:**
   - Inform all team members
   - Update documentation  
   - Review security procedures

## 📞 Security Resources

### Tools:
- **git-secrets**: Prevent secret commits
- **OWASP Dependency Check**: Vulnerability scanning
- **Trivy**: Container security scanning
- **Vault**: Enterprise secret management

### Learning:
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [GitHub Security Best Practices](https://docs.github.com/en/actions/security-guides)
- [Docker Security](https://docs.docker.com/engine/security/)

---

**Remember**: Security is not a one-time setup - it's an ongoing process! 🔐