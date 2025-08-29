
-- Create the database (run this separately as superuser)
-- CREATE DATABASE task_management;
-- \c task_management;

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- CREATE SCHEMAS FOR MODULAR ORGANIZATION
-- =============================================
CREATE SCHEMA IF NOT EXISTS "user_mgmt";
CREATE SCHEMA IF NOT EXISTS "project";
CREATE SCHEMA IF NOT EXISTS "notification";
CREATE SCHEMA IF NOT EXISTS "collaboration";
CREATE SCHEMA IF NOT EXISTS "audit";
CREATE SCHEMA IF NOT EXISTS "analytics";

-- Set search path to include all schemas
SET search_path TO user_mgmt, project, notification, collaboration, audit, analytics, public;

-- =============================================
-- ENUMS AND TYPES (in public schema for global access)
-- =============================================
CREATE TYPE user_status AS ENUM ('online', 'offline', 'busy', 'away');
CREATE TYPE task_status AS ENUM ('pending', 'in_progress', 'completed', 'cancelled', 'on_hold');
CREATE TYPE task_priority AS ENUM ('low', 'medium', 'high', 'urgent');
CREATE TYPE repeat_interval AS ENUM ('none', 'daily', 'weekly', 'monthly', 'yearly');
CREATE TYPE notification_type AS ENUM ('email', 'push', 'sms');
CREATE TYPE action_type AS ENUM ('create', 'update', 'delete', 'complete', 'archive', 'restore');

-- =============================================
-- USER_MGMT SCHEMA - User Management Module
-- =============================================

-- User Roles
CREATE TABLE user_mgmt.roles (
    role_id SMALLINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    permissions JSONB, -- Store role permissions as JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_mgmt.roles IS 'System roles and permissions management';
COMMENT ON COLUMN user_mgmt.roles.permissions IS 'JSON object defining role permissions';

-- User Profiles
CREATE TABLE user_mgmt.profiles (
    profile_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) DEFAULT 'guest',
    middle_name VARCHAR(50),
    last_name VARCHAR(50),
    date_of_birth DATE,
    gender CHAR(1) CHECK (gender IN ('M', 'F', 'O', 'N')), -- M=Male, F=Female, O=Other, N=Not specified
    avatar_path VARCHAR(500),
    timezone VARCHAR(50) DEFAULT 'UTC',
    language VARCHAR(5) DEFAULT 'en',
    status user_status DEFAULT 'offline',
    last_login TIMESTAMP,
    last_activity TIMESTAMP,
    verified_at TIMESTAMP,
    deleted_at TIMESTAMP,
    auth_provider VARCHAR(30),
    auth_provider_id VARCHAR(100),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    session_id VARCHAR(255),
    login_attempts SMALLINT DEFAULT 0,
    locked_until TIMESTAMP,
    failed_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_mgmt.profiles IS 'User personal information and authentication details';

-- Main Users Table
CREATE TABLE user_mgmt.users (
    user_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    email_verified_at TIMESTAMP,
    mobile VARCHAR(20) UNIQUE,
    mobile_verified_at TIMESTAMP,
    password_hash VARCHAR(255) NOT NULL,
    role_id SMALLINT NOT NULL DEFAULT 1,
    profile_id BIGINT UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP, -- Soft delete
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES user_mgmt.roles(role_id),
    CONSTRAINT fk_users_profile FOREIGN KEY (profile_id) REFERENCES user_mgmt.profiles(profile_id)
);

COMMENT ON TABLE user_mgmt.users IS 'Core user accounts and authentication';

-- User Sessions (for session management)
CREATE TABLE user_mgmt.sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id) ON DELETE CASCADE
);

COMMENT ON TABLE user_mgmt.sessions IS 'Active user sessions tracking';

-- =============================================
-- PROJECT SCHEMA - Task and Project Management
-- =============================================

-- Workspaces/Projects (for team collaboration)
CREATE TABLE project.workspaces (
    workspace_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    is_personal BOOLEAN DEFAULT FALSE,
    settings JSONB DEFAULT '{}',
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workspaces_owner FOREIGN KEY (owner_id) REFERENCES user_mgmt.users(user_id)
);

COMMENT ON TABLE project.workspaces IS 'Workspaces for organizing tasks and team collaboration';

-- Workspace Members (for team collaboration)
CREATE TABLE project.workspace_members (
    workspace_id BIGINT,
    user_id BIGINT,
    role VARCHAR(20) DEFAULT 'member', -- owner, admin, member, viewer
    invited_by BIGINT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (workspace_id, user_id),
    CONSTRAINT fk_workspace_members_workspace FOREIGN KEY (workspace_id) REFERENCES project.workspaces(workspace_id) ON DELETE CASCADE,
    CONSTRAINT fk_workspace_members_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT fk_workspace_members_inviter FOREIGN KEY (invited_by) REFERENCES user_mgmt.users(user_id)
);

COMMENT ON TABLE project.workspace_members IS 'Users membership in workspaces';

