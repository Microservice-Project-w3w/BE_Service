-- Chỉ tạo database, chưa tạo bảng hoặc dữ liệu mẫu.
CREATE DATABASE IF NOT EXISTS identity_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
       -- =====================================================
-- DATABASE QUẢN LÝ TÀI KHOẢN VÀ PHÂN QUYỀN
-- Dùng cho identity-service / auth-service
-- Chỉ đăng ký và đăng nhập bằng Gmail
-- Mỗi người dùng chỉ có một vai trò
-- =====================================================

CREATE DATABASE IF NOT EXISTS identity_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE identity_db;

-- =====================================================
-- 1. BẢNG VAI TRÒ
-- Phải tạo trước bảng users vì users có role_id
-- =====================================================

CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       code VARCHAR(50) NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       description VARCHAR(255),

                       is_system BOOLEAN NOT NULL DEFAULT FALSE,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at DATETIME NOT NULL
                       DEFAULT CURRENT_TIMESTAMP,

                       updated_at DATETIME NOT NULL
                        DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT uq_roles_code
                           UNIQUE (code)
);

-- =====================================================
-- 2. BẢNG QUYỀN HẠN
-- Ví dụ:
-- USER_VIEW
-- USER_CREATE
-- ROLE_UPDATE
-- =====================================================

CREATE TABLE permissions (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

                             code VARCHAR(100) NOT NULL,
                             name VARCHAR(150) NOT NULL,

                             resource_name VARCHAR(100) NOT NULL,
                             permission_action VARCHAR(50) NOT NULL,

                             description VARCHAR(255),

                             created_at DATETIME NOT NULL
                              DEFAULT CURRENT_TIMESTAMP,

                             updated_at DATETIME NOT NULL
                              DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

                             CONSTRAINT uq_permissions_code
                                 UNIQUE (code)
);

-- =====================================================
-- 3. BẢNG NGƯỜI DÙNG
-- Chỉ đăng ký và đăng nhập bằng Gmail
-- Mỗi người dùng chỉ có một role_id
-- =====================================================

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Mỗi người dùng chỉ có một vai trò
                       role_id BIGINT NOT NULL,

                       full_name VARCHAR(150) NOT NULL,

    -- Chỉ sử dụng địa chỉ Gmail
                       email VARCHAR(150) NOT NULL,

    -- Chỉ lưu mật khẩu đã mã hóa bằng BCrypt hoặc Argon2
                       password_hash VARCHAR(255) NOT NULL,

                       status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    -- PENDING: chưa xác minh Gmail
    -- ACTIVE: đang hoạt động
    -- INACTIVE: tạm ngừng hoạt động
    -- LOCKED: bị khóa
    -- DELETED: đã xóa mềm

                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,

                       failed_login_attempts INT NOT NULL DEFAULT 0,

                       locked_until DATETIME NULL,

                       last_login_at DATETIME NULL,

    -- Người đã tạo tài khoản
    -- NULL nếu khách hàng tự đăng ký hoặc hệ thống tự tạo
                       created_by BIGINT NULL,

    -- Người cập nhật tài khoản gần nhất
                       updated_by BIGINT NULL,

                       created_at DATETIME NOT NULL
                           DEFAULT CURRENT_TIMESTAMP,

                       updated_at DATETIME NOT NULL
                           DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,

                       deleted_at DATETIME NULL,

                       CONSTRAINT uq_users_email
                           UNIQUE (email),

                       CONSTRAINT fk_users_role
                           FOREIGN KEY (role_id)
                               REFERENCES roles(id)
                               ON DELETE RESTRICT
                               ON UPDATE CASCADE,

                       CONSTRAINT fk_users_created_by
                           FOREIGN KEY (created_by)
                               REFERENCES users(id)
                               ON DELETE SET NULL
                               ON UPDATE CASCADE,

                       CONSTRAINT fk_users_updated_by
                           FOREIGN KEY (updated_by)
                               REFERENCES users(id)
                               ON DELETE SET NULL
                               ON UPDATE CASCADE,

    -- Chỉ cho phép email kết thúc bằng @gmail.com
                       CONSTRAINT chk_users_gmail
                           CHECK (
                               LOWER(email)
                               REGEXP '^[a-z0-9._%+-]+@gmail[.]com$'
)
    );

