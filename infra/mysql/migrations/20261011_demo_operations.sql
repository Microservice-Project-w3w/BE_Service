-- Additive demo tables only. Existing database names and business rows are preserved.
CREATE TABLE IF NOT EXISTS rental_db.demo_rental_accounts (
  rental_order_id BIGINT NOT NULL PRIMARY KEY,
  invoice_issued BIT NOT NULL DEFAULT 0, invoice_cancelled BIT NOT NULL DEFAULT 0,
  invoice_amount DECIMAL(15,2) DEFAULT 0, required_deposit DECIMAL(15,2) DEFAULT 0,
  due_date DATE NULL, payment_method VARCHAR(30) DEFAULT 'COD'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS rental_db.demo_rental_events (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, rental_order_id BIGINT NOT NULL,
  request_key VARCHAR(80) NOT NULL, kind VARCHAR(40) NOT NULL, amount DECIMAL(15,2) DEFAULT 0,
  payment_method VARCHAR(30) NULL, note VARCHAR(1000) NULL, details LONGTEXT NULL,
  actor_user_id VARCHAR(100) NOT NULL, occurred_at DATETIME(6) NOT NULL,
  UNIQUE KEY uk_demo_rental_event (rental_order_id,request_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS rental_db.demo_sync_tasks (
  id VARCHAR(80) NOT NULL PRIMARY KEY, rental_order_id BIGINT NOT NULL,
  organization_id BIGINT NOT NULL, branch_id BIGINT NOT NULL,
  kind VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  payload LONGTEXT NOT NULL, last_error VARCHAR(1000) NULL, attempts INT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NULL,
  KEY ix_demo_sync_order (rental_order_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS inventory_db.demo_inventory_operations (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, request_key VARCHAR(80) NOT NULL,
  organization_id BIGINT NOT NULL, branch_id BIGINT NOT NULL, rental_order_id BIGINT NOT NULL,
  reservation_id BIGINT NOT NULL, kind VARCHAR(30) NOT NULL, details LONGTEXT NOT NULL,
  actor_user_id VARCHAR(100) NOT NULL, occurred_at DATETIME(6) NOT NULL,
  UNIQUE KEY uk_demo_inventory_request (request_key), UNIQUE KEY uk_demo_inventory_order_kind (rental_order_id,kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS inventory_db.demo_maintenance_tickets (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, organization_id BIGINT NOT NULL, branch_id BIGINT NOT NULL,
  equipment_id BIGINT NOT NULL, scheduled_at DATETIME(6) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
  reason VARCHAR(1000) NOT NULL, result VARCHAR(1000) NULL, cost DECIMAL(15,2) DEFAULT 0,
  created_by VARCHAR(100) NOT NULL, completed_by VARCHAR(100) NULL, started_at DATETIME(6) NULL, completed_at DATETIME(6) NULL,
  KEY ix_demo_maintenance_scope (organization_id,branch_id,scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
