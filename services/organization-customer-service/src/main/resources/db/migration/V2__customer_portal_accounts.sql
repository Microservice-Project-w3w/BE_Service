CREATE TABLE IF NOT EXISTS customer_portal_accounts (
    user_id BIGINT PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_customer_portal_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);
