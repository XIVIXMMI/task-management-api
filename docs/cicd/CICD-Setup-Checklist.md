# ✅ **GitHub Actions CI/CD với Tailscale - Setup Checklist**

## 🎯 **Overview**
This checklist guides you through setting up automated CI/CD from GitHub to your Ubuntu server using Tailscale for secure networking.

**Estimated Total Time: ~90 minutes**

---

## 🎯 **Phase 1: Setup Tailscale (30 phút)**

### **1.1 Install Tailscale trên Server**
```bash
# SSH vào server trước
ssh your-username@your-server-ip

# Install Tailscale
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up

# ✅ Check: Copy Tailscale IP hiển thị (dạng 100.x.x.x)
sudo tailscale ip -4
```

**Checklist:**
- [ ] SSH successfully into your Ubuntu server
- [ ] Install Tailscale using the official script
- [ ] Run `sudo tailscale up` and complete authentication in browser
- [ ] **IMPORTANT: Write down your server's Tailscale IP** (100.x.x.x format)
- [ ] Test connectivity: `ping 8.8.8.8` from server (verify internet works)

### **1.2 Install Tailscale trên máy local**
```bash
# MacOS
brew install tailscale
# Then: System Preferences → Tailscale → Login

# Windows: 
# Download from https://tailscale.com/download/windows
# Install and login with same account

# Linux:
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up
```

**Checklist:**
- [ ] Install Tailscale on your development machine
- [ ] Login with the same Tailscale account as server
- [ ] Verify both devices appear in Tailscale admin console
- [ ] **Test SSH via Tailscale:** `ssh username@TAILSCALE_SERVER_IP`
- [ ] Verify you can reach server without public IP

---

## 🔐 **Phase 2: Setup Tailscale OAuth (15 phút)**

### **2.1 Create OAuth Application**
1. Go to: https://login.tailscale.com/admin/settings/oauth
2. Click **"Generate OAuth client"**
3. Fill in details:
   - **Description:** `GitHub Actions CI/CD`
   - **Tags:** `tag:ci`
4. Click **Generate client**
5. **IMPORTANT:** Copy both Client ID and Client Secret immediately

