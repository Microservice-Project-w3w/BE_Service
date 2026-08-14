
CREATE DATABASE IF NOT EXISTS identity_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


USE identity_db;
CREATE TABLE IF NOT EXISTS roles (
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

CREATE TABLE IF NOT EXISTS permissions (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

                             code VARCHAR(150) NOT NULL,
                             name VARCHAR(150) NOT NULL,

                             domain VARCHAR(50) NOT NULL,

                             description VARCHAR(500),

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

CREATE TABLE IF NOT EXISTS users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Mỗi người dùng chỉ có một vai trò
                       role_id BIGINT NOT NULL,

                       organization_id BIGINT NULL,

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

-- Danh sách chi nhánh được phân công; không có FK vì branch thuộc
-- organization-customer-service, không thuộc identity_db.
CREATE TABLE IF NOT EXISTS user_branch_assignments (
    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_user_branch_assignments_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- =====================================================
-- 4. BẢNG GÁN QUYỀN CHO VAI TRÒ
--
-- Người dùng chỉ có một vai trò.
-- Tuy nhiên, một vai trò vẫn có thể có nhiều quyền.
-- =====================================================

CREATE TABLE IF NOT EXISTS role_permissions (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,
                                  data_scope VARCHAR(20) NOT NULL,

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

                                  CONSTRAINT uq_role_permissions_role_permission
                                      UNIQUE (role_id, permission_id),

                                  CONSTRAINT chk_role_permissions_scope
                                      CHECK (data_scope IN ('SYSTEM', 'ORGANIZATION', 'BRANCH', 'OWN'))
);

-- =====================================================
-- 5. BẢNG PHIÊN ĐĂNG NHẬP
-- Theo dõi thiết bị, IP và thời gian đăng nhập
-- =====================================================

CREATE TABLE IF NOT EXISTS user_sessions (
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

CREATE TABLE IF NOT EXISTS verification_codes (
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

CREATE TABLE IF NOT EXISTS password_history (
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

CREATE TABLE IF NOT EXISTS audit_logs (
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
       -- =========================================================
-- ORGANIZATION CUSTOMER SERVICE DATABASE
-- =========================================================

CREATE DATABASE IF NOT EXISTS organization_customer_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE organization_customer_db;


-- =========================================================
-- 1. ORGANIZATIONS - DOANH NGHIỆP
-- =========================================================

CREATE TABLE organizations (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,

                               organization_code VARCHAR(50) NOT NULL,
                               organization_name VARCHAR(255) NOT NULL,

                               tax_code VARCHAR(50),
                               email VARCHAR(255),
                               phone VARCHAR(30),

                               address VARCHAR(500),

                               status ENUM(
        'ACTIVE',
        'INACTIVE',
        'SUSPENDED',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                               created_by BIGINT,
                               updated_by BIGINT,

                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,

                               deleted_at DATETIME NULL,

                               CONSTRAINT uq_organizations_code
                                   UNIQUE (organization_code),

                               CONSTRAINT uq_organizations_tax_code
                                   UNIQUE (tax_code)
);


-- =========================================================
-- 2. BRANCHES - CHI NHÁNH
-- =========================================================

CREATE TABLE branches (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                          organization_id BIGINT NOT NULL,

                          branch_code VARCHAR(50) NOT NULL,
                          branch_name VARCHAR(255) NOT NULL,

                          email VARCHAR(255),
                          phone VARCHAR(30),

                          address VARCHAR(500),

                          status ENUM(
        'ACTIVE',
        'INACTIVE',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                          created_by BIGINT,
                          updated_by BIGINT,

                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

                          deleted_at DATETIME NULL,

                          CONSTRAINT fk_branches_organization
                              FOREIGN KEY (organization_id)
                                  REFERENCES organizations(id),

                          CONSTRAINT uq_branch_code_per_organization
                              UNIQUE (organization_id, branch_code),

    -- phục vụ FK kép để bảo đảm đúng organization
                          CONSTRAINT uq_branch_id_organization
                              UNIQUE (id, organization_id)
);


-- =========================================================
-- 3. EMPLOYEES - NHÂN VIÊN
--
-- user_id chỉ tham chiếu LOGIC sang identity-service.
-- KHÔNG tạo foreign key tới identity_db.
-- =========================================================

CREATE TABLE employees (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,

                           organization_id BIGINT NOT NULL,

    -- ID user lấy từ identity-service nếu nhân viên có tài khoản
                           user_id BIGINT NULL,

                           employee_code VARCHAR(50) NOT NULL,

                           full_name VARCHAR(255) NOT NULL,

                           email VARCHAR(255),
                           phone VARCHAR(30),

                           job_title VARCHAR(100),

                           status ENUM(
        'ACTIVE',
        'INACTIVE',
        'RESIGNED',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                           hire_date DATE,

                           created_by BIGINT,
                           updated_by BIGINT,

                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,

                           deleted_at DATETIME NULL,

                           CONSTRAINT fk_employees_organization
                               FOREIGN KEY (organization_id)
                                   REFERENCES organizations(id),

                           CONSTRAINT uq_employee_code_per_organization
                               UNIQUE (organization_id, employee_code),

                           CONSTRAINT uq_employee_user_per_organization
                               UNIQUE (organization_id, user_id),

    -- phục vụ EmployeeBranchAssignment
                           CONSTRAINT uq_employee_id_organization
                               UNIQUE (id, organization_id)
);


-- =========================================================
-- 4. EMPLOYEE BRANCH ASSIGNMENTS
-- GÁN NHÂN VIÊN VÀO CHI NHÁNH
--
-- Đây là bảng RIÊNG theo đúng yêu cầu.
-- Không nhét branch trực tiếp vào employee.
-- =========================================================

CREATE TABLE employee_branch_assignments (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                             organization_id BIGINT NOT NULL,
                                             employee_id BIGINT NOT NULL,
                                             branch_id BIGINT NOT NULL,

                                             is_primary BOOLEAN NOT NULL DEFAULT FALSE,

                                             assigned_from DATE,
                                             assigned_to DATE,

                                             status ENUM(
        'ACTIVE',
        'INACTIVE'
    ) NOT NULL DEFAULT 'ACTIVE',

                                             created_by BIGINT,
                                             updated_by BIGINT,

                                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                 ON UPDATE CURRENT_TIMESTAMP,

                                             CONSTRAINT fk_assignment_organization
                                                 FOREIGN KEY (organization_id)
                                                     REFERENCES organizations(id),

    -- employee phải thuộc đúng organization
                                             CONSTRAINT fk_assignment_employee
                                                 FOREIGN KEY (employee_id, organization_id)
                                                     REFERENCES employees(id, organization_id),

    -- branch phải thuộc đúng organization
                                             CONSTRAINT fk_assignment_branch
                                                 FOREIGN KEY (branch_id, organization_id)
                                                     REFERENCES branches(id, organization_id),

                                             CONSTRAINT uq_employee_branch
                                                 UNIQUE (employee_id, branch_id)
);


-- =========================================================
-- 5. CUSTOMERS - KHÁCH HÀNG
--
-- Dùng chung 1 bảng cho:
-- + INDIVIDUAL = khách hàng cá nhân
-- + BUSINESS   = khách hàng doanh nghiệp
--
-- owner_user_id hỗ trợ kiểm tra scope OWN.
-- =========================================================

CREATE TABLE customers (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,

                           organization_id BIGINT NOT NULL,

    -- Có thể NULL nếu khách hàng không thuộc riêng một chi nhánh
                           branch_id BIGINT NULL,

    -- user chịu trách nhiệm quản lý customer này.
    -- Chỉ tham chiếu logic tới identity-service.
                           owner_user_id BIGINT NULL,

                           customer_code VARCHAR(50) NOT NULL,

                           customer_type ENUM(
        'INDIVIDUAL',
        'BUSINESS'
    ) NOT NULL,

    -- =====================================================
    -- Thông tin chung
    -- =====================================================

                           display_name VARCHAR(255) NOT NULL,

                           email VARCHAR(255),
                           phone VARCHAR(30),

                           address VARCHAR(500),

    -- =====================================================
    -- Thông tin khách hàng cá nhân
    -- =====================================================

                           full_name VARCHAR(255),
                           date_of_birth DATE,

                           identity_number VARCHAR(100),

    -- =====================================================
    -- Thông tin khách hàng doanh nghiệp
    -- =====================================================

                           company_name VARCHAR(255),
                           tax_code VARCHAR(50),

                           representative_name VARCHAR(255),
                           representative_phone VARCHAR(30),
                           representative_email VARCHAR(255),

    -- =====================================================

                           status ENUM(
        'ACTIVE',
        'INACTIVE',
        'BLOCKED',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                           note TEXT,

                           created_by BIGINT,
                           updated_by BIGINT,

                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,

                           deleted_at DATETIME NULL,

                           CONSTRAINT fk_customers_organization
                               FOREIGN KEY (organization_id)
                                   REFERENCES organizations(id),

                           CONSTRAINT fk_customers_branch
                               FOREIGN KEY (branch_id, organization_id)
                                   REFERENCES branches(id, organization_id),

                           CONSTRAINT uq_customer_code_per_organization
                               UNIQUE (organization_id, customer_code),

                           CONSTRAINT uq_customer_id_organization
                               UNIQUE (id, organization_id)
);


-- =========================================================
-- 6. CUSTOMER GROUPS - NHÓM KHÁCH HÀNG
-- =========================================================

CREATE TABLE customer_groups (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                 organization_id BIGINT NOT NULL,

                                 group_code VARCHAR(50) NOT NULL,
                                 group_name VARCHAR(255) NOT NULL,

                                 description TEXT,

                                 status ENUM(
        'ACTIVE',
        'INACTIVE',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                                 created_by BIGINT,
                                 updated_by BIGINT,

                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,

                                 deleted_at DATETIME NULL,

                                 CONSTRAINT fk_customer_groups_organization
                                     FOREIGN KEY (organization_id)
                                         REFERENCES organizations(id),

                                 CONSTRAINT uq_customer_group_code
                                     UNIQUE (organization_id, group_code),

                                 CONSTRAINT uq_customer_group_id_organization
                                     UNIQUE (id, organization_id)
);


-- =========================================================
-- 7. CUSTOMER GROUP MEMBERS
--
-- Quan hệ nhiều-nhiều:
-- Customer <-> CustomerGroup
-- =========================================================

CREATE TABLE customer_group_members (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                        organization_id BIGINT NOT NULL,

                                        customer_group_id BIGINT NOT NULL,
                                        customer_id BIGINT NOT NULL,

                                        created_by BIGINT,

                                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT fk_group_member_organization
                                            FOREIGN KEY (organization_id)
                                                REFERENCES organizations(id),

                                        CONSTRAINT fk_group_member_group
                                            FOREIGN KEY (customer_group_id, organization_id)
                                                REFERENCES customer_groups(id, organization_id),

                                        CONSTRAINT fk_group_member_customer
                                            FOREIGN KEY (customer_id, organization_id)
                                                REFERENCES customers(id, organization_id),

                                        CONSTRAINT uq_customer_group_member
                                            UNIQUE (customer_group_id, customer_id)
);


-- =========================================================
-- 8. RESTRICTED CUSTOMERS
-- DANH SÁCH KHÁCH HÀNG HẠN CHẾ
--
-- Đây là bảng riêng theo đúng yêu cầu.
-- Không chỉ dùng một boolean trong bảng customers.
-- =========================================================

CREATE TABLE restricted_customers (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                      organization_id BIGINT NOT NULL,
                                      customer_id BIGINT NOT NULL,

                                      restriction_type ENUM(
        'RENTAL_BLOCK',
        'PAYMENT_RISK',
        'LATE_RETURN',
        'DAMAGED_EQUIPMENT',
        'FRAUD_RISK',
        'OTHER'
    ) NOT NULL,

                                      reason VARCHAR(1000) NOT NULL,

                                      status ENUM(
        'ACTIVE',
        'EXPIRED',
        'REMOVED'
    ) NOT NULL DEFAULT 'ACTIVE',

                                      restricted_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      restricted_until DATETIME NULL,

    -- user từ identity-service thực hiện hạn chế
                                      restricted_by_user_id BIGINT NOT NULL,

                                      removed_at DATETIME NULL,
                                      removed_by_user_id BIGINT NULL,
                                      removed_reason VARCHAR(1000),

                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                          ON UPDATE CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_restricted_organization
                                          FOREIGN KEY (organization_id)
                                              REFERENCES organizations(id),

                                      CONSTRAINT fk_restricted_customer
                                          FOREIGN KEY (customer_id, organization_id)
                                              REFERENCES customers(id, organization_id)
);


-- =========================================================
-- INDEX
-- =========================================================

CREATE INDEX idx_branches_organization
    ON branches(organization_id);

CREATE INDEX idx_employees_organization
    ON employees(organization_id);

CREATE INDEX idx_employees_user
    ON employees(user_id);

CREATE INDEX idx_assignment_employee
    ON employee_branch_assignments(employee_id);

CREATE INDEX idx_assignment_branch
    ON employee_branch_assignments(branch_id);

CREATE INDEX idx_customers_organization
    ON customers(organization_id);

CREATE INDEX idx_customers_branch
    ON customers(branch_id);

CREATE INDEX idx_customers_owner
    ON customers(owner_user_id);

CREATE INDEX idx_customers_email
    ON customers(email);

CREATE INDEX idx_customers_phone
    ON customers(phone);

CREATE INDEX idx_customer_groups_organization
    ON customer_groups(organization_id);

CREATE INDEX idx_restricted_customer
    ON restricted_customers(customer_id);

CREATE INDEX idx_restricted_status
    ON restricted_customers(organization_id, status);
CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS rental_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rental_db;

CREATE TABLE IF NOT EXISTS rental_prices (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                             price_name VARCHAR(150) NOT NULL,

    organization_id BIGINT NOT NULL,

    branch_id BIGINT NOT NULL,

    equipment_type_id BIGINT NOT NULL,

    rental_unit ENUM(
                        'HOUR',
                        'DAY',
                        'WEEK',
                        'MONTH'
                    ) NOT NULL,

    rental_price DECIMAL(15,2) NOT NULL DEFAULT 0,

    deposit_type ENUM('FIXED', 'PERCENT') NOT NULL,

    deposit_value DECIMAL(15,2) NOT NULL DEFAULT 0,

    late_fee DECIMAL(15,2) NOT NULL DEFAULT 0,

    valid_from DATETIME NOT NULL,

    valid_to DATETIME NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    description VARCHAR(500) NULL,

    created_at DATETIME NOT NULL
    DEFAULT CURRENT_TIMESTAMP,

    updated_at DATETIME NOT NULL
    DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_rental_price_equipment_unit_valid_from
    UNIQUE (
               equipment_type_id,
               rental_unit,
               organization_id,
               branch_id,
               valid_from
           ),

    CONSTRAINT chk_rental_price_non_negative
    CHECK (rental_price >= 0),

    CONSTRAINT chk_deposit_value_non_negative
    CHECK (deposit_value >= 0),

    CONSTRAINT chk_late_fee_non_negative
    CHECK (late_fee >= 0),

    CONSTRAINT chk_valid_date
    CHECK (
              valid_to IS NULL
              OR valid_to >= valid_from
          )
    );
-- Các bảng dưới đây thuộc rental-service. Các cột *_id là ID tham chiếu giữa
-- service chỉ được lưu giá trị; riêng rental_request_items có FK nội bộ rental_db.
CREATE TABLE IF NOT EXISTS discount_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    discount_type ENUM('PERCENT', 'FIXED') NOT NULL,
    discount_value DECIMAL(15,2) NOT NULL,
    max_discount DECIMAL(15,2) NULL,
    min_order_value DECIMAL(15,2) NULL,
    customer_group VARCHAR(50) NULL,
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_discount_code_organization_branch_code UNIQUE (organization_id, branch_id, code),
    CONSTRAINT chk_discount_value_non_negative CHECK (discount_value >= 0),
    CONSTRAINT chk_discount_dates CHECK (valid_to >= valid_from)
);

CREATE TABLE IF NOT EXISTS rental_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    request_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    delivery_address VARCHAR(500) NULL,
    note VARCHAR(1000) NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rental_request_scope (organization_id, branch_id),
    CONSTRAINT chk_rental_request_dates CHECK (end_at > start_at)
);

CREATE TABLE IF NOT EXISTS rental_request_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_request_id BIGINT NOT NULL,
    equipment_type_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_rental_request_item_request
        FOREIGN KEY (rental_request_id) REFERENCES rental_requests(id),
    CONSTRAINT chk_rental_request_item_quantity CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS quotations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    quotation_code VARCHAR(50) NOT NULL UNIQUE,
    rental_request_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rental_amount DECIMAL(15,2) NOT NULL,
    deposit_amount DECIMAL(15,2) NOT NULL,
    delivery_fee DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    discount_code VARCHAR(50) NULL,
    status VARCHAR(30) NOT NULL,
    valid_until DATETIME NOT NULL,
    special_terms VARCHAR(1000) NULL,
    INDEX idx_quotation_scope (organization_id, branch_id),
    CONSTRAINT chk_quotation_amounts CHECK (
        rental_amount >= 0 AND deposit_amount >= 0 AND delivery_fee >= 0
        AND discount_amount >= 0 AND total_amount >= 0
    )
);

CREATE TABLE IF NOT EXISTS rental_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    quotation_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reserved_until DATETIME NULL,
    inventory_reservation_id VARCHAR(100) NULL,
    cancel_reason VARCHAR(500) NULL,
    INDEX idx_rental_order_scope (organization_id, branch_id),
    CONSTRAINT chk_rental_order_dates CHECK (end_at > start_at),
    CONSTRAINT chk_rental_order_total CHECK (total_amount >= 0)
);

CREATE TABLE IF NOT EXISTS rental_contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    contract_code VARCHAR(50) NOT NULL,
    rental_order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    terms VARCHAR(2000) NULL,
    approved_at DATETIME NULL,
    signed_at DATETIME NULL,
    liquidated_at DATETIME NULL,
    cancel_reason VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_rental_contract_order UNIQUE (rental_order_id),
    CONSTRAINT uk_rental_contract_code UNIQUE (contract_code),
    INDEX idx_rental_contract_scope (organization_id, branch_id),
    CONSTRAINT chk_rental_contract_dates CHECK (end_at > start_at),
    CONSTRAINT chk_rental_contract_total CHECK (total_amount >= 0)
);

CREATE TABLE IF NOT EXISTS contract_appendices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    appendix_code VARCHAR(50) NOT NULL,
    appendix_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    new_end_at DATETIME NULL,
    terms VARCHAR(2000) NOT NULL,
    approved_at DATETIME NULL,
    signed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_contract_appendix_code UNIQUE (appendix_code),
    CONSTRAINT fk_contract_appendix_contract FOREIGN KEY (contract_id) REFERENCES rental_contracts(id),
    INDEX idx_contract_appendix_scope (organization_id, branch_id)
);

CREATE DATABASE IF NOT EXISTS logistics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE logistics_db;


-- =========================================================
-- 1. CẤU HÌNH PHÍ GIAO NHẬN
-- =========================================================
CREATE TABLE delivery_fee_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    organization_id BIGINT NOT NULL,
    branch_id BIGINT NULL,

    name VARCHAR(100) NOT NULL,

    base_fee DECIMAL(15,2) NOT NULL,
    max_distance_km DECIMAL(10,2) NOT NULL,
    extra_fee_per_km DECIMAL(15,2) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL
        ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_delivery_fee_org (organization_id),
    INDEX idx_delivery_fee_branch (branch_id),
    INDEX idx_delivery_fee_active (is_active)
);


-- =========================================================
-- 2. NHIỆM VỤ / PHÂN CÔNG GIAO NHẬN
-- =========================================================
CREATE TABLE delivery_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    rental_order_id BIGINT NOT NULL,

    -- DELIVERY / RETURN_PICKUP
    task_type VARCHAR(30) NOT NULL,

    -- User ID lấy từ identity-service
    delivery_staff_user_id BIGINT NOT NULL,

    scheduled_at DATETIME NOT NULL,

    -- PENDING / ASSIGNED / IN_PROGRESS / COMPLETED / CANCELLED
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    notes VARCHAR(1000),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL
        ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_delivery_task_order (rental_order_id),
    INDEX idx_delivery_task_staff (delivery_staff_user_id),
    INDEX idx_delivery_task_schedule (scheduled_at),
    INDEX idx_delivery_task_status (status)
);


-- =========================================================
-- 3. PHIẾU XUẤT KHO / GIAO THIẾT BỊ
-- =========================================================
CREATE TABLE dispatch_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    dispatch_code VARCHAR(50) NOT NULL UNIQUE,

    -- ID từ rental-service
    rental_order_id BIGINT NOT NULL,

    -- ID từ organization-customer-service
    customer_id BIGINT NOT NULL,

    -- FK nội bộ logistics-service
    delivery_task_id BIGINT NOT NULL,

    -- PREPARED / DISPATCHED / DELIVERED / CANCELLED
    status VARCHAR(30) NOT NULL DEFAULT 'PREPARED',

    prepared_at DATETIME NULL,
    dispatched_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_dispatch_note_task
        FOREIGN KEY (delivery_task_id)
        REFERENCES delivery_tasks(id),

    INDEX idx_dispatch_order (rental_order_id),
    INDEX idx_dispatch_customer (customer_id),
    INDEX idx_dispatch_task (delivery_task_id),
    INDEX idx_dispatch_status (status)
);


-- =========================================================
-- 4. CHI TIẾT PHIẾU XUẤT
-- =========================================================
CREATE TABLE dispatch_note_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    dispatch_note_id BIGINT NOT NULL,

    -- ID thiết bị từ inventory-service
    equipment_id BIGINT NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dispatch_item_note
        FOREIGN KEY (dispatch_note_id)
        REFERENCES dispatch_notes(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_dispatch_equipment
        UNIQUE (dispatch_note_id, equipment_id),

    INDEX idx_dispatch_item_equipment (equipment_id)
);


-- =========================================================
-- 5. BIÊN BẢN BÀN GIAO
-- =========================================================
CREATE TABLE handover_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    dispatch_note_id BIGINT NOT NULL,

    handover_time DATETIME NOT NULL,

    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,

    -- Chỉ lưu URL/object key, không lưu binary
    customer_signature_url VARCHAR(1000),

    confirmed_by_customer BOOLEAN NOT NULL DEFAULT FALSE,
    confirmed_at DATETIME NULL,

    notes VARCHAR(1000),

    -- PENDING / COMPLETED / REJECTED / CANCELLED
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_handover_dispatch
        FOREIGN KEY (dispatch_note_id)
        REFERENCES dispatch_notes(id),

    INDEX idx_handover_dispatch (dispatch_note_id),
    INDEX idx_handover_status (status)
);


-- =========================================================
-- 6. ẢNH BÀN GIAO
-- =========================================================
CREATE TABLE handover_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    handover_record_id BIGINT NOT NULL,

    -- URL hoặc object key
    photo_url VARCHAR(1000) NOT NULL,

    -- BEFORE_DELIVERY / EQUIPMENT / ACCESSORY /
    -- CUSTOMER_RECEIVED / DAMAGE / OTHER
    photo_type VARCHAR(30) NOT NULL,

    sort_order INT NOT NULL DEFAULT 0,

    -- ACTIVE / INACTIVE
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_photo_handover
        FOREIGN KEY (handover_record_id)
        REFERENCES handover_records(id)
        ON DELETE CASCADE,

    INDEX idx_handover_photo_record (handover_record_id)
);


-- =========================================================
-- 7. CHECKLIST BÀN GIAO
-- =========================================================
CREATE TABLE handover_checklists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    handover_record_id BIGINT NOT NULL,

    checkpoint_name VARCHAR(200) NOT NULL,

    sort_order INT NOT NULL DEFAULT 0,

    -- PENDING / CHECKED / FAILED
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    is_passed BOOLEAN NOT NULL DEFAULT FALSE,

    remarks VARCHAR(500),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_checklist_handover
        FOREIGN KEY (handover_record_id)
        REFERENCES handover_records(id)
        ON DELETE CASCADE,

    INDEX idx_checklist_handover (handover_record_id)
);


