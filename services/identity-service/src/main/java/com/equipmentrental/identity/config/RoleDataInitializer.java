package com.equipmentrental.identity.config;

import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.repository.RoleRepository;
import com.equipmentrental.identity.repository.UserRepository;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class RoleDataInitializer {

    @Bean
    @Order(1)
    ApplicationRunner initialRoleData(RoleRepository roleRepository, UserRepository userRepository) {
        return arguments -> {
            seedInitialRoles(roleRepository);
            migrateLegacyRoles(roleRepository, userRepository);
        };
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


    private void migrateLegacyRoles(RoleRepository roleRepository, UserRepository userRepository) {
        for (Map.Entry<String, String> migration : LEGACY_ROLE_MAPPINGS.entrySet()) {
            roleRepository.findByCode(migration.getKey()).ifPresent(legacyRole -> {
                Role replacementRole = roleRepository.findByCode(migration.getValue()).orElseThrow();
                userRepository.reassignRole(legacyRole.getCode(), replacementRole);
                roleRepository.delete(legacyRole);
            });
        }
    }

    private static final Map<String, String> LEGACY_ROLE_MAPPINGS = Map.of(
            "SUPER_ADMIN", "ADMIN",
            "ORG_ADMIN", "ADMIN",
            "BRANCH_MANAGER", "MANAGER",
            "WAREHOUSE_STAFF", "OPERATIONS_STAFF",
            "DELIVERY_STAFF", "OPERATIONS_STAFF",
            "TECHNICIAN", "OPERATIONS_STAFF"
    );

    private enum InitialRole {
        ADMIN("ADMIN", "Administrator", "Quản trị doanh nghiệp và hệ thống demo"),
        MANAGER("MANAGER", "Manager", "Quản lý chi nhánh"),
        SALES_STAFF("SALES_STAFF", "Sales Staff", "Nhân viên kinh doanh"),
        OPERATIONS_STAFF("OPERATIONS_STAFF", "Operations Staff", "Nhân viên vận hành, kho, giao nhận và kỹ thuật"),
        ACCOUNTANT("ACCOUNTANT", "Accountant", "Kế toán"),
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
