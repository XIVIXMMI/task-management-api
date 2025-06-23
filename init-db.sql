
-----===== USER SCHEMA =====-----

----- USER PROFILE -----
CREATE TYPE user_status AS ENUM ('online', 'offline', 'busy', 'away');

CREATE TABLE user_profiles (
    usr_pf_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) DEFAULT 'guest',
    middle_name VARCHAR(50),
    last_name VARCHAR(50),
    date_of_birth DATE,
    gender char(1),
    avatar_path VARCHAR(200),
    status user_status DEFAULT 'offline',
    last_login TIMESTAMP,
    last_activity TIMESTAMP,
    verified_at TIMESTAMP,
    deleted_at TIMESTAMP,
    auth_provider VARCHAR(30),
    auth_provider_id VARCHAR(100),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    session_id VARCHAR(255),
    login_attempt SMALLINT DEFAULT 0,
    fail_login_at TIMESTAMP
);

COMMENT ON TABLE user_profiles IS 'Chứa thông tin cá nhân và trạng thái hoạt động của người dùng';
COMMENT ON COLUMN user_profiles.usr_pf_id IS 'Khóa chính tự tăng';
COMMENT ON COLUMN user_profiles.first_name IS 'Họ của người dùng';
COMMENT ON COLUMN user_profiles.middle_name IS 'Tên đệm của người dùng';
COMMENT ON COLUMN user_profiles.last_name IS 'Tên của người dùng';
COMMENT ON COLUMN user_profiles.date_of_birth IS 'Ngày sinh';
COMMENT ON COLUMN user_profiles.gender IS 'M is male, F is female, and another character if you want to specify other gender :D';
COMMENT ON COLUMN user_profiles.avatar_path IS 'Đường dẫn ảnh đại diện';
COMMENT ON COLUMN user_profiles.status IS 'Trạng thái hoạt động của người dùng (online, offline...)';
COMMENT ON COLUMN user_profiles.last_login IS 'Lần đăng nhập gần nhất';
COMMENT ON COLUMN user_profiles.last_activity IS 'Thời điểm tương tác gần nhất';
COMMENT ON COLUMN user_profiles.verified_at IS 'Thời điểm tài khoản được xác minh';
COMMENT ON COLUMN user_profiles.deleted_at IS 'Thời điểm tài khoản bị xoá (soft delete)';
COMMENT ON COLUMN user_profiles.auth_provider IS 'Dịch vụ đăng nhập bên thứ 3 (Google, Facebook, etc)';
COMMENT ON COLUMN user_profiles.auth_provider_id IS 'ID xác định của user bên thứ 3';
COMMENT ON COLUMN user_profiles.two_factor_enabled IS 'Bật xác thực hai bước hay chưa';
COMMENT ON COLUMN user_profiles.session_id IS 'ID phiên đăng nhập hiện tại';
COMMENT ON COLUMN user_profiles.login_attempt IS 'Số lần đăng nhập thất bại liên tiếp';
COMMENT ON COLUMN user_profiles.fail_login_at IS 'Thời điểm đăng nhập thất bại gần nhất';

----- ROLE -----

CREATE TABLE user_roles (
    role_id SMALLINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

COMMENT ON TABLE user_roles IS 'Danh sách vai trò người dùng trong hệ thống';
COMMENT ON COLUMN user_roles.role_id IS 'Mã vai trò duy nhất (ví dụ: 1 = USER, 2 = ADMIN)';
COMMENT ON COLUMN user_roles.name IS 'Tên vai trò, định dạng chuẩn như ROLE_USER, ROLE_ADMIN';
COMMENT ON COLUMN user_roles.description IS 'Mô tả vai trò để người quản trị hiểu rõ chức năng';

-- Sample data
INSERT INTO user_roles (role_id, name, description) VALUES
(1, 'ROLE_USER', 'Người dùng thông thường'),
(2, 'ROLE_ADMIN', 'Quản trị viên hệ thống');

----- USER -----

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    usr_id SERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    mobile VARCHAR(20) UNIQUE,
    role_id SMALLINT REFERENCES user_roles(role_id),
    is_verified BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    profile_id BIGINT UNIQUE,
    task_id BIGINT UNIQUE,
    FOREIGN KEY (profile_id) REFERENCES user_profiles(usr_pf_id),
    FOREIGN KEY (task_id) REFERENCES Project.tasks(tsk_id)
);