-- =========================================================
-- 8. YÊU CẦU TRẢ THIẾT BỊ
-- =========================================================
CREATE TABLE return_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- ID từ rental-service
    rental_order_id BIGINT NOT NULL,

    -- ID từ organization-customer-service
    customer_id BIGINT NOT NULL,

    requested_return_date DATETIME NOT NULL,

    reason VARCHAR(500),

    -- Nếu logistics đến lấy tại địa chỉ khách
    pickup_address VARCHAR(500),

    -- PENDING / APPROVED / SCHEDULED /
    -- IN_PROGRESS / COMPLETED / CANCELLED
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL
        ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_return_request_order (rental_order_id),
    INDEX idx_return_request_customer (customer_id),
    INDEX idx_return_request_status (status),
    INDEX idx_return_request_date (requested_return_date)
);


-- =========================================================
-- 9. BIÊN BẢN NHẬN TRẢ
-- =========================================================
CREATE TABLE return_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    return_request_id BIGINT NOT NULL,

    -- ID từ rental-service
    rental_order_id BIGINT NOT NULL,

    -- User ID từ identity-service
    inspector_staff_user_id BIGINT NOT NULL,

    actual_return_time DATETIME NOT NULL,

    -- Dữ liệu đầu vào chính thức cho Billing
    is_late_return BOOLEAN NOT NULL DEFAULT FALSE,

    -- Số phút trả trễ.
    -- Logistics ghi nhận thời gian, Billing chịu trách nhiệm tính tiền.
    late_minutes BIGINT NOT NULL DEFAULT 0,

    -- Tổng quan của toàn bộ lần trả
    missing_accessories_description VARCHAR(1000),
    condition_damage_description VARCHAR(1000),

    -- DRAFT / INSPECTED / CONFIRMED / COMPLETED / CANCELLED
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    notes VARCHAR(1000),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_return_record_request
        FOREIGN KEY (return_request_id)
        REFERENCES return_requests(id),

    INDEX idx_return_record_request (return_request_id),
    INDEX idx_return_record_order (rental_order_id),
    INDEX idx_return_record_inspector (inspector_staff_user_id),
    INDEX idx_return_record_status (status)
);


