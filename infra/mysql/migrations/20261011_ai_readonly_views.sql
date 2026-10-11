-- Optional AI read adapter. Keeps the existing database names and business tables.
-- Run as schema owner, never as the AI account. Grant SELECT on these views ONLY.
CREATE OR REPLACE SQL SECURITY DEFINER VIEW inventory_db.ai_equipment AS
SELECT id, organization_id AS organizationId, branch_id AS branchId,
       warehouse_id AS warehouseId, model_id AS modelId, asset_code AS assetCode,
       serial_number AS serialNumber, status, condition_status AS conditionStatus, note
FROM inventory_db.equipment;

CREATE OR REPLACE SQL SECURITY DEFINER VIEW inventory_db.ai_warehouses AS
SELECT id, organization_id AS organizationId, branch_id AS branchId, name, active
FROM inventory_db.warehouses;

CREATE OR REPLACE SQL SECURITY DEFINER VIEW inventory_db.ai_available_equipment AS
SELECT e.id, e.organization_id AS organizationId, e.branch_id AS branchId,
       m.equipment_type_id AS equipmentTypeId
FROM inventory_db.equipment e
JOIN inventory_db.equipment_models m ON m.id = e.model_id AND m.organization_id = e.organization_id AND m.active = TRUE
JOIN inventory_db.equipment_types t ON t.id = m.equipment_type_id AND t.organization_id = e.organization_id AND t.active = TRUE
JOIN inventory_db.warehouses w ON w.id = e.warehouse_id AND w.organization_id = e.organization_id AND w.branch_id = e.branch_id AND w.active = TRUE
WHERE e.status = 'AVAILABLE';

CREATE OR REPLACE SQL SECURITY DEFINER VIEW inventory_db.ai_reservation_blocks AS
SELECT r.organization_id AS organizationId, r.branch_id AS branchId,
       i.equipment_id AS equipmentId, i.start_at AS startAt, i.end_at AS endAt,
       r.status, r.expires_at AS expiresAt
FROM inventory_db.equipment_reservation_items i
JOIN inventory_db.equipment_reservations r ON r.id = i.reservation_id
WHERE r.status IN ('HELD', 'CONFIRMED');

CREATE OR REPLACE SQL SECURITY DEFINER VIEW rental_db.ai_quotations AS
SELECT id, organization_id AS organizationId, branch_id AS branchId,
       quotation_code AS quotationCode, rental_request_id AS rentalRequestId,
       customer_id AS customerId, valid_until AS validUntil, total_amount AS totalAmount,
       CASE WHEN valid_until <= CURRENT_TIMESTAMP AND status IN ('DRAFT','SENT','PENDING_APPROVAL','APPROVED','ACCEPTED')
            THEN 'EXPIRED' ELSE status END AS status
FROM rental_db.quotations;

CREATE OR REPLACE SQL SECURITY DEFINER VIEW rental_db.ai_rental_orders AS
SELECT id, organization_id AS organizationId, branch_id AS branchId,
       order_code AS orderCode, quotation_id AS quotationId, customer_id AS customerId,
       start_at AS startAt, end_at AS endAt,
       CASE WHEN status = 'RESERVED' AND reserved_until <= CURRENT_TIMESTAMP
            THEN 'EXPIRED' ELSE status END AS status
FROM rental_db.rental_orders;

CREATE OR REPLACE SQL SECURITY DEFINER VIEW rental_db.ai_rental_requests AS
SELECT id, organization_id AS organizationId, branch_id AS branchId, delivery_address AS deliveryAddress
FROM rental_db.rental_requests;

CREATE OR REPLACE SQL SECURITY DEFINER VIEW rental_db.ai_rental_contracts AS
SELECT id, organization_id AS organizationId, branch_id AS branchId,
       contract_code AS contractCode, rental_order_id AS rentalOrderId, customer_id AS customerId,
       start_at AS startAt, end_at AS endAt, total_amount AS totalAmount, status
FROM rental_db.rental_contracts;

-- Deliberately excludes identity_number, date_of_birth and unrelated customer notes.
CREATE OR REPLACE SQL SECURITY DEFINER VIEW organization_customer_db.ai_customers AS
SELECT id, organization_id AS organizationId, branch_id AS branchId,
       customer_code AS customerCode, customer_type AS customerType, display_name AS displayName,
       email, phone, address, full_name AS fullName, company_name AS companyName, tax_code AS taxCode, status
FROM organization_customer_db.customers
WHERE deleted_at IS NULL AND status <> 'DELETED';
