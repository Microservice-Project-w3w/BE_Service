package com.equipmentrental.identity.config;

import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.repository.RoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class RoleDataInitializer {

    @Bean
    @Order(1)
    ApplicationRunner initialRoleData(RoleRepository roleRepository) {
        return arguments -> seedInitialRoles(roleRepository);
    }

    void seedInitialRoles(RoleRepository roleRepository) {
        for (InitialRole initialRole : InitialRole.values()) {
            if (roleRepository.findByCode(initialRole.code()).isPresent()) {
                continue;
            }
            roleRepository.save(new Role(
                    initialRole.code(),
                    initialRole.displayName(),
                    initialRole.description(),
                    true,
                    true
            ));
        }
    }

    private enum InitialRole {
        SUPER_ADMIN("SUPER_ADMIN", "Super Administrator", "Quản trị toàn hệ thống"),
        ORG_ADMIN("ORG_ADMIN", "Organization Administrator", "Quản trị doanh nghiệp"),
        BRANCH_MANAGER("BRANCH_MANAGER", "Branch Manager", "Quản lý chi nhánh"),
        SALES_STAFF("SALES_STAFF", "Sales Staff", "Nhân viên kinh doanh"),
        WAREHOUSE_STAFF("WAREHOUSE_STAFF", "Warehouse Staff", "Nhân viên kho"),
        DELIVERY_STAFF("DELIVERY_STAFF", "Delivery Staff", "Nhân viên giao nhận"),
        ACCOUNTANT("ACCOUNTANT", "Accountant", "Kế toán"),
        TECHNICIAN("TECHNICIAN", "Technician", "Kỹ thuật viên"),
        CUSTOMER("CUSTOMER", "Customer", "Khách hàng");

        private final String code;
        private final String name;
        private final String description;

        InitialRole(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        String code() {
            return code;
        }

        String displayName() {
            return name;
        }

        String description() {
            return description;
        }
    }
}
