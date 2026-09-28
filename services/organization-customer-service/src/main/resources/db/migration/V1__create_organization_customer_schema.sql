CREATE TABLE organizations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_code VARCHAR(50) NOT NULL,
    organization_name VARCHAR(255) NOT NULL,
    tax_code VARCHAR(50), email VARCHAR(255), phone VARCHAR(30), address VARCHAR(500),
    status VARCHAR(30) NOT NULL, created_by BIGINT, updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (id), UNIQUE (organization_code), UNIQUE (tax_code)
);

CREATE TABLE branches (
    id BIGINT NOT NULL AUTO_INCREMENT, organization_id BIGINT NOT NULL,
    branch_code VARCHAR(255) NOT NULL, branch_name VARCHAR(255) NOT NULL,
    email VARCHAR(255), phone VARCHAR(255), address VARCHAR(255), status VARCHAR(30) NOT NULL,
    created_by BIGINT, updated_by BIGINT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, deleted_at TIMESTAMP NULL,
    PRIMARY KEY (id), UNIQUE (organization_id, branch_code),
    CONSTRAINT fk_branches_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT, organization_id BIGINT NOT NULL, user_id BIGINT,
    employee_code VARCHAR(255) NOT NULL, full_name VARCHAR(255) NOT NULL, email VARCHAR(255),
    phone VARCHAR(255), job_title VARCHAR(255), status VARCHAR(30), hire_date DATE,
    created_by BIGINT, updated_by BIGINT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, deleted_at TIMESTAMP NULL,
    PRIMARY KEY (id), UNIQUE (organization_id, employee_code), UNIQUE (organization_id, user_id),
    CONSTRAINT fk_employees_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE employee_branch_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT, organization_id BIGINT NOT NULL, employee_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL, is_primary BOOLEAN NOT NULL, assigned_from DATE, assigned_to DATE,
    status VARCHAR(30), created_by BIGINT, updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE (organization_id, employee_id, branch_id),
    CONSTRAINT fk_assignments_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_assignments_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_assignments_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT, organization_id BIGINT NOT NULL, branch_id BIGINT, owner_user_id BIGINT,
    customer_code VARCHAR(255) NOT NULL, customer_type VARCHAR(30) NOT NULL, display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255), phone VARCHAR(255), address VARCHAR(255), full_name VARCHAR(255), date_of_birth DATE,
    identity_number VARCHAR(255), company_name VARCHAR(255), tax_code VARCHAR(255), representative_name VARCHAR(255),
    representative_phone VARCHAR(255), representative_email VARCHAR(255), status VARCHAR(30), note VARCHAR(255),
    created_by BIGINT, updated_by BIGINT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, deleted_at TIMESTAMP NULL,
    PRIMARY KEY (id), UNIQUE (organization_id, customer_code),
    CONSTRAINT fk_customers_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_customers_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE TABLE customer_groups (
    id BIGINT NOT NULL AUTO_INCREMENT, organization_id BIGINT NOT NULL, group_code VARCHAR(255) NOT NULL,
    group_name VARCHAR(255) NOT NULL, description VARCHAR(255), status VARCHAR(30),
    created_by BIGINT, updated_by BIGINT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, deleted_at TIMESTAMP NULL,
    PRIMARY KEY (id), UNIQUE (organization_id, group_code),
    CONSTRAINT fk_groups_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE customer_group_members (
    id BIGINT NOT NULL AUTO_INCREMENT, organization_id BIGINT NOT NULL, customer_group_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL, created_by BIGINT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE (organization_id, customer_group_id, customer_id),
    CONSTRAINT fk_members_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_members_group FOREIGN KEY (customer_group_id) REFERENCES customer_groups(id),
    CONSTRAINT fk_members_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE restricted_customers (
    id BIGINT NOT NULL AUTO_INCREMENT, organization_id BIGINT NOT NULL, customer_id BIGINT NOT NULL,
    restriction_type VARCHAR(40) NOT NULL, reason VARCHAR(1000) NOT NULL, status VARCHAR(30) NOT NULL,
    restricted_from TIMESTAMP NOT NULL, restricted_until TIMESTAMP NULL, restricted_by_user_id BIGINT NOT NULL,
    removed_at TIMESTAMP NULL, removed_by_user_id BIGINT, removed_reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_restrictions_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_restrictions_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_branches_organization ON branches(organization_id);
CREATE INDEX idx_employees_organization ON employees(organization_id);
CREATE INDEX idx_customers_organization ON customers(organization_id);
CREATE INDEX idx_customers_search ON customers(organization_id, customer_type, owner_user_id, branch_id);
CREATE INDEX idx_groups_organization ON customer_groups(organization_id);
CREATE INDEX idx_restrictions_customer ON restricted_customers(organization_id, customer_id, status);
