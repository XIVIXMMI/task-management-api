# Database Auto-Migration Setup

## 🎯 Current Status
Your app currently uses `ddl-auto: none` with manual `init-db.sql`, which requires manual database initialization.

## 🚀 Auto-Migration Options

### **Option 1: Simple Hibernate DDL (Quick Setup)**

#### Advantages:
- ✅ Zero configuration needed
- ✅ Automatically creates/updates schema
- ✅ Works with existing JPA entities

#### Disadvantages:
- ⚠️ Limited control over migrations
- ⚠️ Can be unpredictable in production
- ⚠️ No rollback capability

#### Setup:
```yaml
# src/main/resources/application-prod.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Change from 'none' to 'update'
    show-sql: false  # Set to true for debugging
```

### **Option 2: Flyway Migration (Recommended)**

#### Advantages:
- ✅ Version-controlled migrations
- ✅ Production-safe
- ✅ Rollback support
- ✅ Team collaboration friendly

#### Setup Steps:

1. **Add Flyway Dependency:**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

2. **Configure Flyway:**
```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
  jpa:
    hibernate:
      ddl-auto: validate  # Ensure Hibernate doesn't modify schema
```

3. **Convert init-db.sql to Flyway Migration:**
```bash
# Create migration directory
mkdir -p src/main/resources/db/migration

# Move and rename init-db.sql
cp init-db.sql src/main/resources/db/migration/V1__Initial_schema.sql
```

4. **Future Migrations:**
```sql
-- src/main/resources/db/migration/V2__Add_user_profiles.sql
ALTER TABLE users ADD COLUMN profile_image VARCHAR(255);

-- src/main/resources/db/migration/V3__Create_notifications.sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### **Option 3: Keep Manual Migration (Current)**
- ✅ Full control over database schema
- ✅ Explicit and predictable
- ⚠️ Requires manual coordination
- ⚠️ More CI/CD complexity

## 🎯 **Recommended Approach for Your Setup**

### **For Development: Hibernate DDL**
```yaml
# application-dev.yml (local development)
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### **For Production: Flyway**
```yaml
# application-prod.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  jpa:
    hibernate:
      ddl-auto: validate
```

## 🔧 **Implementation Steps**

### Step 1: Add Flyway to Project
```xml
<!-- pom.xml - add this dependency -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### Step 2: Create Migration Structure
```bash
# Create migration directory
mkdir -p src/main/resources/db/migration

# Convert your init-db.sql
cp init-db.sql src/main/resources/db/migration/V1__Initial_schema.sql
```

### Step 3: Update Configuration
```yaml
# src/main/resources/application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1
  jpa:
    hibernate:
      ddl-auto: validate  # Changed from 'none'
```

### Step 4: Update CI/CD Pipeline
```yaml
# Remove manual database initialization from CI/CD
# Flyway will handle it automatically

# In .github/workflows/tailscale-deploy.yml, remove these lines:
# PGPASSWORD=xxx psql -h postgres -U xxx -d xxx -f init-db.sql
```

### Step 5: Test Migration
```bash
# Local testing
mvn clean spring-boot:run

# Check Flyway executed
# Look for logs: "Flyway Community Edition x.x.x by Redgate"
# Should see: "Successfully applied 1 migration"
```

## 🚀 **Migration Workflow**

### Adding New Features:
```bash
# 1. Create new migration file
touch src/main/resources/db/migration/V2__Add_task_categories.sql

# 2. Add SQL changes
echo "ALTER TABLE tasks ADD COLUMN category_id BIGINT;" > src/main/resources/db/migration/V2__Add_task_categories.sql

# 3. Commit and deploy
git add .
git commit -m "feat: add task categories migration"
git push origin main

# 4. CI/CD automatically applies migration
```

### Migration File Naming:
```
V1__Initial_schema.sql           ✅ Correct
V2__Add_user_profiles.sql        ✅ Correct  
V3__Update_task_status.sql       ✅ Correct
V1.1__Hotfix_users.sql          ✅ Correct (hotfix)
V001__Initial.sql               ❌ Use V1 instead
migration_001.sql               ❌ Wrong format
```

## 🔍 **Monitoring Migrations**

### Check Migration Status:
```sql
-- Flyway creates this table automatically
SELECT * FROM flyway_schema_history ORDER BY version;
```

### Application Logs:
```bash
# Look for these log messages
INFO  FlywayAutoConfiguration : Flyway available: true
INFO  Flyway : Successfully applied 1 migration to schema "public"
```

## 🆘 **Troubleshooting**

### Common Issues:

#### Migration Failed:
```bash
# Check flyway_schema_history table
SELECT * FROM flyway_schema_history WHERE success = false;

# Repair failed migration
mvn flyway:repair -Dflyway.url=jdbc:postgresql://localhost:5432/task_management
```

#### Schema Validation Failed:
```bash
# Hibernate validation vs actual schema mismatch
# Solution: Update entity annotations or create migration
```

#### Baseline Issues:
```bash
# If you have existing database with data:
mvn flyway:baseline -Dflyway.baselineVersion=1
```

## 📊 **Comparison: Manual vs Auto-Migration**

| Aspect | Manual (init-db.sql) | Auto (Flyway) | Auto (Hibernate DDL) |
|--------|---------------------|---------------|---------------------|
| **Setup Complexity** | Simple | Medium | Very Simple |
| **Production Safety** | High | High | Medium |
| **Version Control** | Manual | Automatic | None |
| **Team Collaboration** | Complex | Easy | Medium |
| **Rollback Support** | Manual | Built-in | Limited |
| **CI/CD Integration** | Custom | Seamless | Seamless |

## 🎯 **Recommendation**

**For your project, I recommend:**

1. **Short term:** Switch to Flyway with your existing `init-db.sql` as V1 migration
2. **Medium term:** Add new features as versioned migrations (V2, V3, etc.)
3. **Long term:** Consider Liquibase for more complex enterprise needs

**Implementation priority:**
1. ✅ Add Flyway dependency to pom.xml
2. ✅ Move init-db.sql to db/migration/V1__Initial_schema.sql  
3. ✅ Update application.yml with Flyway config
4. ✅ Test locally before deploying
5. ✅ Update CI/CD to remove manual database initialization

This gives you the best of both worlds: control + automation! 🚀