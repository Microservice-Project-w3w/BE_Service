package com.equipmentrental.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class RolePermissionSeedTest {
    @Test
    void sixDemoRolesHaveExpectedCriticalPermissions() throws Exception {
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/security/role-permission-seed.csv"), StandardCharsets.UTF_8))) {
            lines = reader.lines().skip(1).toList();
        }

        assertThat(lines).contains(
                "MANAGER,rental.quotation.approve,BRANCH",
                "MANAGER,rental.contract.approve,BRANCH",
                "MANAGER,maintenance.ticket.read,BRANCH",
                "SALES_STAFF,inventory.availability.read,BRANCH",
                "OPERATIONS_STAFF,logistics.delivery.create,BRANCH",
                "OPERATIONS_STAFF,logistics.delivery.schedule,BRANCH",
                "CUSTOMER,rental.quotation.accept,OWN",
                "CUSTOMER,rental.order.read,OWN");
        assertThat(lines).doesNotContain(
                "MANAGER,customer.profile.create,BRANCH",
                "MANAGER,customer.profile.update,BRANCH",
                "CUSTOMER,rental.quotation.approve,OWN");
        assertThat(lines.stream().map(line -> line.substring(0, line.indexOf(','))).distinct())
                .containsExactlyInAnyOrder(
                        "ADMIN", "MANAGER", "SALES_STAFF", "OPERATIONS_STAFF", "ACCOUNTANT", "CUSTOMER");
    }
}
