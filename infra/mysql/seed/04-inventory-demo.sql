-- Demo catalog, warehouse, equipment and stock records for inventory-service.
USE inventory_db;

SET @demo_organization_id := (
  SELECT id FROM organization_customer_db.organizations WHERE organization_code = 'DEMO-RENTAI' LIMIT 1
);
SET @demo_hn_branch_id := (
  SELECT b.id FROM organization_customer_db.branches b
  WHERE b.organization_id = @demo_organization_id AND b.branch_code = 'DEMO-HN' LIMIT 1
);
SET @demo_hcm_branch_id := (
  SELECT b.id FROM organization_customer_db.branches b
  WHERE b.organization_id = @demo_organization_id AND b.branch_code = 'DEMO-HCM' LIMIT 1
);
SET @demo_admin_user_id := (
  SELECT id FROM identity_db.users WHERE email = 'rentai.demo.admin@gmail.com' LIMIT 1
);
SET @demo_operations_user_id := (
  SELECT id FROM identity_db.users WHERE email = 'rentai.demo.operations@gmail.com' LIMIT 1
);

INSERT INTO equipment_categories (organization_id, code, name, description, active)
VALUES
  (@demo_organization_id, 'DEMO-CAMERA', 'Máy ảnh và quay phim', 'Danh mục thiết bị quay chụp demo.', TRUE),
  (@demo_organization_id, 'DEMO-LAPTOP', 'Máy tính xách tay', 'Danh mục máy tính cho sự kiện demo.', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = TRUE;

SET @demo_camera_category_id := (
  SELECT id FROM equipment_categories WHERE organization_id = @demo_organization_id AND code = 'DEMO-CAMERA' LIMIT 1
);
SET @demo_laptop_category_id := (
  SELECT id FROM equipment_categories WHERE organization_id = @demo_organization_id AND code = 'DEMO-LAPTOP' LIMIT 1
);

INSERT INTO equipment_types (organization_id, category_id, code, name, description, active)
VALUES
  (@demo_organization_id, @demo_camera_category_id, 'DEMO-MIRRORLESS', 'Máy ảnh mirrorless', 'Loại thiết bị dùng cho thuê quay chụp.', TRUE),
  (@demo_organization_id, @demo_laptop_category_id, 'DEMO-LAPTOP-14', 'Laptop 14 inch', 'Loại thiết bị dùng cho thuê sự kiện.', TRUE)
ON DUPLICATE KEY UPDATE category_id = VALUES(category_id), name = VALUES(name), description = VALUES(description), active = TRUE;

SET @demo_camera_type_id := (
  SELECT id FROM equipment_types WHERE organization_id = @demo_organization_id AND code = 'DEMO-MIRRORLESS' LIMIT 1
);
SET @demo_laptop_type_id := (
  SELECT id FROM equipment_types WHERE organization_id = @demo_organization_id AND code = 'DEMO-LAPTOP-14' LIMIT 1
);

INSERT INTO brands (organization_id, code, name, description, active)
VALUES
  (@demo_organization_id, 'DEMO-CANON', 'Canon', 'Hãng máy ảnh demo.', TRUE),
  (@demo_organization_id, 'DEMO-APPLE', 'Apple', 'Hãng laptop demo.', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), active = TRUE;

SET @demo_canon_brand_id := (
  SELECT id FROM brands WHERE organization_id = @demo_organization_id AND code = 'DEMO-CANON' LIMIT 1
);
SET @demo_apple_brand_id := (
  SELECT id FROM brands WHERE organization_id = @demo_organization_id AND code = 'DEMO-APPLE' LIMIT 1
);

INSERT INTO equipment_models (
  organization_id, equipment_type_id, brand_id, code, name, manufacturer_model, description, active
) VALUES
  (@demo_organization_id, @demo_camera_type_id, @demo_canon_brand_id, 'DEMO-CANON-R6', 'Canon EOS R6', 'EOS R6', 'Máy ảnh mirrorless full-frame.', TRUE),
  (@demo_organization_id, @demo_laptop_type_id, @demo_apple_brand_id, 'DEMO-MBP-14', 'MacBook Pro 14', 'A2992', 'Laptop 14 inch cho sự kiện.', TRUE)
ON DUPLICATE KEY UPDATE
  equipment_type_id = VALUES(equipment_type_id), brand_id = VALUES(brand_id), name = VALUES(name),
  manufacturer_model = VALUES(manufacturer_model), description = VALUES(description), active = TRUE;

SET @demo_camera_model_id := (
  SELECT id FROM equipment_models WHERE organization_id = @demo_organization_id AND code = 'DEMO-CANON-R6' LIMIT 1
);
SET @demo_laptop_model_id := (
  SELECT id FROM equipment_models WHERE organization_id = @demo_organization_id AND code = 'DEMO-MBP-14' LIMIT 1
);

INSERT INTO warehouses (organization_id, branch_id, code, name, address, active)
VALUES
  (@demo_organization_id, @demo_hn_branch_id, 'DEMO-HN-WH', 'Kho Hà Nội', 'Cầu Giấy, Hà Nội', TRUE),
  (@demo_organization_id, @demo_hcm_branch_id, 'DEMO-HCM-WH', 'Kho Hồ Chí Minh', 'Quận 7, Hồ Chí Minh', TRUE)
ON DUPLICATE KEY UPDATE branch_id = VALUES(branch_id), name = VALUES(name), address = VALUES(address), active = TRUE;

SET @demo_hn_warehouse_id := (
  SELECT id FROM warehouses WHERE organization_id = @demo_organization_id AND code = 'DEMO-HN-WH' LIMIT 1
);
SET @demo_hcm_warehouse_id := (
  SELECT id FROM warehouses WHERE organization_id = @demo_organization_id AND code = 'DEMO-HCM-WH' LIMIT 1
);

INSERT INTO warehouse_locations (warehouse_id, code, name, zone, rack, shelf, active)
VALUES
  (@demo_hn_warehouse_id, 'DEMO-A01', 'Kệ máy ảnh A01', 'A', '01', '01', TRUE),
  (@demo_hn_warehouse_id, 'DEMO-B01', 'Kệ laptop B01', 'B', '01', '01', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), zone = VALUES(zone), rack = VALUES(rack), shelf = VALUES(shelf), active = TRUE;

SET @demo_camera_location_id := (
  SELECT id FROM warehouse_locations WHERE warehouse_id = @demo_hn_warehouse_id AND code = 'DEMO-A01' LIMIT 1
);
SET @demo_laptop_location_id := (
  SELECT id FROM warehouse_locations WHERE warehouse_id = @demo_hn_warehouse_id AND code = 'DEMO-B01' LIMIT 1
);

INSERT INTO equipment (
  organization_id, branch_id, warehouse_id, warehouse_location_id, model_id, asset_code,
  serial_number, qr_code, status, condition_status, purchase_date, purchase_price, note, version
) VALUES
  (@demo_organization_id, @demo_hn_branch_id, @demo_hn_warehouse_id, @demo_camera_location_id, @demo_camera_model_id,
   'DEMO-CAM-001', 'DEMO-SN-CAM-001', 'DEMO-QR-CAM-001', 'AVAILABLE', 'GOOD', '2026-01-01', 45000000, 'Máy ảnh sẵn sàng cho thuê.', 0),
  (@demo_organization_id, @demo_hn_branch_id, @demo_hn_warehouse_id, @demo_camera_location_id, @demo_camera_model_id,
   'DEMO-CAM-002', 'DEMO-SN-CAM-002', 'DEMO-QR-CAM-002', 'RESERVED', 'GOOD', '2026-01-02', 45000000, 'Máy ảnh đang được giữ chỗ.', 0),
  (@demo_organization_id, @demo_hn_branch_id, @demo_hn_warehouse_id, @demo_laptop_location_id, @demo_laptop_model_id,
   'DEMO-LAP-001', 'DEMO-SN-LAP-001', 'DEMO-QR-LAP-001', 'AVAILABLE', 'GOOD', '2026-01-03', 52000000, 'Laptop sẵn sàng cho thuê.', 0),
  (@demo_organization_id, @demo_hn_branch_id, @demo_hn_warehouse_id, @demo_camera_location_id, @demo_camera_model_id,
   'DEMO-CAM-003', 'DEMO-SN-CAM-003', 'DEMO-QR-CAM-003', 'MAINTENANCE', 'FAIR', '2025-12-15', 45000000, 'Thiết bị đang bảo dưỡng để demo trạng thái.', 0)
ON DUPLICATE KEY UPDATE
  warehouse_id = VALUES(warehouse_id), warehouse_location_id = VALUES(warehouse_location_id), model_id = VALUES(model_id),
  status = VALUES(status), condition_status = VALUES(condition_status), note = VALUES(note);

SET @demo_cam_one_id := (SELECT id FROM equipment WHERE organization_id = @demo_organization_id AND asset_code = 'DEMO-CAM-001' LIMIT 1);
SET @demo_cam_two_id := (SELECT id FROM equipment WHERE organization_id = @demo_organization_id AND asset_code = 'DEMO-CAM-002' LIMIT 1);
SET @demo_laptop_one_id := (SELECT id FROM equipment WHERE organization_id = @demo_organization_id AND asset_code = 'DEMO-LAP-001' LIMIT 1);

INSERT INTO equipment_reservations (
  organization_id, branch_id, reservation_code, request_reference, start_at, end_at, expires_at, status, created_by, version
) VALUES (
  @demo_organization_id, @demo_hn_branch_id, 'DEMO-HOLD-001', 'ORD-DEMO-RESERVED',
  DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY),
  'HELD', @demo_operations_user_id, 0
)
ON DUPLICATE KEY UPDATE
  start_at = VALUES(start_at), end_at = VALUES(end_at), expires_at = VALUES(expires_at), status = 'HELD',
  created_by = VALUES(created_by), rental_order_id = NULL;