**Checklist:**
- [ ] Navigate to Tailscale OAuth settings
- [ ] Create new OAuth client with description "GitHub Actions CI/CD"
- [ ] Set tags to `tag:ci`
- [ ] **Copy Client ID (starts with tsoc-)**
- [ ] **Copy Client Secret (starts with tsocs-)**
- [ ] Store both values securely (you'll add them to GitHub secrets later)

### **2.2 Test OAuth (Optional but Recommended)**
```bash
# Test OAuth credentials work
curl -u "CLIENT_ID:CLIENT_SECRET" \
  https://api.tailscale.com/api/v2/tailnet/-/devices
```

**Checklist:**
- [ ] Replace CLIENT_ID and CLIENT_SECRET with your actual values
- [ ] Run curl command - should return JSON with your devices
- [ ] If it fails, regenerate OAuth credentials

---

## 🏗️ **Phase 3: Prepare Server Environment (20 phút)**

### **3.1 Create Deployment Directory Structure**
```bash
# SSH into your server via Tailscale IP
ssh username@TAILSCALE_SERVER_IP

# Create deployment directories
sudo mkdir -p /opt/task-management/{config,logs,data}
cd /opt/task-management

# Set proper ownership (replace 'username' with your actual username)
sudo chown -R $USER:$USER /opt/task-management

# Verify directory structure
ls -la /opt/task-management/
```

**Checklist:**
- [ ] SSH into server using Tailscale IP
- [ ] Create `/opt/task-management/` directory structure
- [ ] Set proper ownership with your username
- [ ] Verify you can write to the directory: `touch /opt/task-management/test.txt`

### **3.2 Upload Required Files to Server**
```bash
# From your local machine, copy required files to server
scp init-db.sql username@TAILSCALE_SERVER_IP:/opt/task-management/
scp docker-compose.yml username@TAILSCALE_SERVER_IP:/opt/task-management/docker-compose.prod.yml

# SSH back into server and verify files
ssh username@TAILSCALE_SERVER_IP
ls -la /opt/task-management/
```

**Checklist:**
- [ ] Copy `init-db.sql` from local to server
- [ ] Copy and rename `docker-compose.yml` to `docker-compose.prod.yml` on server
- [ ] Verify both files exist on server in `/opt/task-management/`
- [ ] Check file permissions are readable

### **3.3 Create Secure Environment File**
```bash
# On server, create .env file with production values
cd /opt/task-management
nano .env

# Add the following content (replace with your actual secure values):
DB_URL=jdbc:postgresql://db:5432/task_management
DB_USERNAME=postgres
DB_PASSWORD=GENERATE_SECURE_PASSWORD_HERE
REDIS_HOST=redis
REDIS_PORT=6379
JWT_SECRET_KEY=GENERATE_256_BIT_KEY_HERE
JWT_ISSUER=task-management-prod
JWT_EXPIRATION_MINUTE=1440
SPRING_PROFILES_ACTIVE=prod

# Secure the file
chmod 600 .env
```

**Generate Secure Values:**
```bash
# Generate secure database password (32 characters)
openssl rand -base64 32

# Generate JWT secret key (256-bit)
openssl rand -base64 64
```

**Checklist:**
- [ ] Create `.env` file in `/opt/task-management/`
- [ ] Generate secure password for `DB_PASSWORD`
- [ ] Generate 256-bit secret for `JWT_SECRET_KEY`
- [ ] Set file permissions to 600: `chmod 600 .env`
- [ ] **IMPORTANT:** Never commit this .env file to git
- [ ] Verify file ownership: `ls -la .env`

### **3.4 Test Manual Deployment**
```bash
# On your local machine, build the application
mvn clean package -DskipTests -Dspring.profiles.active=prod
docker build -t task-management:latest .

# Save and transfer image to server
docker save task-management:latest > task-management.tar
scp task-management.tar username@TAILSCALE_SERVER_IP:/tmp/

# On server, load and test
ssh username@TAILSCALE_SERVER_IP
cd /opt/task-management
docker load < /tmp/task-management.tar

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Wait and test
sleep 30
curl http://localhost:8080/actuator/health
```

**Checklist:**
- [ ] Build application JAR successfully (no compilation errors)
- [ ] Build Docker image without errors
- [ ] Transfer image file to server
- [ ] Load Docker image on server successfully
- [ ] Start all services with docker-compose
- [ ] **Verify health check returns HTTP 200**: `{"status":"UP"}`
- [ ] Check all containers running: `docker ps`
- [ ] Stop services after test: `docker-compose -f docker-compose.prod.yml down`

---

## 🔑 **Phase 4: GitHub Repository Setup (10 phút)**

### **4.1 Generate SSH Key for CI/CD**
```bash
# On your local machine, generate new SSH key specifically for CI/CD
ssh-keygen -t rsa -b 4096 -C "github-actions-cicd" -f ~/.ssh/github-actions

# Add public key to server
ssh-copy-id -i ~/.ssh/github-actions username@TAILSCALE_SERVER_IP

# Test new key works
ssh -i ~/.ssh/github-actions username@TAILSCALE_SERVER_IP "whoami"

# Get private key content for GitHub secret
cat ~/.ssh/github-actions
# Copy this entire content (including -----BEGIN/END----- lines)
```

**Checklist:**
- [ ] Generate new SSH key pair specifically for GitHub Actions
- [ ] Add public key to server's authorized_keys
- [ ] Test SSH connection with new private key
- [ ] **Copy private key content** (you'll paste this into GitHub secrets)

### **4.2 Add GitHub Repository Secrets**
Navigate to: `Your GitHub Repository → Settings → Secrets and variables → Actions → New repository secret`

Add each of these secrets individually:

| Secret Name | Value | Example |
|-------------|-------|---------|
| `TAILSCALE_OAUTH_CLIENT_ID` | Your OAuth Client ID | `tsoc-1234567890abcdef` |
| `TAILSCALE_OAUTH_SECRET` | Your OAuth Client Secret | `tsocs-abcdef1234567890` |
| `TAILSCALE_SERVER_IP` | Server's Tailscale IP | `100.64.1.23` |
| `SERVER_USER` | Your server username | `ubuntu` or `your-username` |
| `SERVER_SSH_KEY` | Private key content | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `DB_PASSWORD` | Same as in .env file | Your secure database password |
| `JWT_SECRET_KEY` | Same as in .env file | Your 256-bit JWT secret |

**Checklist:**
- [ ] Add `TAILSCALE_OAUTH_CLIENT_ID` secret
- [ ] Add `TAILSCALE_OAUTH_SECRET` secret  
- [ ] Add `TAILSCALE_SERVER_IP` secret (your server's 100.x.x.x IP)
- [ ] Add `SERVER_USER` secret (your server username)
- [ ] Add `SERVER_SSH_KEY` secret (entire private key content)
- [ ] Add `DB_PASSWORD` secret (same value as in server's .env)
- [ ] Add `JWT_SECRET_KEY` secret (same value as in server's .env)
- [ ] Verify all 7 secrets are added to GitHub repository

### **4.3 Add Workflow File to Repository**
```bash
# On your local machine, make sure you have the workflow file
ls -la .github/workflows/tailscale-deploy.yml

# If missing, copy it from the generated file
# Commit and push the workflow
git add .github/workflows/tailscale-deploy.yml
git commit -m "feat: add Tailscale CI/CD workflow"
git push origin main
```

**Checklist:**
- [ ] Verify `.github/workflows/tailscale-deploy.yml` exists in your repository
- [ ] Review workflow file - ensure `TAILSCALE_SERVER_IP` references match your setup
- [ ] Commit workflow file to your repository
- [ ] Push to `main` branch
- [ ] Verify file appears in GitHub repository

---

## 🧪 **Phase 5: Test CI/CD Pipeline (15 phút)**

### **5.1 Trigger First Deployment**
```bash
# Make a small change to trigger the CI/CD pipeline
echo "# CI/CD Test - $(date)" >> README.md

# Commit and push to main branch
git add README.md
git commit -m "test: trigger CI/CD pipeline deployment"
git push origin main
```

**Checklist:**
- [ ] Make a small change to your repository
- [ ] Commit and push to `main` branch
- [ ] Navigate to GitHub → Actions tab
- [ ] Watch workflow execution in real-time
- [ ] Verify "Connect to Tailscale" step succeeds
- [ ] Verify all build steps complete successfully

### **5.2 Monitor Deployment Process**
Watch these steps in GitHub Actions:
1. **Checkout code** ✅
2. **Set up JDK 21** ✅
3. **Build application** ✅
4. **Build Docker image** ✅  
5. **Connect to Tailscale** ✅
6. **Deploy to server via Tailscale** ✅
7. **Deploy application** ✅

**Checklist:**
- [ ] All workflow steps show green checkmarks
- [ ] No red error messages in any step
- [ ] "Connect to Tailscale" step shows successful connection
- [ ] File transfer to server completes successfully
- [ ] Application deployment completes without errors
- [ ] Health check passes at end of workflow

### **5.3 Verify Deployment on Server**
```bash
# SSH into server and verify deployment
ssh username@TAILSCALE_SERVER_IP

# Check containers are running
docker ps

# Check application health
curl http://localhost:8080/actuator/health

# Check logs for any errors
docker logs task-management-app --tail 50

# Check database connection
docker logs task-management-db --tail 20
```

**Checklist:**
- [ ] SSH into server successfully
- [ ] All Docker containers are running (app, db, redis)
- [ ] Health endpoint returns `{"status":"UP"}`
- [ ] Application logs show no errors
- [ ] Database logs show successful initialization
- [ ] Application accessible via Tailscale IP: `curl http://TAILSCALE_IP:8080/actuator/health`

---

## 📊 **Phase 6: Verification & Testing (Ongoing)**

### **6.1 End-to-End Application Test**
```bash
# Test API endpoints
curl http://TAILSCALE_SERVER_IP:8080/actuator/health
curl http://TAILSCALE_SERVER_IP:8080/actuator/info

# If you have authentication endpoints, test those too
# curl -X POST http://TAILSCALE_SERVER_IP:8080/api/v1/auth/register -d '{"email":"test@test.com"}'
```

**Checklist:**
- [ ] Health endpoint responds correctly
- [ ] Info endpoint shows application details
- [ ] Application starts without errors in logs
- [ ] Database connectivity confirmed
- [ ] Redis connectivity confirmed (if used)

### **6.2 Security Verification**
```bash
# Verify secrets don't appear in GitHub Actions logs
# Check GitHub Actions → Your workflow → View logs

# Verify server is not publicly accessible (should timeout/fail)
curl http://YOUR_PUBLIC_SERVER_IP:8080/actuator/health

# Verify Tailscale access works
curl http://TAILSCALE_SERVER_IP:8080/actuator/health
```

**Checklist:**
- [ ] No passwords or secrets visible in GitHub Actions logs
- [ ] Application NOT accessible via public server IP
- [ ] Application IS accessible via Tailscale IP
- [ ] SSH key permissions correct (600): `ls -la ~/.ssh/github-actions`
- [ ] .env file not committed to git repository

### **6.3 Monitoring and Maintenance Setup**
```bash
# On server, set up log rotation
sudo nano /etc/logrotate.d/docker-containers

# Add monitoring script (optional)
nano /opt/task-management/health-check.sh
```

**Checklist:**
- [ ] Application logs rotate properly
- [ ] Docker containers auto-restart on failure
- [ ] Health checks can be monitored externally
- [ ] Backup strategy planned for database
- [ ] Server monitoring tools configured (optional)

---

## 📝 **Quick Reference Commands**

### **Development Commands**
```bash
# Check Tailscale status
tailscale status

# View your Tailscale IP
tailscale ip -4

# SSH via Tailscale
ssh username@$(tailscale ip -4 server-name)
```

### **Server Management**
```bash
# View application logs
docker logs task-management-app -f

# Restart services
cd /opt/task-management
docker-compose -f docker-compose.prod.yml restart

# Manual deployment
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### **Debugging Commands**
```bash
# Check container status
docker ps -a

# Check system resources
docker stats

# View detailed container logs
docker logs task-management-app --details --timestamps

# Check network connectivity
docker exec task-management-app ping db
```

---

## 🆘 **Troubleshooting Guide**

### **If Tailscale Connection Fails in GitHub Actions:**
```bash
# Check OAuth credentials in GitHub secrets
# Regenerate OAuth client if needed
# Verify tags configuration matches
```

### **If SSH Connection Fails:**
```bash
# Test SSH manually first
ssh -vvv -i ~/.ssh/github-actions username@TAILSCALE_IP

# Check authorized_keys on server
cat ~/.ssh/authorized_keys | grep github-actions

# Regenerate SSH key if needed
```

### **If Application Fails to Start:**
```bash
# Check environment variables
docker exec task-management-app env | grep -E "DB_|JWT_"

# Check database connection
docker exec task-management-app pg_isready -h db

# Review application logs
docker logs task-management-app --tail 100
```

### **If Health Check Fails:**
```bash
# Wait longer for startup
sleep 60 && curl localhost:8080/actuator/health

# Check if port is accessible
netstat -tlnp | grep :8080

# Verify Docker networking
docker network ls
docker network inspect task-management_app-network
```

---

## 🎯 **Success Criteria**

By the end of this checklist, you should have:

✅ **Secure Network**: Server only accessible via Tailscale, not public internet  
✅ **Automated CI/CD**: Code changes automatically deploy to server  
✅ **Health Monitoring**: Application health checks pass consistently  
✅ **Security**: No secrets in code, all credentials properly managed  
✅ **Reliability**: Containers auto-restart, services recover from failures  

---

## 📅 **Maintenance Schedule**

### **Weekly:**
- [ ] Review GitHub Actions workflow logs
- [ ] Check application health and performance
- [ ] Verify automatic deployments working

### **Monthly:**
- [ ] Rotate SSH keys and passwords
- [ ] Update Tailscale OAuth tokens if needed
- [ ] Review and update Docker images

### **Quarterly:**
- [ ] Full security audit of secrets and access
- [ ] Performance optimization review
- [ ] Backup and recovery testing

---

**🎉 Congratulations! You now have a secure, automated CI/CD pipeline using GitHub Actions and Tailscale!**

---

*Last updated: $(date)*
*Total estimated time: ~90 minutes*