-- =========================================================
-- 10. CHI TIẾT KIỂM TRA THIẾT BỊ KHI TRẢ
-- =========================================================
CREATE TABLE return_record_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    return_record_id BIGINT NOT NULL,

    -- ID từ inventory-service
    equipment_id BIGINT NOT NULL,

    -- GOOD / SCRATCHED / DAMAGED / BROKEN / MISSING
    returned_condition VARCHAR(30) NOT NULL DEFAULT 'GOOD',

    -- Dữ liệu phục vụ Maintenance
    is_damaged BOOLEAN NOT NULL DEFAULT FALSE,
    damage_description VARCHAR(1000),

    -- Dữ liệu phục vụ Billing
    is_missing_accessories BOOLEAN NOT NULL DEFAULT FALSE,
    missing_accessories_description VARCHAR(1000),

    notes VARCHAR(500),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_return_item_record
        FOREIGN KEY (return_record_id)
        REFERENCES return_records(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_return_equipment
        UNIQUE (return_record_id, equipment_id),

    INDEX idx_return_item_equipment (equipment_id),
    INDEX idx_return_item_condition (returned_condition)
);
CREATE DATABASE IF NOT EXISTS billing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE billing_db;

-- 1. BẢNG HÓA ĐƠN (INVOICES)
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    rental_order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    invoice_type VARCHAR(30) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    due_date TIMESTAMP,
    request_reference VARCHAR(100) UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 2. CHI TIẾT HÓA ĐƠN (INVOICE ITEMS)
