-- Employee records must be loaded after 02-identity-demo.sql so their user_id values are available.
USE organization_customer_db;

SET @demo_organization_id := (
  SELECT id FROM organizations WHERE organization_code = 'DEMO-RENTAI' LIMIT 1
);
SET @demo_hn_branch_id := (
  SELECT id FROM branches WHERE organization_id = @demo_organization_id AND branch_code = 'DEMO-HN' LIMIT 1
);
SET @demo_admin_user_id := (
  SELECT id FROM identity_db.users WHERE email = 'rentai.demo.admin@gmail.com' LIMIT 1
);
SET @demo_manager_user_id := (
  SELECT id FROM identity_db.users WHERE email = 'rentai.demo.manager@gmail.com' LIMIT 1
);
SET @demo_sales_user_id := (
  SELECT id FROM identity_db.users WHERE email = 'rentai.demo.sales@gmail.com' LIMIT 1
);
SET @demo_operations_user_id := (
  SELECT id FROM identity_db.users WHERE email = 'rentai.demo.operations@gmail.com' LIMIT 1
);

INSERT INTO employees (
  organization_id, user_id, employee_code, full_name, email, phone, job_title, status, hire_date, created_by, updated_by
) VALUES
  (@demo_organization_id, @demo_manager_user_id, 'DEMO-EMP-MGR', 'Quản lý Demo', 'rentai.demo.manager@gmail.com', '0901000101', 'Quản lý chi nhánh', 'ACTIVE', '2026-01-01', @demo_admin_user_id, @demo_admin_user_id),
  (@demo_organization_id, @demo_sales_user_id, 'DEMO-EMP-SALES', 'Kinh doanh Demo', 'rentai.demo.sales@gmail.com', '0901000102', 'Nhân viên kinh doanh', 'ACTIVE', '2026-01-01', @demo_admin_user_id, @demo_admin_user_id),
  (@demo_organization_id, @demo_operations_user_id, 'DEMO-EMP-OPS', 'Vận hành Demo', 'rentai.demo.operations@gmail.com', '0901000103', 'Nhân viên vận hành', 'ACTIVE', '2026-01-01', @demo_admin_user_id, @demo_admin_user_id)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id), full_name = VALUES(full_name), email = VALUES(email), phone = VALUES(phone),
  job_title = VALUES(job_title), status = 'ACTIVE', deleted_at = NULL, updated_by = VALUES(updated_by);

INSERT IGNORE INTO employee_branch_assignments (
  organization_id, employee_id, branch_id, is_primary, assigned_from, status, created_by, updated_by
)
SELECT @demo_organization_id, id, @demo_hn_branch_id, TRUE, '2026-01-01', 'ACTIVE', @demo_admin_user_id, @demo_admin_user_id
FROM employees
WHERE organization_id = @demo_organization_id
  AND employee_code IN ('DEMO-EMP-MGR', 'DEMO-EMP-SALES', 'DEMO-EMP-OPS');

UPDATE customers
SET owner_user_id = @demo_sales_user_id, updated_by = @demo_admin_user_id
WHERE organization_id = @demo_organization_id
  AND customer_code IN ('DEMO-CUS-IND', 'DEMO-CUS-BIZ');
