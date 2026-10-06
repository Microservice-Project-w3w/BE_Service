-- Demo users for identity-service. All accounts below use password Demo@123.
-- Running this script resets only the five DEMO accounts to that known password.
USE identity_db;

SET @demo_password_hash := '$2y$12$HlCMJq.5XC6ezHYnFk/gseExFW4/ic0Rm7rp8mmF.PGyJ/zBNiYdu';
SET @demo_organization_id := (
  SELECT id FROM organization_customer_db.organizations WHERE organization_code = 'DEMO-RENTAI' LIMIT 1
);
SET @demo_hn_branch_id := (
  SELECT b.id
  FROM organization_customer_db.branches b
  JOIN organization_customer_db.organizations o ON o.id = b.organization_id
  WHERE o.organization_code = 'DEMO-RENTAI' AND b.branch_code = 'DEMO-HN'
  LIMIT 1
);

INSERT INTO users (
  role_id, organization_id, full_name, phone, company_name, tax_code,
  email, password_hash, status, email_verified, failed_login_attempts
)
SELECT id, @demo_organization_id, 'Admin Demo', '0901000100', 'RentAI Demo Equipment', '0109998888',
       'rentai.demo.admin@gmail.com', @demo_password_hash, 'ACTIVE', TRUE, 0
FROM roles WHERE code = 'ADMIN'
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id), organization_id = VALUES(organization_id),
  full_name = VALUES(full_name), phone = VALUES(phone), company_name = VALUES(company_name),
  tax_code = VALUES(tax_code), password_hash = VALUES(password_hash), status = 'ACTIVE',
  email_verified = TRUE, failed_login_attempts = 0, locked_until = NULL, deleted_at = NULL;

INSERT INTO users (
  role_id, organization_id, full_name, phone, email, password_hash, status, email_verified, failed_login_attempts
)
SELECT id, @demo_organization_id, 'Quản lý Demo', '0901000101',
       'rentai.demo.manager@gmail.com', @demo_password_hash, 'ACTIVE', TRUE, 0
FROM roles WHERE code = 'MANAGER'
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id), organization_id = VALUES(organization_id),
  full_name = VALUES(full_name), phone = VALUES(phone), password_hash = VALUES(password_hash),
  status = 'ACTIVE', email_verified = TRUE, failed_login_attempts = 0, locked_until = NULL, deleted_at = NULL;

INSERT INTO users (
  role_id, organization_id, full_name, phone, email, password_hash, status, email_verified, failed_login_attempts
)
SELECT id, @demo_organization_id, 'Kinh doanh Demo', '0901000102',
       'rentai.demo.sales@gmail.com', @demo_password_hash, 'ACTIVE', TRUE, 0
FROM roles WHERE code = 'SALES_STAFF'
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id), organization_id = VALUES(organization_id),
  full_name = VALUES(full_name), phone = VALUES(phone), password_hash = VALUES(password_hash),
  status = 'ACTIVE', email_verified = TRUE, failed_login_attempts = 0, locked_until = NULL, deleted_at = NULL;

INSERT INTO users (
  role_id, organization_id, full_name, phone, email, password_hash, status, email_verified, failed_login_attempts
)
SELECT id, @demo_organization_id, 'Vận hành Demo', '0901000103',
       'rentai.demo.operations@gmail.com', @demo_password_hash, 'ACTIVE', TRUE, 0
FROM roles WHERE code = 'OPERATIONS_STAFF'
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id), organization_id = VALUES(organization_id),
  full_name = VALUES(full_name), phone = VALUES(phone), password_hash = VALUES(password_hash),
  status = 'ACTIVE', email_verified = TRUE, failed_login_attempts = 0, locked_until = NULL, deleted_at = NULL;

INSERT INTO users (
  role_id, organization_id, full_name, phone, email, password_hash, status, email_verified, failed_login_attempts
)
SELECT id, @demo_organization_id, 'Khách hàng Demo', '0901000104',
       'rentai.demo.customer@gmail.com', @demo_password_hash, 'ACTIVE', TRUE, 0
FROM roles WHERE code = 'CUSTOMER'
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id), organization_id = VALUES(organization_id),
  full_name = VALUES(full_name), phone = VALUES(phone), password_hash = VALUES(password_hash),
  status = 'ACTIVE', email_verified = TRUE, failed_login_attempts = 0, locked_until = NULL, deleted_at = NULL;

INSERT IGNORE INTO user_branch_assignments (user_id, branch_id)
SELECT id, @demo_hn_branch_id
FROM users
WHERE email IN (
  'rentai.demo.admin@gmail.com',
  'rentai.demo.manager@gmail.com',
  'rentai.demo.sales@gmail.com',
  'rentai.demo.operations@gmail.com'
);