CREATE TABLE invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(15, 2) NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    request_reference VARCHAR(100) UNIQUE,
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

-- 3. CÁC KHOẢN PHÍ PHÁT SINH (INCURRED FEES)
CREATE TABLE incurred_fees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    fee_type VARCHAR(30) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_incurred_fees_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

-- 4. THANH TOÁN (PAYMENTS)
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    payment_reference VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(15, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    confirmation_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

-- 5. HOÀN TIỀN THANH TOÁN (PAYMENT REFUNDS)
CREATE TABLE payment_refunds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    actor_user_id BIGINT NOT NULL,
    refunded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- 6. ĐẶT CỌC (DEPOSITS)
CREATE TABLE deposits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rental_order_id BIGINT NOT NULL,
    rental_contract_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    deducted_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    refunded_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(30) NOT NULL,
    reference VARCHAR(100),
    notes VARCHAR(1000),
    status VARCHAR(30) NOT NULL DEFAULT 'HELD',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 7. CÔNG NỢ (DEBTS)
CREATE TABLE debts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    remaining_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    due_at TIMESTAMP,
    reason VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 8. LỊCH SỬ HÓA ĐƠN (INVOICE HISTORY)
CREATE TABLE invoice_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    description VARCHAR(500),
    actor_user_id BIGINT,
    created_at TIMESTAMP NOT NULL
);

-- 9. LỊCH SỬ ĐẶT CỌC (DEPOSIT HISTORY)
CREATE TABLE deposit_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deposit_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2),
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    description VARCHAR(500),
    actor_user_id BIGINT,
    created_at TIMESTAMP NOT NULL
);

-- 10. GIAO DỊCH ĐẶT CỌC (DEPOSIT TRANSACTIONS)
CREATE TABLE deposit_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reference_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE DATABASE IF NOT EXISTS maintenance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
