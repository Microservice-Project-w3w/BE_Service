SET NAMES utf8mb4;
USE organization_customer_db;

-- owner_user_id remains the responsible Sales user; this link is separate.
CREATE TABLE IF NOT EXISTS customer_portal_accounts (
    user_id BIGINT PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_customer_portal_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);