SET @demo_reservation_id := (
  SELECT id FROM equipment_reservations WHERE reservation_code = 'DEMO-HOLD-001' LIMIT 1
);

INSERT INTO equipment_reservation_items (
  reservation_id, equipment_type_id, equipment_id, start_at, end_at
)
SELECT @demo_reservation_id, @demo_camera_type_id, @demo_cam_two_id,
       DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY)
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM equipment_reservation_items
  WHERE reservation_id = @demo_reservation_id AND equipment_id = @demo_cam_two_id
);

INSERT INTO stock_in_receipts (
  organization_id, branch_id, warehouse_id, stock_in_code, source_type, reference_code, note,
  status, created_by, confirmed_by, confirmed_at
) VALUES (
  @demo_organization_id, @demo_hn_branch_id, @demo_hn_warehouse_id, 'DEMO-IN-001', 'PURCHASE', 'PO-DEMO-001',
  'Phiếu nhập kho mẫu.', 'CONFIRMED', @demo_operations_user_id, @demo_operations_user_id, NOW()
)
ON DUPLICATE KEY UPDATE status = 'CONFIRMED', note = VALUES(note), confirmed_by = VALUES(confirmed_by), confirmed_at = NOW();

SET @demo_stock_in_id := (
  SELECT id FROM stock_in_receipts WHERE organization_id = @demo_organization_id AND stock_in_code = 'DEMO-IN-001' LIMIT 1
);
INSERT INTO stock_in_items (stock_in_id, equipment_id, note)
SELECT @demo_stock_in_id, @demo_cam_one_id, 'Thiết bị nhập mẫu.' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM stock_in_items WHERE stock_in_id = @demo_stock_in_id AND equipment_id = @demo_cam_one_id);

