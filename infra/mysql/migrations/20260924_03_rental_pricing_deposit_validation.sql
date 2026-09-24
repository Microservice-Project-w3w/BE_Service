-- Chạy một lần trên rental_db sau khi bảng rental_prices đã tồn tại.
USE rental_db;

ALTER TABLE rental_prices
    ADD CONSTRAINT chk_rental_price_deposit_percent_max
    CHECK (deposit_type <> 'PERCENT' OR deposit_value <= 100);
