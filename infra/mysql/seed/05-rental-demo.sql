-- Demo workflow data for rental-service: request -> quotation -> order -> contract.
USE rental_db;

SET @demo_organization_id := (
  SELECT id FROM organization_customer_db.organizations WHERE organization_code = 'DEMO-RENTAI' LIMIT 1
);
SET @demo_hn_branch_id := (
  SELECT id FROM organization_customer_db.branches
  WHERE organization_id = @demo_organization_id AND branch_code = 'DEMO-HN' LIMIT 1
);
SET @demo_business_customer_id := (
  SELECT id FROM organization_customer_db.customers
  WHERE organization_id = @demo_organization_id AND customer_code = 'DEMO-CUS-BIZ' LIMIT 1
);
SET @demo_camera_type_id := (
  SELECT id FROM inventory_db.equipment_types
  WHERE organization_id = @demo_organization_id AND code = 'DEMO-MIRRORLESS' LIMIT 1
);
SET @demo_manager_user_id := (
  SELECT id FROM identity_db.users WHERE email = 'rentai.demo.manager@gmail.com' LIMIT 1
);
SET @demo_inventory_reservation_id := (
  SELECT id FROM inventory_db.equipment_reservations
  WHERE organization_id = @demo_organization_id AND reservation_code = 'DEMO-HOLD-001' LIMIT 1
);

INSERT INTO rental_prices (
  price_name, organization_id, branch_id, equipment_type_id, rental_unit, rental_price,
  deposit_type, deposit_value, late_fee, valid_from, active, description
) VALUES (
  'Bảng giá máy ảnh demo', @demo_organization_id, @demo_hn_branch_id, @demo_camera_type_id, 'DAY', 1500000,
  'PERCENT', 30, 200000, '2026-01-01 00:00:00', TRUE, 'Bảng giá dùng cho luồng thuê demo.'
)
ON DUPLICATE KEY UPDATE
  price_name = VALUES(price_name), rental_price = VALUES(rental_price), deposit_value = VALUES(deposit_value),
  late_fee = VALUES(late_fee), active = TRUE, description = VALUES(description);

INSERT INTO discount_codes (
  organization_id, branch_id, code, name, discount_type, discount_value, max_discount,
  min_order_value, valid_from, valid_to, active
) VALUES (
  @demo_organization_id, @demo_hn_branch_id, 'DEMO10', 'Giảm giá demo 10%', 'PERCENT', 10, 1000000,
  1000000, '2026-01-01 00:00:00', '2030-12-31 23:59:59', TRUE
)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), discount_value = VALUES(discount_value), max_discount = VALUES(max_discount), active = TRUE;

INSERT INTO rental_requests (
  organization_id, branch_id, request_code, customer_id, start_at, end_at, delivery_address, note, status
) VALUES
  (@demo_organization_id, @demo_hn_branch_id, 'REQ-DEMO-NEW', @demo_business_customer_id,
   DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 16 DAY), 'Nam Từ Liêm, Hà Nội', 'Yêu cầu mới để Sales lập báo giá.', 'SUBMITTED'),
  (@demo_organization_id, @demo_hn_branch_id, 'REQ-DEMO-QUOTE', @demo_business_customer_id,
   DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 12 DAY), 'Nam Từ Liêm, Hà Nội', 'Yêu cầu đã có báo giá chờ quản lý duyệt.', 'QUOTED'),
  (@demo_organization_id, @demo_hn_branch_id, 'REQ-DEMO-ORDER', @demo_business_customer_id,
   DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 'Nam Từ Liêm, Hà Nội', 'Yêu cầu đã chuyển thành đơn thuê và hợp đồng chờ duyệt.', 'QUOTED'),
  (@demo_organization_id, @demo_hn_branch_id, 'REQ-DEMO-ACTIVE', @demo_business_customer_id,
   DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 'Nam Từ Liêm, Hà Nội', 'Yêu cầu đang có hợp đồng hiệu lực.', 'QUOTED'),
  (@demo_organization_id, @demo_hn_branch_id, 'REQ-DEMO-REJECTED', @demo_business_customer_id,
   DATE_ADD(NOW(), INTERVAL 21 DAY), DATE_ADD(NOW(), INTERVAL 23 DAY), 'Nam Từ Liêm, Hà Nội', 'Yêu cầu có báo giá đã từ chối để demo lịch sử.', 'REJECTED')