INSERT INTO stock_out_receipts (
  organization_id, branch_id, warehouse_id, stock_out_code, purpose_type, reference_code, note, status, created_by
) VALUES (
  @demo_organization_id, @demo_hn_branch_id, @demo_hn_warehouse_id, 'DEMO-OUT-001', 'RENTAL', 'ORD-DEMO-RESERVED',
  'Phiếu xuất nháp phục vụ demo.', 'DRAFT', @demo_operations_user_id
)
ON DUPLICATE KEY UPDATE status = 'DRAFT', note = VALUES(note);

SET @demo_stock_out_id := (
  SELECT id FROM stock_out_receipts WHERE organization_id = @demo_organization_id AND stock_out_code = 'DEMO-OUT-001' LIMIT 1
);
INSERT INTO stock_out_items (stock_out_id, equipment_id, note)
SELECT @demo_stock_out_id, @demo_cam_two_id, 'Thiết bị giữ chỗ cho đơn demo.' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM stock_out_items WHERE stock_out_id = @demo_stock_out_id AND equipment_id = @demo_cam_two_id);

INSERT INTO stock_transfers (
  organization_id, transfer_code, from_branch_id, from_warehouse_id, to_branch_id, to_warehouse_id,
  status, created_by, note
) VALUES (
  @demo_organization_id, 'DEMO-TRF-001', @demo_hn_branch_id, @demo_hn_warehouse_id, @demo_hcm_branch_id, @demo_hcm_warehouse_id,
  'DRAFT', @demo_operations_user_id, 'Phiếu chuyển kho mẫu.'
)
ON DUPLICATE KEY UPDATE status = 'DRAFT', note = VALUES(note);

SET @demo_transfer_id := (
  SELECT id FROM stock_transfers WHERE organization_id = @demo_organization_id AND transfer_code = 'DEMO-TRF-001' LIMIT 1
);
INSERT INTO stock_transfer_items (transfer_id, equipment_id, note)
SELECT @demo_transfer_id, @demo_laptop_one_id, 'Laptop chuyển kho mẫu.' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM stock_transfer_items WHERE transfer_id = @demo_transfer_id AND equipment_id = @demo_laptop_one_id);

INSERT INTO stock_audits (
  organization_id, branch_id, warehouse_id, audit_code, status, note, created_by, started_by, started_at
) VALUES (
  @demo_organization_id, @demo_hn_branch_id, @demo_hn_warehouse_id, 'DEMO-AUDIT-001', 'IN_PROGRESS',
  'Phiếu kiểm kê mẫu.', @demo_operations_user_id, @demo_operations_user_id, NOW()
)
ON DUPLICATE KEY UPDATE status = 'IN_PROGRESS', note = VALUES(note), started_at = NOW();

SET @demo_audit_id := (
  SELECT id FROM stock_audits WHERE organization_id = @demo_organization_id AND audit_code = 'DEMO-AUDIT-001' LIMIT 1
);
INSERT INTO stock_audit_items (
  stock_audit_id, equipment_id, expected_warehouse_id, expected_location_id, actual_warehouse_id,
  actual_location_id, result, note, checked_by, checked_at
)
SELECT @demo_audit_id, @demo_cam_one_id, @demo_hn_warehouse_id, @demo_camera_location_id,
       @demo_hn_warehouse_id, @demo_camera_location_id, 'MATCHED', 'Khớp tồn kho.', @demo_operations_user_id, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM stock_audit_items WHERE stock_audit_id = @demo_audit_id AND equipment_id = @demo_cam_one_id);
