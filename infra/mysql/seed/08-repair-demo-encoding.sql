-- Repair ONLY exact UTF-8 bytes previously imported as MySQL latin1 (Windows-1252).
-- Known demo fields only; no inserts, status/date/amount/password/ownership changes.
-- Set @demo_encoding_apply=0 in the same session for a read-only preview.
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rental_db;
SET @demo_encoding_apply := COALESCE(@demo_encoding_apply, 1);
SET @encoding_org := (SELECT id FROM organization_customer_db.organizations
  WHERE organization_code = 'DEMO-RENTAI' LIMIT 1);

CREATE TEMPORARY TABLE demo_text_expected (
  table_name VARCHAR(64), field_name VARCHAR(64), record_code VARCHAR(64),
  expected_text TEXT, broken_text TEXT,
  PRIMARY KEY (table_name, field_name, record_code)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO demo_text_expected (table_name, field_name, record_code, expected_text) VALUES
  ('rental_requests', 'delivery_address', 'REQ-DEMO-NEW', 'Nam Từ Liêm, Hà Nội'),
  ('rental_requests', 'note', 'REQ-DEMO-NEW', 'Yêu cầu mới để Sales lập báo giá.'),
  ('rental_requests', 'delivery_address', 'REQ-DEMO-QUOTE', 'Nam Từ Liêm, Hà Nội'),
  ('rental_requests', 'note', 'REQ-DEMO-QUOTE', 'Yêu cầu đã có báo giá chờ quản lý duyệt.'),
  ('rental_requests', 'delivery_address', 'REQ-DEMO-ORDER', 'Nam Từ Liêm, Hà Nội'),
  ('rental_requests', 'note', 'REQ-DEMO-ORDER', 'Yêu cầu đã chuyển thành đơn thuê và hợp đồng chờ duyệt.'),
  ('rental_requests', 'delivery_address', 'REQ-DEMO-ACTIVE', 'Nam Từ Liêm, Hà Nội'),
  ('rental_requests', 'note', 'REQ-DEMO-ACTIVE', 'Yêu cầu đang có hợp đồng hiệu lực.'),
  ('rental_requests', 'delivery_address', 'REQ-DEMO-REJECTED', 'Nam Từ Liêm, Hà Nội'),
  ('rental_requests', 'note', 'REQ-DEMO-REJECTED', 'Yêu cầu có báo giá đã từ chối để demo lịch sử.'),
  ('quotations', 'special_terms', 'QT-DEMO-PENDING', 'Báo giá chờ Manager duyệt.'),
  ('quotations', 'special_terms', 'QT-DEMO-CONVERTED', 'Báo giá đã chuyển thành đơn thuê.'),
  ('quotations', 'special_terms', 'QT-DEMO-ACTIVE', 'Báo giá của hợp đồng đang hiệu lực.'),
  ('quotations', 'special_terms', 'QT-DEMO-REJECTED', 'Báo giá dùng để demo trạng thái từ chối.'),
  ('quotations', 'rejection_reason', 'QT-DEMO-REJECTED', 'Ngân sách khách hàng chưa phù hợp.'),
  ('rental_contracts', 'terms', 'CTR-DEMO-PENDING', 'Hợp đồng mẫu chờ Manager duyệt trong demo.'),
  ('rental_contracts', 'terms', 'CTR-DEMO-ACTIVE', 'Hợp đồng mẫu đang hiệu lực trong demo.'),
  ('contract_appendices', 'terms', 'APP-DEMO-EXT-001', 'Phụ lục gia hạn mẫu chờ duyệt.'),
  ('customers', 'display_name', 'DEMO-CUS-IND', 'Nguyễn Minh Anh'),
  ('customers', 'full_name', 'DEMO-CUS-IND', 'Nguyễn Minh Anh'),
  ('customers', 'address', 'DEMO-CUS-IND', 'Ba Đình, Hà Nội'),
  ('customers', 'note', 'DEMO-CUS-IND', 'Khách hàng cá nhân dùng cho demo.'),
  ('customers', 'display_name', 'DEMO-CUS-BIZ', 'Công ty Sự kiện Sao Việt'),
  ('customers', 'address', 'DEMO-CUS-BIZ', 'Nam Từ Liêm, Hà Nội'),
  ('customers', 'note', 'DEMO-CUS-BIZ', 'Khách doanh nghiệp dùng cho báo giá demo.'),
  ('branches', 'branch_name', 'DEMO-HN', 'Chi nhánh Hà Nội'),
  ('branches', 'address', 'DEMO-HN', 'Cầu Giấy, Hà Nội'),
  ('branches', 'branch_name', 'DEMO-HCM', 'Chi nhánh Hồ Chí Minh'),
  ('branches', 'address', 'DEMO-HCM', 'Quận 7, Hồ Chí Minh'),
  ('organizations', 'address', 'DEMO-RENTAI', 'Hà Nội'),
  ('customer_groups', 'group_name', 'DEMO-VIP', 'Khách hàng ưu tiên'),
  ('customer_groups', 'description', 'DEMO-VIP', 'Nhóm khách hàng doanh nghiệp cho demo.'),
  ('discount_codes', 'name', 'DEMO10', 'Giảm giá demo 10%');
UPDATE demo_text_expected
SET broken_text = CONVERT(CONVERT(CAST(expected_text AS BINARY) USING latin1) USING utf8mb4);

-- Snapshot candidates before updating; user-edited and already-correct values never match.
CREATE TEMPORARY TABLE demo_text_candidates (
  table_name VARCHAR(64), field_name VARCHAR(64), record_code VARCHAR(64),
  old_text TEXT, new_text TEXT
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

START TRANSACTION;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.delivery_address, s.expected_text
FROM rental_db.rental_requests t JOIN demo_text_expected s
  ON s.table_name = 'rental_requests' AND s.field_name = 'delivery_address' AND s.record_code = t.request_code
WHERE t.organization_id = @encoding_org AND BINARY t.delivery_address = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE rental_db.rental_requests t JOIN demo_text_expected s
  ON s.table_name = 'rental_requests' AND s.field_name = 'delivery_address' AND s.record_code = t.request_code
SET t.delivery_address = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.delivery_address = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.note, s.expected_text
FROM rental_db.rental_requests t JOIN demo_text_expected s
  ON s.table_name = 'rental_requests' AND s.field_name = 'note' AND s.record_code = t.request_code
WHERE t.organization_id = @encoding_org AND BINARY t.note = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE rental_db.rental_requests t JOIN demo_text_expected s
  ON s.table_name = 'rental_requests' AND s.field_name = 'note' AND s.record_code = t.request_code
SET t.note = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.note = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.special_terms, s.expected_text
FROM rental_db.quotations t JOIN demo_text_expected s
  ON s.table_name = 'quotations' AND s.field_name = 'special_terms' AND s.record_code = t.quotation_code
WHERE t.organization_id = @encoding_org AND BINARY t.special_terms = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE rental_db.quotations t JOIN demo_text_expected s
  ON s.table_name = 'quotations' AND s.field_name = 'special_terms' AND s.record_code = t.quotation_code
SET t.special_terms = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.special_terms = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.rejection_reason, s.expected_text
FROM rental_db.quotations t JOIN demo_text_expected s
  ON s.table_name = 'quotations' AND s.field_name = 'rejection_reason' AND s.record_code = t.quotation_code
WHERE t.organization_id = @encoding_org AND BINARY t.rejection_reason = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE rental_db.quotations t JOIN demo_text_expected s
  ON s.table_name = 'quotations' AND s.field_name = 'rejection_reason' AND s.record_code = t.quotation_code
SET t.rejection_reason = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.rejection_reason = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.terms, s.expected_text
FROM rental_db.rental_contracts t JOIN demo_text_expected s
  ON s.table_name = 'rental_contracts' AND s.field_name = 'terms' AND s.record_code = t.contract_code
WHERE t.organization_id = @encoding_org AND BINARY t.terms = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE rental_db.rental_contracts t JOIN demo_text_expected s
  ON s.table_name = 'rental_contracts' AND s.field_name = 'terms' AND s.record_code = t.contract_code
SET t.terms = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.terms = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.terms, s.expected_text
FROM rental_db.contract_appendices t JOIN demo_text_expected s
  ON s.table_name = 'contract_appendices' AND s.field_name = 'terms' AND s.record_code = t.appendix_code
WHERE t.organization_id = @encoding_org AND BINARY t.terms = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE rental_db.contract_appendices t JOIN demo_text_expected s
  ON s.table_name = 'contract_appendices' AND s.field_name = 'terms' AND s.record_code = t.appendix_code
SET t.terms = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.terms = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.display_name, s.expected_text
FROM organization_customer_db.customers t JOIN demo_text_expected s
  ON s.table_name = 'customers' AND s.field_name = 'display_name' AND s.record_code = t.customer_code
WHERE t.organization_id = @encoding_org AND BINARY t.display_name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.customers t JOIN demo_text_expected s
  ON s.table_name = 'customers' AND s.field_name = 'display_name' AND s.record_code = t.customer_code
SET t.display_name = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.display_name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.full_name, s.expected_text
FROM organization_customer_db.customers t JOIN demo_text_expected s
  ON s.table_name = 'customers' AND s.field_name = 'full_name' AND s.record_code = t.customer_code
WHERE t.organization_id = @encoding_org AND BINARY t.full_name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.customers t JOIN demo_text_expected s
  ON s.table_name = 'customers' AND s.field_name = 'full_name' AND s.record_code = t.customer_code
SET t.full_name = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.full_name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.address, s.expected_text
FROM organization_customer_db.customers t JOIN demo_text_expected s
  ON s.table_name = 'customers' AND s.field_name = 'address' AND s.record_code = t.customer_code
WHERE t.organization_id = @encoding_org AND BINARY t.address = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.customers t JOIN demo_text_expected s
  ON s.table_name = 'customers' AND s.field_name = 'address' AND s.record_code = t.customer_code
SET t.address = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.address = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.note, s.expected_text
FROM organization_customer_db.customers t JOIN demo_text_expected s
  ON s.table_name = 'customers' AND s.field_name = 'note' AND s.record_code = t.customer_code
WHERE t.organization_id = @encoding_org AND BINARY t.note = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.customers t JOIN demo_text_expected s
  ON s.table_name = 'customers' AND s.field_name = 'note' AND s.record_code = t.customer_code
SET t.note = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.note = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.branch_name, s.expected_text
FROM organization_customer_db.branches t JOIN demo_text_expected s
  ON s.table_name = 'branches' AND s.field_name = 'branch_name' AND s.record_code = t.branch_code
WHERE t.organization_id = @encoding_org AND BINARY t.branch_name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.branches t JOIN demo_text_expected s
  ON s.table_name = 'branches' AND s.field_name = 'branch_name' AND s.record_code = t.branch_code
SET t.branch_name = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.branch_name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.address, s.expected_text
FROM organization_customer_db.branches t JOIN demo_text_expected s
  ON s.table_name = 'branches' AND s.field_name = 'address' AND s.record_code = t.branch_code
WHERE t.organization_id = @encoding_org AND BINARY t.address = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.branches t JOIN demo_text_expected s
  ON s.table_name = 'branches' AND s.field_name = 'address' AND s.record_code = t.branch_code
SET t.address = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.address = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.address, s.expected_text
FROM organization_customer_db.organizations t JOIN demo_text_expected s
  ON s.table_name = 'organizations' AND s.field_name = 'address' AND s.record_code = t.organization_code
WHERE t.id = @encoding_org AND BINARY t.address = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.organizations t JOIN demo_text_expected s
  ON s.table_name = 'organizations' AND s.field_name = 'address' AND s.record_code = t.organization_code
SET t.address = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.id = @encoding_org
  AND BINARY t.address = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.group_name, s.expected_text
FROM organization_customer_db.customer_groups t JOIN demo_text_expected s
  ON s.table_name = 'customer_groups' AND s.field_name = 'group_name' AND s.record_code = t.group_code
WHERE t.organization_id = @encoding_org AND BINARY t.group_name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.customer_groups t JOIN demo_text_expected s
  ON s.table_name = 'customer_groups' AND s.field_name = 'group_name' AND s.record_code = t.group_code
SET t.group_name = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.group_name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.description, s.expected_text
FROM organization_customer_db.customer_groups t JOIN demo_text_expected s
  ON s.table_name = 'customer_groups' AND s.field_name = 'description' AND s.record_code = t.group_code
WHERE t.organization_id = @encoding_org AND BINARY t.description = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE organization_customer_db.customer_groups t JOIN demo_text_expected s
  ON s.table_name = 'customer_groups' AND s.field_name = 'description' AND s.record_code = t.group_code
SET t.description = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.description = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

INSERT INTO demo_text_candidates
SELECT s.table_name, s.field_name, s.record_code, t.name, s.expected_text
FROM rental_db.discount_codes t JOIN demo_text_expected s
  ON s.table_name = 'discount_codes' AND s.field_name = 'name' AND s.record_code = t.code
WHERE t.organization_id = @encoding_org AND BINARY t.name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;
UPDATE rental_db.discount_codes t JOIN demo_text_expected s
  ON s.table_name = 'discount_codes' AND s.field_name = 'name' AND s.record_code = t.code
SET t.name = s.expected_text
WHERE @demo_encoding_apply = 1 AND t.organization_id = @encoding_org
  AND BINARY t.name = BINARY s.broken_text
  AND BINARY s.expected_text <> BINARY s.broken_text;

COMMIT;
SELECT IF(@demo_encoding_apply = 1, 'APPLIED', 'PREVIEW_ONLY') AS mode,
  table_name, field_name, record_code, old_text, new_text
FROM demo_text_candidates ORDER BY table_name, record_code, field_name;
SELECT COUNT(*) AS matched_demo_fields,
  IF(@demo_encoding_apply = 1, 'Known broken demo fields repaired.', 'Preview only; no persistent data changed.') AS result
FROM demo_text_candidates;