CREATE INDEX idx_users_role_id
    ON users(role_id);

CREATE INDEX idx_users_status
    ON users(status);

CREATE INDEX idx_users_created_by
    ON users(created_by);

CREATE INDEX idx_users_updated_by
    ON users(updated_by);

-- =====================================================
-- 4. BẢNG GÁN QUYỀN CHO VAI TRÒ
--
-- Người dùng chỉ có một vai trò.
-- Tuy nhiên, một vai trò vẫn có thể có nhiều quyền.
-- =====================================================

CREATE TABLE role_permissions (
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,

    -- Người thực hiện gán quyền
                                  assigned_by BIGINT NULL,

                                  assigned_at DATETIME NOT NULL
                                      DEFAULT CURRENT_TIMESTAMP,

                                  PRIMARY KEY (role_id, permission_id),

                                  CONSTRAINT fk_role_permissions_role
                                      FOREIGN KEY (role_id)
                                          REFERENCES roles(id)
                                          ON DELETE CASCADE
                                          ON UPDATE CASCADE,

                                  CONSTRAINT fk_role_permissions_permission
                                      FOREIGN KEY (permission_id)
                                          REFERENCES permissions(id)
                                          ON DELETE CASCADE
                                          ON UPDATE CASCADE,

                                  CONSTRAINT fk_role_permissions_assigned_by
                                      FOREIGN KEY (assigned_by)
                                          REFERENCES users(id)
                                          ON DELETE SET NULL
                                          ON UPDATE CASCADE
);

-- =====================================================
-- 5. BẢNG PHIÊN ĐĂNG NHẬP
-- Theo dõi thiết bị, IP và thời gian đăng nhập
-- =====================================================

CREATE TABLE user_sessions (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,

                               user_id BIGINT NOT NULL,

    -- Có thể dùng nếu hệ thống quản lý session token
                               session_token_hash VARCHAR(255) NULL,

    -- Refresh token phải được hash trước khi lưu
                               refresh_token_hash VARCHAR(255) NOT NULL,

                               device_name VARCHAR(150),
                               device_type VARCHAR(50),

                               ip_address VARCHAR(45),
                               user_agent TEXT,

                               login_at DATETIME NOT NULL
                                   DEFAULT CURRENT_TIMESTAMP,

                               last_activity_at DATETIME NOT NULL
                                   DEFAULT CURRENT_TIMESTAMP,

                               expires_at DATETIME NOT NULL,

                               revoked_at DATETIME NULL,
                               revoked_reason VARCHAR(255),

                               CONSTRAINT uq_sessions_refresh_token
                                   UNIQUE (refresh_token_hash),

                               CONSTRAINT fk_sessions_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
                                       ON UPDATE CASCADE
);

CREATE INDEX idx_sessions_user_id
    ON user_sessions(user_id);

CREATE INDEX idx_sessions_status
    ON user_sessions(user_id, revoked_at, expires_at);

-- =====================================================
-- 6. BẢNG OTP VÀ TOKEN XÁC THỰC GMAIL
--
-- Dùng cho:
-- - Xác minh Gmail khi đăng ký
-- - Quên mật khẩu
-- - Đặt lại mật khẩu
--
-- Không còn SMS hoặc số điện thoại
-- =====================================================

