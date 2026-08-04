-- Chỉ tạo database, chưa tạo bảng hoặc dữ liệu mẫu.
CREATE DATABASE IF NOT EXISTS identity_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS organization_customer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS rental_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rental_db;

CREATE TABLE IF NOT EXISTS rental_prices (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                             price_name VARCHAR(150) NOT NULL,

    equipment_type_id BIGINT NOT NULL,

    rental_unit ENUM(
                        'HOUR',
                        'DAY',
                        'WEEK',
                        'MONTH'
                    ) NOT NULL,

    rental_price DECIMAL(15,2) NOT NULL DEFAULT 0,

    deposit_amount DECIMAL(15,2) NOT NULL DEFAULT 0,

    late_fee DECIMAL(15,2) NOT NULL DEFAULT 0,

    valid_from DATETIME NOT NULL,

    valid_to DATETIME NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    description VARCHAR(500) NULL,

    created_at DATETIME NOT NULL
    DEFAULT CURRENT_TIMESTAMP,

    updated_at DATETIME NOT NULL
    DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_rental_price_equipment_unit
    UNIQUE (
               equipment_type_id,
               rental_unit
           ),

    CONSTRAINT chk_rental_price_non_negative
    CHECK (rental_price >= 0),

    CONSTRAINT chk_deposit_non_negative
    CHECK (deposit_amount >= 0),

    CONSTRAINT chk_late_fee_non_negative
    CHECK (late_fee >= 0),

    CONSTRAINT chk_valid_date
    CHECK (
              valid_to IS NULL
              OR valid_to >= valid_from
          )
    );
INSERT INTO rental_prices (
    price_name,
    equipment_type_id,
    rental_unit,
    rental_price,
    deposit_amount,
    late_fee,
    valid_from,
    valid_to,
    active,
    description
)
VALUES
    (
        'Giá thuê laptop theo giờ',
        1,
        'HOUR',
        50000,
        2000000,
        20000,
        '2026-08-04 00:00:00',
        '2026-12-31 23:59:59',
        TRUE,
        'Bảng giá laptop theo giờ'
    ),
    (
        'Giá thuê laptop theo ngày',
        1,
        'DAY',
        300000,
        2000000,
        50000,
        '2026-08-04 00:00:00',
        '2026-12-31 23:59:59',
        TRUE,
        'Bảng giá laptop theo ngày'
    ),
    (
        'Giá thuê laptop theo tuần',
        1,
        'WEEK',
        1700000,
        2000000,
        300000,
        '2026-08-04 00:00:00',
        '2026-12-31 23:59:59',
        TRUE,
        'Bảng giá laptop theo tuần'
    ),
    (
        'Giá thuê laptop theo tháng',
        1,
        'MONTH',
        6000000,
        2000000,
        500000,
        '2026-08-04 00:00:00',
        '2026-12-31 23:59:59',
        TRUE,
        'Bảng giá laptop theo tháng'
    );
CREATE DATABASE IF NOT EXISTS logistics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS billing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS maintenance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
