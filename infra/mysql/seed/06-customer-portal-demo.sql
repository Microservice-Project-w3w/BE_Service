SET NAMES utf8mb4;
USE organization_customer_db;

-- Only bind the existing demo account. Do not reset passwords or rental progress.
INSERT IGNORE INTO customer_portal_accounts (user_id, customer_id)
SELECT u.id, c.id
FROM identity_db.users u
JOIN identity_db.roles r ON r.id = u.role_id AND r.code = 'CUSTOMER'
JOIN customers c ON c.organization_id = u.organization_id
JOIN organizations o ON o.id = c.organization_id
WHERE u.email = 'rentai.demo.customer@gmail.com' AND u.status = 'ACTIVE'
  AND o.organization_code = 'DEMO-RENTAI' AND c.customer_code = 'DEMO-CUS-BIZ'
  AND c.deleted_at IS NULL AND c.status = 'ACTIVE';

INSERT IGNORE INTO identity_db.user_branch_assignments (user_id, branch_id)
SELECT p.user_id, c.branch_id
FROM customer_portal_accounts p
JOIN customers c ON c.id = p.customer_id
JOIN identity_db.users u ON u.id = p.user_id AND u.organization_id = c.organization_id
WHERE u.email = 'rentai.demo.customer@gmail.com' AND c.branch_id IS NOT NULL;
