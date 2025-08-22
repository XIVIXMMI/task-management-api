# 🧪 Swagger Testing Commands

## 📋 Quick Copy-Paste Values for Swagger UI

### 🔍 GET /api/v1/task (Filter Tasks)

#### Test 1: Basic Pagination
```
page: 0
size: 10
sortBy: createdAt
sortDirection: desc
```

#### Test 2: Filter by Status
```
status: pending
page: 0
size: 5
sortBy: title
sortDirection: asc
```

#### Test 3: Filter by Priority
```
priority: high
page: 0
size: 10
sortBy: dueDate
sortDirection: asc
```

#### Test 4: Date Range Filter
```
dueDateFrom: 2025-07-30T00:00:00
dueDateTo: 2025-08-15T23:59:59
page: 0
size: 10
sortBy: dueDate
sortDirection: asc
```

#### Test 5: Multiple Filters
```
status: in_progress
priority: high
categoryId: 1
page: 0
size: 5
sortBy: progress
sortDirection: desc
```

---

### 🔍 GET /api/v1/task/search (Search Tasks)

#### Test 1: Basic Search
```
keyword: docker
page: 0
size: 10
sortBy: createdAt
sortDirection: desc
```

#### Test 2: Search + Filter
```
keyword: api
status: in_progress
page: 0
size: 10
sortBy: createdAt
sortDirection: desc
```

#### Test 3: Search Bug Tasks
```
keyword: bug
priority: urgent
page: 0
size: 5
sortBy: dueDate
sortDirection: asc
```

#### Test 4: Search Learning Tasks
```
keyword: learn
status: pending
page: 0
size: 20
sortBy: dueDate
sortDirection: asc
```

#### Test 5: Search Implementation Tasks
```
keyword: implement
status: in_progress
page: 0
size: 15
sortBy: progress
sortDirection: desc
```

---

### 🔄 PUT /api/v1/task/{taskId} (Update Task)

#### Complete Task Update JSON:
```json
{
  "title": "Implement user authentication system",
  "description": "Design and implement JWT-based authentication with role-based access control. Include login, logout, password reset functionality and session management.",
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
}
```

---

### 🔄 PATCH /api/v1/task/{taskId}/status (Update Status)

#### Status Values to Test:
```
pending
in_progress
completed
cancelled
on_hold
```

---

### 🔄 PATCH /api/v1/task/{taskId}/progress (Update Progress)

#### Progress Values to Test:
```
0
25
50
75
100
```

---

### 📝 POST /api/v1/task/create (Create Task)

#### Simple Task Creation JSON:
```json
{
  "title": "Learn Docker fundamentals",
  "description": "Complete Docker course and hands-on exercises",
  "dueDate": "2025-08-10T23:59:00",
  "startDate": "2025-07-31T08:00:00",
  "priority": "medium",
  "estimatedHours": 16.0,
  "assignedToId": 3,
  "categoryId": 1,
  "workspaceId": 2
}
```

---

## 🎯 Testing Workflow

### Step 1: Authentication
1. Login to get JWT token
2. Copy the token
3. Use "Authorize" button in Swagger
4. Enter: `Bearer YOUR_TOKEN_HERE`

### Step 2: Create Test Data
1. Use POST /api/v1/task/create
2. Create 3-5 tasks with different statuses/priorities
3. Note the task IDs returned

### Step 3: Test Filtering
1. Start with basic pagination
2. Add one filter at a time
3. Test different combinations

### Step 4: Test Searching
1. Search for keywords from your created tasks
2. Combine search with filters
3. Test edge cases (no results)

### Step 5: Test Updates
1. Use task IDs from step 2
2. Test complete updates (PUT)
3. Test partial updates (PATCH)

---

## 🚨 Common Test Scenarios

### Scenario 1: Find High Priority Tasks
```
priority: high
sortBy: dueDate
sortDirection: asc
```

### Scenario 2: Search for Bugs
```
keyword: bug
priority: urgent
status: pending
```

### Scenario 3: Find Overdue Tasks
```
isOverdue: true
sortBy: dueDate
sortDirection: asc
```

### Scenario 4: Search Learning Tasks
```
keyword: learn
status: pending
sortBy: createdAt
sortDirection: desc
```

---

*Copy and paste these values directly into Swagger UI for quick testing!*