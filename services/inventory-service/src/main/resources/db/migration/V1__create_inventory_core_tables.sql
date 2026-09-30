CREATE TABLE equipment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    equipment_code VARCHAR(100) NOT NULL,
    equipment_name VARCHAR(255) NOT NULL,
    branch_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    equipment_condition VARCHAR(30) NOT NULL DEFAULT 'GOOD',

    version BIGINT NOT NULL DEFAULT 0,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_equipment_code
        UNIQUE (equipment_code)
);

CREATE INDEX idx_equipment_branch_status
    ON equipment (branch_id, status);


CREATE TABLE equipment_reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,

    equipment_id BIGINT NOT NULL,
    rental_id BIGINT NOT NULL,

    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'HELD',

    expires_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_reservation_equipment
        FOREIGN KEY (equipment_id)
        REFERENCES equipment (id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_reservation_equipment
    ON equipment_reservations (equipment_id);

CREATE INDEX idx_reservation_rental
    ON equipment_reservations (rental_id);

CREATE INDEX idx_reservation_period
    ON equipment_reservations (start_at, end_at);

CREATE INDEX idx_reservation_equipment_status_period
    ON equipment_reservations (
        equipment_id,
        status,
        start_at,
        end_at
    );