-- Categories
CREATE TABLE project.categories (
    category_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#3498db', -- Hex color
    icon VARCHAR(50), -- For UI icons
    user_id BIGINT NOT NULL,
    workspace_id BIGINT,
    is_default BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT fk_categories_workspace FOREIGN KEY (workspace_id) REFERENCES project.workspaces(workspace_id)
);

COMMENT ON TABLE project.categories IS 'Task categories for organization';

-- Main Tasks Table
CREATE TABLE project.tasks (
    task_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP, -- Changed to TIMESTAMP for precise timing
    start_date TIMESTAMP,
    completed_at TIMESTAMP,
    priority task_priority DEFAULT 'medium',
    status task_status DEFAULT 'pending',
    estimated_hours DECIMAL(5,2),
    actual_hours DECIMAL(5,2),
    progress INTEGER DEFAULT 0 CHECK (progress >= 0 AND progress <= 100),
    category_id BIGINT,
    user_id BIGINT NOT NULL, -- Task owner
    assigned_to BIGINT, -- Can be different from owner
    workspace_id BIGINT,
    parent_task_id BIGINT, -- For task hierarchies
    sort_order INTEGER DEFAULT 0,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern JSONB, -- Store recurrence rules
    metadata JSONB DEFAULT '{}', -- Flexible field for additional data
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tasks_category FOREIGN KEY (category_id) REFERENCES project.categories(category_id),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT fk_tasks_assigned FOREIGN KEY (assigned_to) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT fk_tasks_workspace FOREIGN KEY (workspace_id) REFERENCES project.workspaces(workspace_id),
    CONSTRAINT fk_tasks_parent FOREIGN KEY (parent_task_id) REFERENCES project.tasks(task_id)
);

COMMENT ON TABLE project.tasks IS 'Main tasks table with full task information';

-- Tags
CREATE TABLE project.tags (
    tag_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) DEFAULT '#95a5a6',
    user_id BIGINT NOT NULL,
    workspace_id BIGINT,
    usage_count INTEGER DEFAULT 0,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT fk_tags_workspace FOREIGN KEY (workspace_id) REFERENCES project.workspaces(workspace_id),
    CONSTRAINT uk_tags_name_user UNIQUE (name, user_id, workspace_id)
);

COMMENT ON TABLE project.tags IS 'Tags for flexible task labeling';

-- Task-Tag Relationships
CREATE TABLE project.task_tags (
    task_id BIGINT,
    tag_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (task_id, tag_id),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) REFERENCES project.tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_task_tags_tag FOREIGN KEY (tag_id) REFERENCES project.tags(tag_id) ON DELETE CASCADE
);

COMMENT ON TABLE project.task_tags IS 'Many-to-many relationship between tasks and tags';

-- Subtasks
CREATE TABLE project.subtasks (
    subtask_id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    sort_order INTEGER DEFAULT 0,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_subtasks_task FOREIGN KEY (task_id) REFERENCES project.tasks(task_id) ON DELETE CASCADE
);

COMMENT ON TABLE project.subtasks IS 'Subtasks for breaking down main tasks';

-- =============================================
-- NOTIFICATION SCHEMA - Notification System
-- =============================================

-- Reminders
CREATE TABLE notification.reminders (
    reminder_id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    remind_at TIMESTAMP NOT NULL,
    repeat_interval repeat_interval DEFAULT 'none',
    notification_types notification_type[] DEFAULT ARRAY['push'], -- Array of notification types
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reminders_task FOREIGN KEY (task_id) REFERENCES project.tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_reminders_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id)
);

COMMENT ON TABLE notification.reminders IS 'Task reminders and scheduling';

-- Notifications
CREATE TABLE notification.notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    data JSONB DEFAULT '{}',
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id)
);

COMMENT ON TABLE notification.notifications IS 'User notifications';

