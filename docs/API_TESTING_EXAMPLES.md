# 🚀 Task Management API Testing Examples

## 📋 Table of Contents
- [Task Filter Examples](#task-filter-examples)
- [Task Search Examples](#task-search-examples)
- [Sample Task Data](#sample-task-data)
- [Quick Reference](#quick-reference)

---

## 🔍 Task Filter Examples
**Endpoint:** `GET /api/v1/task`

### Basic Examples
```json
// Example 1: Simple pagination
{
  "page": 0,
  "size": 10,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}

// Example 2: Filter by status
{
  "status": "pending",
  "page": 0,
  "size": 5,
  "sortBy": "title",
  "sortDirection": "asc"
}

// Example 3: Filter by priority
{
  "priority": "high",
  "page": 0,
  "size": 10,
  "sortBy": "dueDate",
  "sortDirection": "asc"
}

// Example 4: Filter overdue tasks
{
  "isOverdue": true,
  "page": 0,
  "size": 10,
  "sortBy": "dueDate",
  "sortDirection": "asc"
}
```

### Advanced Examples
```json
// Example 5: Date range filter
{
  "dueDateFrom": "2025-07-30T00:00:00",
  "dueDateTo": "2025-08-15T23:59:59",
  "page": 0,
  "size": 10,
  "sortBy": "dueDate",
  "sortDirection": "asc"
}

// Example 6: Multiple filters combined
{
  "status": "in_progress",
  "priority": "high",
  "categoryId": 1,
  "page": 0,
  "size": 5,
  "sortBy": "progress",
  "sortDirection": "desc"
}

// Example 7: Workspace filter
{
  "workspaceId": 1,
  "status": "pending",
  "page": 0,
  "size": 10,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

---

## 🔍 Task Search Examples
**Endpoint:** `GET /api/v1/task/search`

### Basic Search Examples
```json
// Example 1: Basic keyword search
{
  "keyword": "docker",
  "page": 0,
  "size": 10,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}

// Example 2: Search task titles
{
  "keyword": "authentication",
  "page": 0,
  "size": 5,
  "sortBy": "title",
  "sortDirection": "asc"
}

// Example 3: Search in descriptions
{
  "keyword": "bug",
  "page": 0,
  "size": 10,
  "sortBy": "priority",
  "sortDirection": "desc"
}

// Example 4: Technology search
{
  "keyword": "spring",
  "page": 0,
  "size": 20,
  "sortBy": "dueDate",
  "sortDirection": "asc"
}
```

### Advanced Search Examples
```json
// Example 5: Search + status filter
{
  "keyword": "api",
  "status": "in_progress",
  "page": 0,
  "size": 10,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}

// Example 6: Search + priority filter
{
  "keyword": "fix",
  "priority": "urgent",
  "page": 0,
  "size": 5,
  "sortBy": "dueDate",
  "sortDirection": "asc"
}

// Example 7: Search + date range
{
  "keyword": "meeting",
  "dueDateFrom": "2025-08-01T00:00:00",
  "dueDateTo": "2025-08-31T23:59:59",
  "page": 0,
  "size": 10,
  "sortBy": "dueDate",
  "sortDirection": "asc"
}

// Example 8: Search + multiple filters
{
  "keyword": "database",
  "status": "pending",
  "priority": "high",
  "categoryId": 2,
  "page": 0,
  "size": 10,
  "sortBy": "title",
  "sortDirection": "asc"
}
```

---

## 📝 Sample Task Data

### Task Update Examples
```json
// Complete task update example
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

// Learning task example
{
  "title": "Complete Docker fundamentals course",
  "description": "Learn Docker basics including containers, images, volumes, and networking. Complete hands-on exercises and build a sample application.",
  "dueDate": "2025-08-10T23:59:00.000Z",
  "startDate": "2025-07-31T08:00:00.000Z",
  "priority": "medium",
  "status": "pending",
  "estimatedHours": 16.0,
  "actualHours": 0.0,
  "progress": 0,
  "categoryId": 1,
  "assignedToId": 3,
  "workspaceId": 2,
  "sortOrder": 2,
  "isRecurring": false,
  "recurrencePattern": null,
  "metadata": {
    "course_provider": "Docker Academy",
    "skill_level": "beginner",
    "certification": "yes"
  }
}

// Bug fix task example
{
  "title": "Fix memory leak in user session management",
  "description": "Investigate and resolve memory leak occurring in user session handling. Memory usage increases over time causing performance degradation.",
  "dueDate": "2025-08-02T18:00:00.000Z",
  "startDate": "2025-07-30T14:30:00.000Z",
  "priority": "urgent",
  "status": "in_progress",
  "estimatedHours": 6.0,
  "actualHours": 3.5,
  "progress": 60,
  "categoryId": 4,
  "assignedToId": 2,
  "workspaceId": 1,
  "sortOrder": 0,
  "isRecurring": false,
  "recurrencePattern": null,
  "metadata": {
    "bug_severity": "high",
    "affected_users": "all",
    "environment": "production"
  }
}
```

---

## 🎯 Quick Reference

### Status Options
- `pending`
- `in_progress`
- `completed`
- `cancelled`
- `on_hold`

### Priority Options
- `low`
- `medium`
- `high`
- `urgent`

### Sort By Options
- `createdAt`
- `title`
- `dueDate`
- `priority`
- `status`
- `progress`

### Sort Direction
- `asc`
- `desc`

### Common Search Keywords
**Technical:** `api`, `database`, `authentication`, `docker`, `spring`, `security`, `frontend`, `backend`

**Actions:** `implement`, `fix`, `create`, `update`, `design`, `test`, `deploy`, `review`

**Types:** `bug`, `feature`, `meeting`, `documentation`, `refactor`, `optimization`

---

## 🧪 Testing Tips

1. **Start Simple**: Begin with basic pagination, then add filters
2. **One at a Time**: Add one filter at a time to see the difference
3. **Test Combinations**: Mix different filters for specific results
4. **Check Edge Cases**: Try empty results, invalid values
5. **Test Sorting**: Try different sort fields and directions

---

*Last updated: 2025-07-30*