ON DUPLICATE KEY UPDATE
  customer_id = VALUES(customer_id), start_at = VALUES(start_at), end_at = VALUES(end_at),
  delivery_address = VALUES(delivery_address), note = VALUES(note), status = VALUES(status);

SET @demo_request_new_id := (SELECT id FROM rental_requests WHERE request_code = 'REQ-DEMO-NEW' LIMIT 1);
SET @demo_request_quote_id := (SELECT id FROM rental_requests WHERE request_code = 'REQ-DEMO-QUOTE' LIMIT 1);
SET @demo_request_order_id := (SELECT id FROM rental_requests WHERE request_code = 'REQ-DEMO-ORDER' LIMIT 1);
SET @demo_request_active_id := (SELECT id FROM rental_requests WHERE request_code = 'REQ-DEMO-ACTIVE' LIMIT 1);
SET @demo_request_rejected_id := (SELECT id FROM rental_requests WHERE request_code = 'REQ-DEMO-REJECTED' LIMIT 1);

INSERT INTO rental_request_items (rental_request_id, equipment_type_id, quantity)
SELECT @demo_request_new_id, @demo_camera_type_id, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rental_request_items WHERE rental_request_id = @demo_request_new_id AND equipment_type_id = @demo_camera_type_id);
INSERT INTO rental_request_items (rental_request_id, equipment_type_id, quantity)
SELECT @demo_request_quote_id, @demo_camera_type_id, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rental_request_items WHERE rental_request_id = @demo_request_quote_id AND equipment_type_id = @demo_camera_type_id);
INSERT INTO rental_request_items (rental_request_id, equipment_type_id, quantity)
SELECT @demo_request_order_id, @demo_camera_type_id, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rental_request_items WHERE rental_request_id = @demo_request_order_id AND equipment_type_id = @demo_camera_type_id);
INSERT INTO rental_request_items (rental_request_id, equipment_type_id, quantity)
SELECT @demo_request_active_id, @demo_camera_type_id, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rental_request_items WHERE rental_request_id = @demo_request_active_id AND equipment_type_id = @demo_camera_type_id);
INSERT INTO rental_request_items (rental_request_id, equipment_type_id, quantity)
SELECT @demo_request_rejected_id, @demo_camera_type_id, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rental_request_items WHERE rental_request_id = @demo_request_rejected_id AND equipment_type_id = @demo_camera_type_id);

INSERT INTO quotations (
  organization_id, branch_id, quotation_code, rental_request_id, customer_id, rental_amount,
  deposit_amount, delivery_fee, discount_amount, total_amount, discount_code, status, valid_until, special_terms
) VALUES
  (@demo_organization_id, @demo_hn_branch_id, 'QT-DEMO-PENDING', @demo_request_quote_id, @demo_business_customer_id,
   3000000, 900000, 200000, 0, 4100000, NULL, 'PENDING_APPROVAL', DATE_ADD(NOW(), INTERVAL 5 DAY), 'Báo giá chờ Manager duyệt.'),
  (@demo_organization_id, @demo_hn_branch_id, 'QT-DEMO-CONVERTED', @demo_request_order_id, @demo_business_customer_id,
   4500000, 1350000, 200000, 450000, 5600000, 'DEMO10', 'CONVERTED', DATE_ADD(NOW(), INTERVAL 5 DAY), 'Báo giá đã chuyển thành đơn thuê.'),
  (@demo_organization_id, @demo_hn_branch_id, 'QT-DEMO-ACTIVE', @demo_request_active_id, @demo_business_customer_id,
   4500000, 1350000, 200000, 0, 6050000, NULL, 'CONVERTED', DATE_ADD(NOW(), INTERVAL 1 DAY), 'Báo giá của hợp đồng đang hiệu lực.'),
  (@demo_organization_id, @demo_hn_branch_id, 'QT-DEMO-REJECTED', @demo_request_rejected_id, @demo_business_customer_id,
   3000000, 900000, 200000, 0, 4100000, NULL, 'REJECTED', DATE_ADD(NOW(), INTERVAL 5 DAY), 'Báo giá dùng để demo trạng thái từ chối.')
ON DUPLICATE KEY UPDATE
  rental_request_id = VALUES(rental_request_id), customer_id = VALUES(customer_id), rental_amount = VALUES(rental_amount),
  deposit_amount = VALUES(deposit_amount), delivery_fee = VALUES(delivery_fee), discount_amount = VALUES(discount_amount),
  total_amount = VALUES(total_amount), discount_code = VALUES(discount_code), status = VALUES(status),
  valid_until = VALUES(valid_until), special_terms = VALUES(special_terms);

