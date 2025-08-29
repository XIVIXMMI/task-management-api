-- =============================================
-- INDEXES FOR PERFORMANCE
-- =============================================

-- User Management Schema Indexes
CREATE INDEX idx_users_email ON user_mgmt.users(email);
CREATE INDEX idx_users_username ON user_mgmt.users(username);
CREATE INDEX idx_users_uuid ON user_mgmt.users(uuid);
CREATE INDEX idx_users_active ON user_mgmt.users(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_profiles_status ON user_mgmt.profiles(status);
CREATE INDEX idx_sessions_user_id ON user_mgmt.sessions(user_id);
CREATE INDEX idx_sessions_expires ON user_mgmt.sessions(expires_at);

-- Project Schema Indexes
CREATE INDEX idx_tasks_user_id ON project.tasks(user_id);
CREATE INDEX idx_tasks_assigned_to ON project.tasks(assigned_to);
CREATE INDEX idx_tasks_status ON project.tasks(status);
CREATE INDEX idx_tasks_due_date ON project.tasks(due_date);
CREATE INDEX idx_tasks_priority ON project.tasks(priority);
CREATE INDEX idx_tasks_workspace ON project.tasks(workspace_id);
CREATE INDEX idx_tasks_category ON project.tasks(category_id);
CREATE INDEX idx_tasks_created_at ON project.tasks(created_at);
CREATE INDEX idx_tasks_active ON project.tasks(task_id) WHERE deleted_at IS NULL;

-- Composite indexes for common queries
CREATE INDEX idx_tasks_user_status ON project.tasks(user_id, status);
CREATE INDEX idx_tasks_user_due_date ON project.tasks(user_id, due_date);
CREATE INDEX idx_tasks_workspace_status ON project.tasks(workspace_id, status);

-- Collaboration Schema Indexes
CREATE INDEX idx_comments_task_id ON collaboration.comments(task_id);
CREATE INDEX idx_comments_user_id ON collaboration.comments(user_id);
CREATE INDEX idx_attachments_task_id ON collaboration.attachments(task_id);

-- Notification Schema Indexes
CREATE INDEX idx_notifications_user_unread ON notification.notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_reminders_remind_at ON notification.reminders(remind_at) WHERE is_active = TRUE;

-- Audit Schema Indexes
CREATE INDEX idx_activity_logs_user_id ON audit.activity_logs(user_id);
CREATE INDEX idx_activity_logs_task_id ON audit.activity_logs(task_id);
CREATE INDEX idx_activity_logs_created_at ON audit.activity_logs(created_at);
CREATE INDEX idx_activity_logs_entity ON audit.activity_logs(entity_type, entity_id);

-- Analytics Schema Indexes
CREATE INDEX idx_user_statistics_date ON analytics.user_statistics(date);
CREATE INDEX idx_workspace_statistics_date ON analytics.workspace_statistics(date);


-- =============================================
-- TRIGGERS FOR AUTOMATION
-- =============================================

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers to relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON user_mgmt.users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE ON user_mgmt.profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON project.tasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_subtasks_updated_at BEFORE UPDATE ON project.subtasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_comments_updated_at BEFORE UPDATE ON collaboration.comments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update tag usage count
CREATE OR REPLACE FUNCTION update_tag_usage_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE project.tags SET usage_count = usage_count + 1 WHERE tag_id = NEW.tag_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE project.tags SET usage_count = usage_count - 1 WHERE tag_id = OLD.tag_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tag_usage_trigger
    AFTER INSERT OR DELETE ON project.task_tags
    FOR EACH ROW EXECUTE FUNCTION update_tag_usage_count();

-- Active tasks view
CREATE VIEW project.active_tasks AS
SELECT
    t.*,
    c.name as category_name,
    c.color as category_color,
    u.username as owner_username,
    au.username as assigned_username,
    w.name as workspace_name
FROM project.tasks t
LEFT JOIN project.categories c ON t.category_id = c.category_id
LEFT JOIN user_mgmt.users u ON t.user_id = u.user_id
LEFT JOIN user_mgmt.users au ON t.assigned_to = au.user_id
LEFT JOIN project.workspaces w ON t.workspace_id = w.workspace_id
WHERE t.deleted_at IS NULL;

-- User task summary view
CREATE VIEW analytics.user_task_summary AS
SELECT
    u.user_id,
    u.username,
    COUNT(CASE WHEN t.status = 'pending' THEN 1 END) as pending_tasks,
    COUNT(CASE WHEN t.status = 'in_progress' THEN 1 END) as in_progress_tasks,
    COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks,
    COUNT(t.task_id) as total_tasks
FROM user_mgmt.users u
LEFT JOIN project.tasks t ON u.user_id = t.user_id AND t.deleted_at IS NULL
GROUP BY u.user_id, u.username;

-- Workspace overview
CREATE VIEW project.workspace_overview AS
SELECT
    w.*,
    u.username as owner_username,
    COUNT(DISTINCT wm.user_id) as member_count,
    COUNT(DISTINCT t.task_id) as task_count,
    COUNT(CASE WHEN t.status = 'completed' THEN 1 END) as completed_tasks
FROM project.workspaces w
LEFT JOIN user_mgmt.users u ON w.owner_id = u.user_id
LEFT JOIN project.workspace_members wm ON w.workspace_id = wm.workspace_id
LEFT JOIN project.tasks t ON w.workspace_id = t.workspace_id AND t.deleted_at IS NULL
WHERE w.deleted_at IS NULL
GROUP BY w.workspace_id, u.username;

-- =============================================
-- FUNCTIONS FOR COMMON OPERATIONS
-- =============================================

-- Function to get user's pending tasks
CREATE OR REPLACE FUNCTION project.get_user_pending_tasks(p_user_id BIGINT)
RETURNS TABLE (
    task_id BIGINT,
    title VARCHAR(255),
    due_date TIMESTAMP,
    priority task_priority,
    category_name VARCHAR(50)
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        t.task_id,
        t.title,
        t.due_date,
        t.priority,
        c.name as category_name
    FROM project.tasks t
    LEFT JOIN project.categories c ON t.category_id = c.category_id
    WHERE t.user_id = p_user_id
    AND t.status = 'pending'
    AND t.deleted_at IS NULL
    ORDER BY t.due_date ASC NULLS LAST, t.priority DESC;
END;
$$ LANGUAGE plpgsql;

-- Function to create a new workspace with default categories
CREATE OR REPLACE FUNCTION project.create_workspace_with_defaults(
    p_name VARCHAR(100),
    p_description TEXT,
    p_owner_id BIGINT
)
RETURNS BIGINT AS $$
DECLARE
    new_workspace_id BIGINT;
BEGIN
    -- Create workspace
    INSERT INTO project.workspaces (name, description, owner_id)
    VALUES (p_name, p_description, p_owner_id)
    RETURNING workspace_id INTO new_workspace_id;

    -- Add owner as workspace member
    INSERT INTO project.workspace_members (workspace_id, user_id, role, invited_by)
    VALUES (new_workspace_id, p_owner_id, 'owner', p_owner_id);

    -- Create default categories
    INSERT INTO project.categories (name, color, user_id, workspace_id, is_default) VALUES
    ('General', '#3498db', p_owner_id, new_workspace_id, true),
    ('Urgent', '#e74c3c', p_owner_id, new_workspace_id, true),
    ('Ideas', '#9b59b6', p_owner_id, new_workspace_id, true);

    RETURN new_workspace_id;
END;
$$ LANGUAGE plpgsql;