-- Notification Templates
CREATE TABLE notification.templates (
    template_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type notification_type NOT NULL,
    subject_template TEXT,
    body_template TEXT NOT NULL,
    variables JSONB DEFAULT '[]', -- Available template variables
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE notification.templates IS 'Notification message templates';

-- =============================================
-- COLLABORATION SCHEMA - Team Collaboration
-- =============================================

-- Task Sharing/Collaboration
CREATE TABLE collaboration.task_collaborators (
    task_id BIGINT,
    user_id BIGINT,
    role VARCHAR(20) DEFAULT 'viewer', -- owner, editor, viewer
    invited_by BIGINT,
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_task_collaborators_task FOREIGN KEY (task_id) REFERENCES project.tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_task_collaborators_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT fk_task_collaborators_inviter FOREIGN KEY (invited_by) REFERENCES user_mgmt.users(user_id)
);

COMMENT ON TABLE collaboration.task_collaborators IS 'Task collaboration and sharing permissions';

-- Comments
CREATE TABLE collaboration.comments (
    comment_id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT, -- For threaded comments
    content TEXT NOT NULL,
    mentions BIGINT[], -- Array of user IDs mentioned
    attachments JSONB DEFAULT '[]', -- File attachments metadata
    is_edited BOOLEAN DEFAULT FALSE,
    edited_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES project.tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES collaboration.comments(comment_id)
);

COMMENT ON TABLE collaboration.comments IS 'Task comments and discussions';

-- File Attachments
CREATE TABLE collaboration.attachments (
    attachment_id BIGSERIAL PRIMARY KEY,
    task_id BIGINT,
    comment_id BIGINT,
    user_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attachments_task FOREIGN KEY (task_id) REFERENCES project.tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_attachments_comment FOREIGN KEY (comment_id) REFERENCES collaboration.comments(comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_attachments_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT chk_attachments_parent CHECK ((task_id IS NOT NULL) OR (comment_id IS NOT NULL))
);

COMMENT ON TABLE collaboration.attachments IS 'File attachments for tasks and comments';

-- Team Invitations
CREATE TABLE collaboration.invitations (
    invitation_id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    inviter_id BIGINT NOT NULL,
    invitee_email VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'member',
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,
    rejected_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invitations_workspace FOREIGN KEY (workspace_id) REFERENCES project.workspaces(workspace_id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_inviter FOREIGN KEY (inviter_id) REFERENCES user_mgmt.users(user_id)
);

COMMENT ON TABLE collaboration.invitations IS 'Workspace and team invitations';

-- =============================================
-- AUDIT SCHEMA - Audit Trail & Activity Tracking
-- =============================================

-- Activity Log
CREATE TABLE audit.activity_logs (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT,
    workspace_id BIGINT,
    action action_type NOT NULL,
    entity_type VARCHAR(50) NOT NULL, -- task, subtask, comment, etc.
    entity_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT fk_activity_logs_task FOREIGN KEY (task_id) REFERENCES project.tasks(task_id),
    CONSTRAINT fk_activity_logs_workspace FOREIGN KEY (workspace_id) REFERENCES project.workspaces(workspace_id)
);

COMMENT ON TABLE audit.activity_logs IS 'Complete audit trail of user actions';

-- Error Logs
CREATE TABLE audit.error_logs (
    error_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    error_type VARCHAR(100) NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    request_data JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_error_logs_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id)
);

COMMENT ON TABLE audit.error_logs IS 'Application error tracking';

-- =============================================
-- ANALYTICS SCHEMA - Performance & Analytics
-- =============================================

-- User Statistics (for dashboard/analytics)
CREATE TABLE analytics.user_statistics (
    stat_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    tasks_created INTEGER DEFAULT 0,
    tasks_completed INTEGER DEFAULT 0,
    total_hours_logged DECIMAL(5,2) DEFAULT 0,
    productivity_score DECIMAL(3,2), -- 0.00 to 1.00

    CONSTRAINT fk_user_statistics_user FOREIGN KEY (user_id) REFERENCES user_mgmt.users(user_id),
    CONSTRAINT uk_user_statistics UNIQUE (user_id, date)
);

COMMENT ON TABLE analytics.user_statistics IS 'Daily user productivity statistics';

-- Workspace Statistics
CREATE TABLE analytics.workspace_statistics (
    stat_id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    date DATE NOT NULL,
    total_tasks INTEGER DEFAULT 0,
    completed_tasks INTEGER DEFAULT 0,
    active_members INTEGER DEFAULT 0,
    total_comments INTEGER DEFAULT 0,

    CONSTRAINT fk_workspace_statistics_workspace FOREIGN KEY (workspace_id) REFERENCES project.workspaces(workspace_id),
    CONSTRAINT uk_workspace_statistics UNIQUE (workspace_id, date)
);

COMMENT ON TABLE analytics.workspace_statistics IS 'Daily workspace activity statistics';

-- Default roles
INSERT INTO user_mgmt.roles (role_id, name, description, permissions) VALUES
(1, 'ROLE_USER', 'Standard user with basic permissions', '{"tasks": ["create", "read", "update", "delete"], "workspaces": ["create", "join"]}'),
(2, 'ROLE_ADMIN', 'System administrator with full permissions', '{"*": ["*"]}'),
(3, 'ROLE_MANAGER', 'Team manager with extended permissions', '{"tasks": ["*"], "workspaces": ["*"], "users": ["read", "invite"]}');

-- Default notification templates
INSERT INTO notification.templates (name, type, subject_template, body_template, variables) VALUES
('task_reminder', 'email', 'Task Reminder: {{task_title}}', 'Hi {{user_name}},\n\nThis is a reminder that your task "{{task_title}}" is due on {{due_date}}.\n\nBest regards,\nTask Management Team', '["user_name", "task_title", "due_date"]'),
('task_assigned', 'push', 'New Task Assigned', 'You have been assigned a new task: {{task_title}}', '["task_title"]'),
('workspace_invitation', 'email', 'Invitation to join {{workspace_name}}', 'Hi,\n\nYou have been invited to join the workspace "{{workspace_name}}" by {{inviter_name}}.\n\nClick here to accept: {{invitation_link}}', '["workspace_name", "inviter_name", "invitation_link"]');

