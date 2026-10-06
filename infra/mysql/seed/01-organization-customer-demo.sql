-- Demo data for organization-customer-service.
-- This file is repeatable: it only creates or refreshes records whose code starts with DEMO-.
USE organization_customer_db;

START TRANSACTION;

INSERT INTO organizations (
  organization_code, organization_name, tax_code, email, phone, address, status
) VALUES (
  'DEMO-RENTAI', 'RentAI Demo Equipment', '0109998888',
  'rentai.demo.company@gmail.com', '0901000000', 'Hà Nội', 'ACTIVE'
)
ON DUPLICATE KEY UPDATE
  organization_name = VALUES(organization_name), email = VALUES(email),
  phone = VALUES(phone), address = VALUES(address), status = 'ACTIVE', deleted_at = NULL;

SET @demo_organization_id := (
  SELECT id FROM organizations WHERE organization_code = 'DEMO-RENTAI' LIMIT 1
);

INSERT INTO branches (
  organization_id, branch_code, branch_name, email, phone, address, status
) VALUES
  (@demo_organization_id, 'DEMO-HN', 'Chi nhánh Hà Nội', 'rentai.demo.hn@gmail.com', '0901000001', 'Cầu Giấy, Hà Nội', 'ACTIVE'),
  (@demo_organization_id, 'DEMO-HCM', 'Chi nhánh Hồ Chí Minh', 'rentai.demo.hcm@gmail.com', '0901000002', 'Quận 7, Hồ Chí Minh', 'ACTIVE')
ON DUPLICATE KEY UPDATE
  branch_name = VALUES(branch_name), email = VALUES(email), phone = VALUES(phone),
  address = VALUES(address), status = 'ACTIVE', deleted_at = NULL;

SET @demo_hn_branch_id := (
  SELECT id FROM branches WHERE organization_id = @demo_organization_id AND branch_code = 'DEMO-HN' LIMIT 1
);

INSERT INTO customers (
  organization_id, branch_id, customer_code, customer_type, display_name,
  email, phone, address, full_name, identity_number, status, note
) VALUES
  (@demo_organization_id, @demo_hn_branch_id, 'DEMO-CUS-IND', 'INDIVIDUAL', 'Nguyễn Minh Anh',
   'minhanh.demo@gmail.com', '0901000010', 'Ba Đình, Hà Nội', 'Nguyễn Minh Anh', '001205000001', 'ACTIVE', 'Khách hàng cá nhân dùng cho demo.'),
  (@demo_organization_id, @demo_hn_branch_id, 'DEMO-CUS-BIZ', 'BUSINESS', 'Công ty Sự kiện Sao Việt',
   'saoviet.demo@gmail.com', '0901000011', 'Nam Từ Liêm, Hà Nội', NULL, NULL, 'ACTIVE', 'Khách doanh nghiệp dùng cho báo giá demo.')
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name), email = VALUES(email), phone = VALUES(phone),
  address = VALUES(address), status = 'ACTIVE', note = VALUES(note), deleted_at = NULL;

SET @demo_business_customer_id := (
  SELECT id FROM customers WHERE organization_id = @demo_organization_id AND customer_code = 'DEMO-CUS-BIZ' LIMIT 1
);

INSERT INTO customer_groups (
  organization_id, group_code, group_name, description, status
) VALUES (
  @demo_organization_id, 'DEMO-VIP', 'Khách hàng ưu tiên', 'Nhóm khách hàng doanh nghiệp cho demo.', 'ACTIVE'
)
ON DUPLICATE KEY UPDATE
  group_name = VALUES(group_name), description = VALUES(description), status = 'ACTIVE', deleted_at = NULL;

SET @demo_vip_group_id := (
  SELECT id FROM customer_groups WHERE organization_id = @demo_organization_id AND group_code = 'DEMO-VIP' LIMIT 1
);

INSERT IGNORE INTO customer_group_members (
  organization_id, customer_group_id, customer_id
) VALUES (@demo_organization_id, @demo_vip_group_id, @demo_business_customer_id);

COMMIT;
