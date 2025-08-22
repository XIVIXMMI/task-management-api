# 🌐 cURL Commands for API Testing

## 🔐 Authentication
```bash
# Login to get JWT token
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password"
  }'

# Save the token from response and use in subsequent requests
export JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 📋 Task Management Commands

### Create Task
```bash
curl -X POST "http://localhost:8080/api/v1/task/create" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn Docker fundamentals",
    "description": "Complete Docker course and hands-on exercises",
    "dueDate": "2025-08-10T23:59:00",
    "startDate": "2025-07-31T08:00:00",
    "priority": "medium",
    "estimatedHours": 16.0,
    "assignedToId": 3,
    "categoryId": 1,
    "workspaceId": 2
  }'
```

### Get Task by ID
```bash
curl -X GET "http://localhost:8080/api/v1/task/123" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Update Complete Task
```bash
curl -X PUT "http://localhost:8080/api/v1/task/123" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement user authentication system",
    "description": "Design and implement JWT-based authentication",
    "dueDate": "2025-08-15T17:00:00.000Z",
    "startDate": "2025-08-01T09:00:00.000Z",
    "priority": "high",
    "status": "in_progress",
    "estimatedHours": 24.5,
    "actualHours": 8.0,
    "progress": 35,
    "categoryId": 2,
    "assignedToId": 5,
    "workspaceId": 1,
    "sortOrder": 1,
    "isRecurring": false,
    "recurrencePattern": null,
    "metadata": {
      "technology": "Spring Security",
      "complexity": "medium",
      "team": "backend"
    }
  }'
```

### Update Task Status (PATCH)
```bash
curl -X PATCH "http://localhost:8080/api/v1/task/123/status?status=completed" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Update Task Progress (PATCH)
```bash
curl -X PATCH "http://localhost:8080/api/v1/task/123/progress?progress=75" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Delete Task
```bash
curl -X DELETE "http://localhost:8080/api/v1/task/123" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 🔍 Filter and Search Commands

### Get Tasks with Basic Filter
```bash
curl -X GET "http://localhost:8080/api/v1/task?page=0&size=10&sortBy=createdAt&sortDirection=desc" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Filter by Status
```bash
curl -X GET "http://localhost:8080/api/v1/task?status=pending&page=0&size=5" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Filter by Priority
```bash
curl -X GET "http://localhost:8080/api/v1/task?priority=high&page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Filter by Date Range
```bash
curl -X GET "http://localhost:8080/api/v1/task?dueDateFrom=2025-07-30T00:00:00&dueDateTo=2025-08-15T23:59:59&page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Multiple Filters
```bash
curl -X GET "http://localhost:8080/api/v1/task?status=in_progress&priority=high&categoryId=1&page=0&size=5" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Search Tasks
```bash
curl -X GET "http://localhost:8080/api/v1/task/search?keyword=docker&page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Search with Filters
```bash
curl -X GET "http://localhost:8080/api/v1/task/search?keyword=api&status=in_progress&page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Search Bug Tasks
```bash
curl -X GET "http://localhost:8080/api/v1/task/search?keyword=bug&priority=urgent&page=0&size=5" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 🔄 Batch Operations

### Update Multiple Tasks Status
```bash
curl -X PATCH "http://localhost:8080/api/v1/task/batch/status?status=completed" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3, 4, 5]'
```

### Delete Multiple Tasks
```bash
curl -X DELETE "http://localhost:8080/api/v1/task/batch" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

---

## 🧪 Testing Scenarios

### Scenario 1: Complete Task Workflow
```bash
# 1. Create a task
TASK_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/task/create" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task",
    "description": "Testing workflow",
    "dueDate": "2025-08-10T23:59:00",
    "startDate": "2025-07-31T08:00:00",
    "priority": "medium"
  }')

# Extract task ID (assuming jq is available)
TASK_ID=$(echo $TASK_RESPONSE | jq -r '.id')

# 2. Update task status
curl -X PATCH "http://localhost:8080/api/v1/task/$TASK_ID/status?status=in_progress" \
  -H "Authorization: Bearer $JWT_TOKEN"

# 3. Update progress
curl -X PATCH "http://localhost:8080/api/v1/task/$TASK_ID/progress?progress=50" \
  -H "Authorization: Bearer $JWT_TOKEN"

# 4. Complete the task
curl -X PATCH "http://localhost:8080/api/v1/task/$TASK_ID/status?status=completed" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Scenario 2: Search and Filter Testing
```bash
# Search for all tasks containing "test"
curl -X GET "http://localhost:8080/api/v1/task/search?keyword=test" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Filter high priority tasks
curl -X GET "http://localhost:8080/api/v1/task?priority=high" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Find overdue tasks
curl -X GET "http://localhost:8080/api/v1/task?isOverdue=true" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 🎯 Quick Test Commands

### Test Authentication
```bash
curl -X GET "http://localhost:8080/api/v1/task" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.'
```

### Test Error Handling
```bash
# Test with invalid task ID
curl -X GET "http://localhost:8080/api/v1/task/99999" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Test with invalid status
curl -X PATCH "http://localhost:8080/api/v1/task/1/status?status=invalid_status" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## 📝 Notes

- Replace `$JWT_TOKEN` with your actual JWT token
- Replace task IDs (123, 1, 2, etc.) with actual task IDs from your system
- Add `| jq '.'` at the end for pretty JSON formatting (requires jq)
- Use `-v` flag for verbose output: `curl -v ...`
- Use `-s` flag for silent mode: `curl -s ...`

---

*Save these commands in a script file for easy reuse!*