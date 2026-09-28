-- Chạy một lần trên database local đã được khởi tạo trước ngày 2026-08-04.
USE rental_db;

ALTER TABLE rental_orders
    ADD COLUMN inventory_reservation_id VARCHAR(100) NULL AFTER reserved_until;

CREATE TABLE IF NOT EXISTS rental_contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    contract_code VARCHAR(50) NOT NULL,
    rental_order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    terms VARCHAR(2000) NULL,
    approved_at DATETIME NULL,
    signed_at DATETIME NULL,
    liquidated_at DATETIME NULL,
    cancel_reason VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_rental_contract_order UNIQUE (rental_order_id),
    CONSTRAINT uk_rental_contract_code UNIQUE (contract_code),
    INDEX idx_rental_contract_scope (organization_id, branch_id)
);

CREATE TABLE IF NOT EXISTS contract_appendices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    appendix_code VARCHAR(50) NOT NULL,
    appendix_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    new_end_at DATETIME NULL,
    terms VARCHAR(2000) NOT NULL,
    approved_at DATETIME NULL,
    signed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_contract_appendix_code UNIQUE (appendix_code),
    CONSTRAINT fk_contract_appendix_contract FOREIGN KEY (contract_id) REFERENCES rental_contracts(id),
    INDEX idx_contract_appendix_scope (organization_id, branch_id)
);