COMMENT ON TABLE users IS 'Chứa thông tin định danh, xác thực và bảo mật của người dùng';
COMMENT ON COLUMN users.usr_id IS 'Khoá chính tự tăng';
COMMENT ON COLUMN users.uuid IS 'UUID duy nhất cho mỗi người dùng (không thay đổi)';
COMMENT ON COLUMN users.username IS 'Tên đăng nhập duy nhất';
COMMENT ON COLUMN users.password_hash IS 'Mật khẩu đã được mã hoá';
COMMENT ON COLUMN users.email IS 'Email người dùng (tuỳ chọn)';
COMMENT ON COLUMN users.mobile IS 'Số điện thoại người dùng (tuỳ chọn)';
COMMENT ON COLUMN users.role_id IS 'Vai trò người dùng (1: USER, 2: ADMIN, etc)';
COMMENT ON COLUMN users.is_verified IS 'Người dùng đã xác minh tài khoản hay chưa';
COMMENT ON COLUMN users.is_locked IS 'Tài khoản có đang bị khóa hay không';
COMMENT ON COLUMN users.is_deleted IS 'Đánh dấu soft delete';
COMMENT ON COLUMN users.created_at IS 'Thời điểm tạo tài khoản';
COMMENT ON COLUMN users.updated_at IS 'Thời điểm cập nhật gần nhất';
COMMENT ON COLUMN users.profile_id IS 'Khóa ngoại liên kết đến bảng user_profiles';


-----===== PROJECT SCHEMA =====-----

-- phân loại công việc
CREATE TABLE categories (
    c8s_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    user_id BIGINT REFERENCES users(id),
    color VARCHAR(7) -- #RRGGBB cho UI
);

-- công việc chính
CREATE TABLE tasks (
    tsk_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,-- need a table to determine exactly time of deadline
    priority SMALLINT, -- 1: low, 2: normal, 3: high
    status VARCHAR(20), -- pending, in_progress, completed for scalable must slpit into another table
    category_id BIGINT REFERENCES categories(c8s_id),
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    is_deleted ,
    deleted_at TIMESTAMP,
);

-- nhãn dán linh hoạt, mỗi task có thể có nhiều tag ( quan trọng, khẩn cấp ... )
CREATE TABLE tags (
    tgs_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    user_id BIGINT REFERENCES users(usr_id)
);

-- quan hệ nhiều - nhiều qua bảng task_tags
CREATE TABLE task_tags (
    task_id BIGINT REFERENCES tasks(tsk_id),
    tag_id BIGINT REFERENCES tags(tgs_id),
    PRIMARY KEY (task_id, tag_id)
);

-- công việc con, có thể mở rộng due_date, note, ... nếu cần
CREATE TABLE subtasks (
    stk_id SERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES tasks(tsk_id),
    title VARCHAR(255) NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT now()
    is_deleted ,
    deleted_at TIMESTAMP
);

-- có thể tạo schedule nhắc nhở
CREATE TABLE reminders (
    rmd_id SERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES tasks(tsk_id),
    remind_at TIMESTAMP NOT NULL,
    repeat_interval VARCHAR(20) -- daily, weekly, monthly
-- need to specify the time user need to remind them
);

-- ghi lại mọi thao tác thêm xoá sửa, trường details có thể lưu dưới dạng JSON (e.g {"title":{"Old","New"}})
CREATE TABLE activity_log (
    act_id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(usr_id),
    task_id BIGINT REFERENCES tasks(tsk_id),
    action VARCHAR(20), -- create, update, delete
    details JSONB, -- e.g {"title": ["Old", "New"]}
    created_at TIMESTAMP DEFAULT now()
);


CREATE TABLE comments (

);