UPDATE quotations
SET rejection_reason = 'Ngân sách khách hàng chưa phù hợp.'
WHERE quotation_code = 'QT-DEMO-REJECTED';

SET @demo_quote_order_id := (SELECT id FROM quotations WHERE quotation_code = 'QT-DEMO-CONVERTED' LIMIT 1);
SET @demo_quote_active_id := (SELECT id FROM quotations WHERE quotation_code = 'QT-DEMO-ACTIVE' LIMIT 1);

INSERT INTO rental_orders (
  organization_id, branch_id, order_code, quotation_id, customer_id, start_at, end_at,
  total_amount, status, reserved_until, inventory_reservation_id
) VALUES
  (@demo_organization_id, @demo_hn_branch_id, 'ORD-DEMO-RESERVED', @demo_quote_order_id, @demo_business_customer_id,
   DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 5600000, 'RESERVED', DATE_ADD(NOW(), INTERVAL 2 DAY), CAST(@demo_inventory_reservation_id AS CHAR)),
  (@demo_organization_id, @demo_hn_branch_id, 'ORD-DEMO-ACTIVE', @demo_quote_active_id, @demo_business_customer_id,
   DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 6050000, 'CONFIRMED', NULL, NULL)
ON DUPLICATE KEY UPDATE
  quotation_id = VALUES(quotation_id), customer_id = VALUES(customer_id), start_at = VALUES(start_at), end_at = VALUES(end_at),
  total_amount = VALUES(total_amount), status = VALUES(status), reserved_until = VALUES(reserved_until),
  inventory_reservation_id = VALUES(inventory_reservation_id), cancel_reason = NULL;

SET @demo_order_reserved_id := (SELECT id FROM rental_orders WHERE order_code = 'ORD-DEMO-RESERVED' LIMIT 1);
SET @demo_order_active_id := (SELECT id FROM rental_orders WHERE order_code = 'ORD-DEMO-ACTIVE' LIMIT 1);

UPDATE inventory_db.equipment_reservations
SET rental_order_id = @demo_order_reserved_id
WHERE reservation_code = 'DEMO-HOLD-001';

INSERT INTO rental_contracts (
  organization_id, branch_id, contract_code, rental_order_id, customer_id, start_at, end_at,
  total_amount, status, terms
) VALUES
  (@demo_organization_id, @demo_hn_branch_id, 'CTR-DEMO-PENDING', @demo_order_reserved_id, @demo_business_customer_id,
   DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 5600000, 'PENDING_APPROVAL',
   'Hợp đồng mẫu chờ Manager duyệt trong demo.'),
  (@demo_organization_id, @demo_hn_branch_id, 'CTR-DEMO-ACTIVE', @demo_order_active_id, @demo_business_customer_id,
   DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 6050000, 'ACTIVE',
   'Hợp đồng mẫu đang hiệu lực trong demo.')
ON DUPLICATE KEY UPDATE
  customer_id = VALUES(customer_id), start_at = VALUES(start_at), end_at = VALUES(end_at),
  total_amount = VALUES(total_amount), status = VALUES(status), terms = VALUES(terms),
  cancel_reason = NULL;

UPDATE rental_contracts
SET approved_at = CASE WHEN status = 'ACTIVE' THEN DATE_SUB(NOW(), INTERVAL 2 DAY) ELSE NULL END,
    signed_at = CASE WHEN status = 'ACTIVE' THEN DATE_SUB(NOW(), INTERVAL 1 DAY) ELSE NULL END
WHERE contract_code IN ('CTR-DEMO-PENDING', 'CTR-DEMO-ACTIVE');

SET @demo_active_contract_id := (SELECT id FROM rental_contracts WHERE contract_code = 'CTR-DEMO-ACTIVE' LIMIT 1);
INSERT INTO contract_appendices (
  organization_id, branch_id, contract_id, appendix_code, appendix_type, status, new_end_at, terms
) VALUES (
  @demo_organization_id, @demo_hn_branch_id, @demo_active_contract_id, 'APP-DEMO-EXT-001', 'EXTENSION', 'PENDING_APPROVAL',
  DATE_ADD(NOW(), INTERVAL 7 DAY), 'Phụ lục gia hạn mẫu chờ duyệt.'
)
ON DUPLICATE KEY UPDATE
  contract_id = VALUES(contract_id), status = 'PENDING_APPROVAL', new_end_at = VALUES(new_end_at), terms = VALUES(terms),
  approved_at = NULL, signed_at = NULL;
