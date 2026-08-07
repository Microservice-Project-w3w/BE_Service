package com.equipmentrental.identity.config;

import com.equipmentrental.common.security.DataScope;
import com.equipmentrental.identity.entity.Permission;
import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.entity.RolePermission;
import com.equipmentrental.identity.repository.PermissionRepository;
import com.equipmentrental.identity.repository.RoleRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import com.equipmentrental.identity.repository.RolePermissionRepository;
@Configuration
public class PermissionDataInitializer {

    private static final String PERMISSION_SEED = "security/permission-seed.csv";
    private static final String ROLE_PERMISSION_SEED = "security/role-permission-seed.csv";

    @Bean
    @Order(2)
    ApplicationRunner initialPermissionData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository
    ) {
        return arguments -> {
            Map<String, Permission> permissions = seedPermissions(permissionRepository);
            seedRolePermissions(roleRepository, permissions,  rolePermissionRepository);
        };
    }

    private Map<String, Permission> seedPermissions(PermissionRepository permissionRepository) throws IOException {
        Map<String, Permission> result = new LinkedHashMap<>();
        forEachDataLine(PERMISSION_SEED, line -> {
            String[] columns = line.split(",", 4);
            if (columns.length != 4) {
                throw new IllegalStateException("Permission seed không hợp lệ: " + line);
            }
            Permission permission = permissionRepository.findByCode(columns[0])
                    .orElseGet(() -> permissionRepository.save(new Permission(
                            columns[0], columns[1], columns[2], columns[3]
                    )));
            result.put(columns[0], permission);
        });
        return result;
    }

    private void seedRolePermissions(RoleRepository roleRepository, Map<String, Permission> permissions, RolePermissionRepository rolePermissionRepository) throws IOException {
        Map<String, Set<RolePermission>> assignments = new LinkedHashMap<>();
        forEachDataLine(ROLE_PERMISSION_SEED, line -> {
            String[] columns = line.split(",", 3);
            if (columns.length != 3) {
                throw new IllegalStateException("Role permission seed không hợp lệ: " + line);
            }
            Role role = roleRepository.findByCode(columns[0])
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy role seed: " + columns[0]));
            Permission permission = permissions.get(columns[1]);
            if (permission == null) {
                throw new IllegalStateException("Không tìm thấy permission seed: " + columns[1]);
            }
            assignments.computeIfAbsent(role.getCode(), ignored -> new LinkedHashSet<>())
                    .add(new RolePermission(role, permission, DataScope.valueOf(columns[2])));
        });
        assignments.forEach((roleCode, rolePermissions) -> {

            Role role = roleRepository
                    .findByCode(roleCode)
                    .orElseThrow();

            rolePermissions.forEach(rolePermission -> {

                boolean exists =
                        rolePermissionRepository
                                .existsByRoleIdAndPermissionId(
                                        role.getId(),
                                        rolePermission.getPermission().getId()
                                );

                if (!exists) {
                    rolePermissionRepository.save(rolePermission);
                }
            });
        });
    }

    private void forEachDataLine(String resourcePath, CsvLineConsumer consumer) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    consumer.accept(line);
                }
            }
        }
    }

    @FunctionalInterface
    private interface CsvLineConsumer {
        void accept(String line);
    }
}
