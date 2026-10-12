-- Additive demo catalog. No password, reservation, workflow or existing asset reset.
-- Explicit connection charset also makes manual mysql < file imports safe.
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE inventory_db;

SET @catalog_org := (SELECT id FROM organization_customer_db.organizations
  WHERE organization_code = 'DEMO-RENTAI' LIMIT 1);
SET @catalog_branch := (SELECT id FROM organization_customer_db.branches
  WHERE organization_id = @catalog_org AND branch_code = 'DEMO-HN' LIMIT 1);
SET @catalog_warehouse := (SELECT id FROM warehouses
  WHERE organization_id = @catalog_org AND branch_id = @catalog_branch
    AND code = 'DEMO-HN-WH' AND active = TRUE LIMIT 1);

-- Fail before touching persistent rows when the base demo has not been loaded.
CREATE TEMPORARY TABLE catalog_context (
  organization_id BIGINT NOT NULL, branch_id BIGINT NOT NULL, warehouse_id BIGINT NOT NULL
);
INSERT INTO catalog_context VALUES (@catalog_org, @catalog_branch, @catalog_warehouse);

CREATE TEMPORARY TABLE catalog_categories (
  code VARCHAR(50) PRIMARY KEY, name VARCHAR(150), description VARCHAR(500)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO catalog_categories VALUES
  ('DEMO-CAMERA', 'Máy ảnh và quay phim', 'Danh mục thiết bị quay chụp demo.'),
  ('DEMO-LAPTOP', 'Máy tính xách tay', 'Danh mục máy tính cho sự kiện demo.'),
  ('DEMO-PROJECTION', 'Trình chiếu', 'Thiết bị trình chiếu cho hội họp và sự kiện.'),
  ('DEMO-AUDIO', 'Âm thanh', 'Loa và micro cho hội thảo, sự kiện.'),
  ('DEMO-LIGHTING', 'Ánh sáng', 'Thiết bị chiếu sáng cho quay phim và chụp ảnh.'),
  ('DEMO-ACCESSORIES', 'Phụ kiện quay chụp', 'Phụ kiện hỗ trợ máy ảnh và quay phim.');

CREATE TEMPORARY TABLE catalog_types (
  code VARCHAR(50) PRIMARY KEY, category_code VARCHAR(50), name VARCHAR(150), description VARCHAR(500)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO catalog_types VALUES
  ('DEMO-MIRRORLESS', 'DEMO-CAMERA', 'Máy ảnh mirrorless', 'Loại thiết bị dùng cho thuê quay chụp.'),
  ('DEMO-LAPTOP-14', 'DEMO-LAPTOP', 'Laptop 14 inch', 'Loại thiết bị dùng cho thuê sự kiện.'),
  ('DEMO-PROJECTOR', 'DEMO-PROJECTION', 'Máy chiếu Full HD', 'Trình chiếu slide, hình ảnh và video trong phòng họp hoặc sự kiện trong nhà.'),
  ('DEMO-SPEAKER', 'DEMO-AUDIO', 'Loa sự kiện di động', 'Loa di động phục vụ hội thảo, sinh hoạt nhóm và sự kiện quy mô nhỏ.'),
  ('DEMO-WIRELESS-MIC', 'DEMO-AUDIO', 'Bộ micro không dây', 'Bộ thu và hai micro cầm tay cho thuyết trình, hội họp và dẫn chương trình.'),
  ('DEMO-LED-LIGHT', 'DEMO-LIGHTING', 'Đèn LED quay phim', 'Đèn LED kèm chân đèn, hỗ trợ ánh sáng khi quay video và chụp sản phẩm.'),
  ('DEMO-TRIPOD', 'DEMO-ACCESSORIES', 'Chân máy ảnh tripod', 'Chân máy ba chân, phù hợp chụp ảnh cố định và quay video.'),
  ('DEMO-ACTION-CAM', 'DEMO-CAMERA', 'Camera hành trình 4K', 'Camera nhỏ gọn cho hoạt động ngoài trời, du lịch và ghi hình chuyển động.');

CREATE TEMPORARY TABLE catalog_brands (
  code VARCHAR(50) PRIMARY KEY, name VARCHAR(150), description VARCHAR(500)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO catalog_brands VALUES
  ('DEMO-CANON', 'Canon', 'Hãng máy ảnh demo.'),
  ('DEMO-APPLE', 'Apple', 'Hãng laptop demo.'),
  ('DEMO-EPSON', 'Epson', 'Thương hiệu máy chiếu demo.'),
  ('DEMO-JBL', 'JBL', 'Thương hiệu âm thanh demo.'),
  ('DEMO-SHURE', 'Shure', 'Thương hiệu micro demo.'),
  ('DEMO-GODOX', 'Godox', 'Thương hiệu ánh sáng demo.'),
  ('DEMO-BENRO', 'Benro', 'Thương hiệu chân máy demo.'),
  ('DEMO-GOPRO', 'GoPro', 'Thương hiệu camera hành trình demo.');

CREATE TEMPORARY TABLE catalog_models (
  code VARCHAR(50) PRIMARY KEY, type_code VARCHAR(50), brand_code VARCHAR(50),
  name VARCHAR(150), manufacturer_model VARCHAR(150), description VARCHAR(1000),
  asset_prefix VARCHAR(50), purchase_price DECIMAL(15,2)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO catalog_models VALUES
  ('DEMO-CANON-R6', 'DEMO-MIRRORLESS', 'DEMO-CANON', 'Canon EOS R6', 'EOS R6', 'Máy ảnh mirrorless full-frame.', 'CAM', 45000000),
  ('DEMO-MBP-14', 'DEMO-LAPTOP-14', 'DEMO-APPLE', 'MacBook Pro 14', 'A2992', 'Laptop 14 inch cho sự kiện.', 'LAP', 52000000),
  ('DEMO-EPSON-FHD', 'DEMO-PROJECTOR', 'DEMO-EPSON', 'Epson Full HD Demo', 'DEMO-FHD', 'Máy chiếu dùng thử cho luồng thuê thiết bị.', 'PROJ', 18000000),
  ('DEMO-JBL-PORTABLE', 'DEMO-SPEAKER', 'DEMO-JBL', 'JBL Portable Demo', 'DEMO-PORTABLE', 'Loa di động dùng thử cho sự kiện.', 'SPK', 12000000),
  ('DEMO-SHURE-DUAL', 'DEMO-WIRELESS-MIC', 'DEMO-SHURE', 'Shure Dual Demo', 'DEMO-DUAL', 'Bộ hai micro không dây và bộ thu.', 'MIC', 8000000),
  ('DEMO-GODOX-LED', 'DEMO-LED-LIGHT', 'DEMO-GODOX', 'Godox LED Demo', 'DEMO-LED', 'Đèn LED kèm chân đèn cho quay phim.', 'LED', 4500000),
  ('DEMO-BENRO-TRIPOD', 'DEMO-TRIPOD', 'DEMO-BENRO', 'Benro Tripod Demo', 'DEMO-TRIPOD', 'Chân máy ảnh dùng thử cho thuê.', 'TRIPOD', 2500000),
  ('DEMO-GOPRO-4K', 'DEMO-ACTION-CAM', 'DEMO-GOPRO', 'GoPro 4K Demo', 'DEMO-4K', 'Camera hành trình dùng thử cho thuê.', 'ACTION', 9000000);

START TRANSACTION;

-- Insert only missing master data. Existing activation/category/brand choices stay unchanged.
INSERT INTO equipment_categories (organization_id, code, name, description, active)
SELECT @catalog_org, s.code, s.name, s.description, TRUE FROM catalog_categories s
WHERE NOT EXISTS (SELECT 1 FROM equipment_categories t WHERE t.organization_id = @catalog_org AND t.code = s.code);
INSERT INTO equipment_types (organization_id, category_id, code, name, description, active)
SELECT @catalog_org, c.id, s.code, s.name, s.description, TRUE FROM catalog_types s
JOIN equipment_categories c ON c.organization_id = @catalog_org AND c.code = s.category_code
WHERE NOT EXISTS (SELECT 1 FROM equipment_types t WHERE t.organization_id = @catalog_org AND t.code = s.code);
INSERT INTO brands (organization_id, code, name, description, active)
SELECT @catalog_org, s.code, s.name, s.description, TRUE FROM catalog_brands s
WHERE NOT EXISTS (SELECT 1 FROM brands t WHERE t.organization_id = @catalog_org AND t.code = s.code);
INSERT INTO equipment_models (organization_id, equipment_type_id, brand_id, code, name, manufacturer_model, description, active)
SELECT @catalog_org, t.id, b.id, s.code, s.name, s.manufacturer_model, s.description, TRUE FROM catalog_models s
JOIN equipment_types t ON t.organization_id = @catalog_org AND t.code = s.type_code
JOIN brands b ON b.organization_id = @catalog_org AND b.code = s.brand_code
WHERE NOT EXISTS (SELECT 1 FROM equipment_models m WHERE m.organization_id = @catalog_org AND m.code = s.code);

-- Repair ONLY an exact latin1-misdecoded copy of the known demo string.
-- Correct text and user-edited descriptions are not overwritten.
UPDATE equipment_categories t JOIN catalog_categories s ON t.code = s.code
SET t.name = IF(BINARY t.name = BINARY CONVERT(CONVERT(CAST(s.name AS BINARY) USING latin1) USING utf8mb4), s.name, t.name),
    t.description = IF(BINARY t.description = BINARY CONVERT(CONVERT(CAST(s.description AS BINARY) USING latin1) USING utf8mb4), s.description, t.description)
WHERE t.organization_id = @catalog_org;
UPDATE equipment_types t JOIN catalog_types s ON t.code = s.code
SET t.name = IF(BINARY t.name = BINARY CONVERT(CONVERT(CAST(s.name AS BINARY) USING latin1) USING utf8mb4), s.name, t.name),
    t.description = IF(BINARY t.description = BINARY CONVERT(CONVERT(CAST(s.description AS BINARY) USING latin1) USING utf8mb4), s.description, t.description)
WHERE t.organization_id = @catalog_org;
UPDATE brands t JOIN catalog_brands s ON t.code = s.code
SET t.name = IF(BINARY t.name = BINARY CONVERT(CONVERT(CAST(s.name AS BINARY) USING latin1) USING utf8mb4), s.name, t.name),
    t.description = IF(BINARY t.description = BINARY CONVERT(CONVERT(CAST(s.description AS BINARY) USING latin1) USING utf8mb4), s.description, t.description)
WHERE t.organization_id = @catalog_org;
UPDATE equipment_models t JOIN catalog_models s ON t.code = s.code
SET t.name = IF(BINARY t.name = BINARY CONVERT(CONVERT(CAST(s.name AS BINARY) USING latin1) USING utf8mb4), s.name, t.name),
    t.description = IF(BINARY t.description = BINARY CONVERT(CONVERT(CAST(s.description AS BINARY) USING latin1) USING utf8mb4), s.description, t.description)
WHERE t.organization_id = @catalog_org;

-- Two additional physical assets per type. 101/102 avoid the base seed's 001-003.
-- Re-running NEVER makes an existing reserved/rented/maintenance asset AVAILABLE.
INSERT INTO equipment (organization_id, branch_id, warehouse_id, model_id, asset_code,
  serial_number, qr_code, status, condition_status, purchase_date, purchase_price, note, version)
SELECT @catalog_org, @catalog_branch, @catalog_warehouse, m.id,
  CONCAT('DEMO-', s.asset_prefix, '-', n.unit), CONCAT('DEMO-SN-', s.asset_prefix, '-', n.unit),
  CONCAT('DEMO-QR-', s.asset_prefix, '-', n.unit), 'AVAILABLE', 'GOOD', '2026-01-01', s.purchase_price,
  'Thiết bị bổ sung cho danh mục thuê demo.', 0
FROM catalog_models s
JOIN equipment_models m ON m.organization_id = @catalog_org AND m.code = s.code
JOIN equipment_types t ON t.id = m.equipment_type_id AND t.organization_id = @catalog_org AND t.active = TRUE
CROSS JOIN (SELECT '101' AS unit UNION ALL SELECT '102') n
WHERE m.active = TRUE AND NOT EXISTS (
  SELECT 1 FROM equipment e WHERE e.organization_id = @catalog_org AND e.asset_code = CONCAT('DEMO-', s.asset_prefix, '-', n.unit)
);

COMMIT;

SELECT t.code, t.name,
  SUM(CASE WHEN e.branch_id = @catalog_branch AND e.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_status_in_hanoi
FROM equipment_types t JOIN catalog_types s ON s.code = t.code
LEFT JOIN equipment_models m ON m.equipment_type_id = t.id
LEFT JOIN equipment e ON e.model_id = m.id AND e.organization_id = @catalog_org
WHERE t.organization_id = @catalog_org
GROUP BY t.id, t.code, t.name ORDER BY t.id;
-- Status counts are NOT date-range availability; use the availability API for that.