CREATE TABLE verification_codes (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Có thể NULL khi gửi mã trước lúc tạo tài khoản
                                    user_id BIGINT NULL,

    -- Gmail nhận OTP hoặc liên kết xác thực
                                    email VARCHAR(150) NOT NULL,

                                    purpose VARCHAR(50) NOT NULL,
    -- REGISTER
    -- VERIFY_EMAIL
    -- RESET_PASSWORD

    -- Nếu sử dụng OTP
                                    code_hash VARCHAR(255) NULL,

    -- Nếu sử dụng liên kết xác thực
                                    token_hash VARCHAR(255) NULL,

                                    attempt_count INT NOT NULL DEFAULT 0,
                                    max_attempts INT NOT NULL DEFAULT 5,

                                    expires_at DATETIME NOT NULL,

                                    used_at DATETIME NULL,

                                    created_at DATETIME NOT NULL
                                                               DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_verification_codes_user
                                        FOREIGN KEY (user_id)
                                            REFERENCES users(id)
                                            ON DELETE SET NULL
                                            ON UPDATE CASCADE,

                                    CONSTRAINT chk_verification_codes_gmail
                                        CHECK (
                                            LOWER(email)
                                            REGEXP '^[a-z0-9._%+-]+@gmail[.]com$'
),

    -- Phải có OTP hoặc token
    CONSTRAINT chk_verification_code_or_token
        CHECK (
            code_hash IS NOT NULL
            OR token_hash IS NOT NULL
        )
);

CREATE INDEX idx_verification_lookup
    ON verification_codes(email, purpose, expires_at);

CREATE INDEX idx_verification_user_id
    ON verification_codes(user_id);

-- =====================================================
-- 7. LỊCH SỬ MẬT KHẨU
-- Ngăn người dùng sử dụng lại mật khẩu cũ
-- =====================================================

CREATE TABLE password_history (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                  user_id BIGINT NOT NULL,

                                  password_hash VARCHAR(255) NOT NULL,

    -- Người thực hiện thay đổi mật khẩu
                                  changed_by BIGINT NULL,

                                  change_reason VARCHAR(50) NOT NULL
                                      DEFAULT 'USER_CHANGE',
    -- REGISTER
    -- USER_CHANGE
    -- RESET_PASSWORD
    -- ADMIN_RESET

                                  changed_at DATETIME NOT NULL
                                      DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_password_history_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE
                                          ON UPDATE CASCADE,

                                  CONSTRAINT fk_password_history_changed_by
                                      FOREIGN KEY (changed_by)
                                          REFERENCES users(id)
                                          ON DELETE SET NULL
                                          ON UPDATE CASCADE
);

CREATE INDEX idx_password_history_user
    ON password_history(user_id, changed_at);

-- =====================================================
-- 8. NHẬT KÝ HOẠT ĐỘNG
-- Theo dõi đăng ký, đăng nhập, đăng xuất,
-- đổi mật khẩu, đổi vai trò và phân quyền
-- =====================================================

CREATE TABLE audit_logs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Người thực hiện hành động
                            user_id BIGINT NULL,

                            action_code VARCHAR(100) NOT NULL,
    -- REGISTER
    -- VERIFY_EMAIL
    -- LOGIN_SUCCESS
    -- LOGIN_FAILED
    -- LOGOUT
    -- CHANGE_PASSWORD
    -- RESET_PASSWORD
    -- CHANGE_ROLE
    -- ASSIGN_PERMISSION
    -- LOCK_ACCOUNT
    -- UNLOCK_ACCOUNT
    -- DELETE_ACCOUNT

                            entity_type VARCHAR(100),
                            entity_id VARCHAR(100),

                            ip_address VARCHAR(45),
                            user_agent TEXT,

                            details JSON NULL,

                            created_at DATETIME NOT NULL
                                DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_audit_logs_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE SET NULL
                                    ON UPDATE CASCADE
);

CREATE INDEX idx_audit_logs_user
    ON audit_logs(user_id, created_at);

CREATE INDEX idx_audit_logs_action
    ON audit_logs(action_code, created_at);




CREATE DATABASE IF NOT EXISTS organization_customer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS rental_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS logistics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS billing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS maintenance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
