package com.equipmentrental.inventory.security;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import com.equipmentrental.inventory.entity.Equipment;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Service-layer scope: check persisted ownership before mutation; filter unscoped lists. */
@Component
public class InventoryDataScopeGuard {
    private static final Map<String, String> TABLES = Map.ofEntries(
            Map.entry("EquipmentService", "equipment"), Map.entry("EquipmentQrServiceImpl", "equipment"),
            Map.entry("InternalEquipmentQueryService", "equipment"),
            Map.entry("InternalEquipmentCheckoutService", "equipment"), Map.entry("InternalEquipmentCheckinService", "equipment"),
            Map.entry("EquipmentImageService", "equipment_images"), Map.entry("EquipmentAccessoryService", "equipment_accessories"),
            Map.entry("EquipmentCategoryService", "equipment_categories"), Map.entry("EquipmentTypeService", "equipment_types"),
            Map.entry("EquipmentModelService", "equipment_models"), Map.entry("BrandService", "brands"),
            Map.entry("WarehouseService", "warehouses"), Map.entry("StockInService", "stock_in_receipts"),
            Map.entry("StockOutService", "stock_out_receipts"), Map.entry("StockTransferService", "stock_transfers"),
            Map.entry("StockAuditService", "stock_audits"), Map.entry("InternalReservationService", "equipment_reservations"),
            Map.entry("EquipmentReservationQueryService", "equipment_reservations"));
    private static final Map<String, String> REFERENCES = Map.ofEntries(
            Map.entry("equipmentId", "equipment"), Map.entry("warehouseId", "warehouses"),
            Map.entry("actualWarehouseId", "warehouses"), Map.entry("fromWarehouseId", "warehouses"),
            Map.entry("toWarehouseId", "warehouses"), Map.entry("sourceWarehouseId", "warehouses"),
            Map.entry("destinationWarehouseId", "warehouses"), Map.entry("modelId", "equipment_models"),
            Map.entry("categoryId", "equipment_categories"), Map.entry("brandId", "brands"),
            Map.entry("equipmentTypeId", "equipment_types"));
    private final CurrentUserProvider users;
    private final DataScopeAuthorizer authorizer;
    private final JdbcTemplate jdbc;

    public InventoryDataScopeGuard(CurrentUserProvider users, DataScopeAuthorizer authorizer, JdbcTemplate jdbc) {
        this.users = users; this.authorizer = authorizer; this.jdbc = jdbc;
    }

    public void requireScope(Long organizationId, Long branchId) {
        CurrentUser user = users.getCurrentUser();
        boolean allowed = branchId == null ? authorizer.canAccessOrganization(user, organizationId)
                : authorizer.canAccessBranch(user, organizationId, branchId);
        if (!allowed) throw new AccessDeniedException("Không có quyền truy cập organization/chi nhánh này");
    }

    public void checkArguments(String service, String[] names, Object[] args) {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            fields.put(names[i], args[i]);
            fields.putAll(fields(args[i]));
        }
        Long organizationId = number(fields.get("organizationId"));
        Long branchId = number(fields.get("branchId"));
        if (organizationId != null) requireScope(organizationId, branchId);
        if (branchId != null && organizationId == null) requireScope(users.getCurrentUser().organizationId(), branchId);
        Long id = number(fields.get("id"));
        String table = TABLES.get(service);
        if (id != null && table != null) checkEntity(table, id, organizationId);
        for (Map.Entry<String, Object> field : fields.entrySet()) checkReference(field.getKey(), field.getValue(), organizationId);
        for (Object arg : args) checkNested(arg, organizationId);
        // Identifier searches must not rely on the caller-supplied organization alone.
        if (table != null && table.equals("equipment")) {
            for (String key : List.of("serial", "serialNumber", "imei", "qrCode")) {
                if (fields.get(key) instanceof String value && !value.isBlank()) {
                    String column = key.equals("qrCode") ? "qr_code" : key.equals("imei") ? "imei" : "serial_number";
                    List<Map<String, Object>> rows = organizationId == null
                            ? jdbc.queryForList("SELECT * FROM equipment WHERE " + column + " = ?", value)
                            : jdbc.queryForList("SELECT * FROM equipment WHERE " + column + " = ? AND organization_id = ?", value, organizationId);
                    for (Map<String, Object> row : rows) requireRow(row, organizationId);
                }
            }
        }
    }

    private void checkNested(Object value, Long organizationId) {
        if (value instanceof Iterable<?> items) { for (Object item : items) checkNested(item, organizationId); return; }
        Map<String, Object> fields = fields(value);
        Long ownOrganization = number(fields.get("organizationId"));
        if (ownOrganization != null) requireScope(ownOrganization, number(fields.get("branchId")));
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            checkReference(field.getKey(), field.getValue(), organizationId);
            if (field.getValue() instanceof Iterable<?>) checkNested(field.getValue(), organizationId);
        }
    }

    private void checkReference(String name, Object value, Long organizationId) {
        Long id = number(value);
        String table = REFERENCES.get(name);
        if (id != null && table != null) checkEntity(table, id, organizationId);
    }

    private void checkEntity(String table, Long id, Long expectedOrganization) {
        // Table names come exclusively from the two constant allowlists above.
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM " + table + " WHERE id = ?", id);
        for (Map<String, Object> row : rows) {
            if (!row.containsKey("organization_id") && row.get("equipment_id") != null) {
                checkEntity("equipment", number(row.get("equipment_id")), expectedOrganization);
            } else requireRow(row, expectedOrganization);
        }
        // Let the existing service return its normal 404 for a missing ID.
    }

    private void requireRow(Map<String, Object> row, Long expectedOrganization) {
        Long organization = number(row.get("organization_id"));
        if (expectedOrganization != null && !expectedOrganization.equals(organization))
            throw new AccessDeniedException("Dữ liệu không thuộc organization được yêu cầu");
        requireScope(organization, number(row.get("branch_id")));
        for (String key : List.of("from_branch_id", "to_branch_id")) {
            if (row.get(key) != null) requireScope(organization, number(row.get(key)));
        }
    }

    public boolean canRead(Object value) {
        Map<String, Object> fields = fields(value);
        Long organization = number(fields.get("organizationId"));
        if (organization == null) return true; // Child DTOs are scoped through their checked parent.
        CurrentUser user = users.getCurrentUser();
        if (!authorizer.canAccessOrganization(user, organization)) return false;
        for (String key : List.of("branchId", "fromBranchId", "toBranchId")) {
            Long branch = number(fields.get(key));
            if (branch != null && !authorizer.canAccessBranch(user, organization, branch)) return false;
        }
        return true;
    }

    private static Map<String, Object> fields(Object value) {
        if (value instanceof Equipment equipment) return Map.of("organizationId", equipment.getOrganizationId(), "branchId", equipment.getBranchId());
        Map<String, Object> fields = new LinkedHashMap<>();
        if (value == null || !value.getClass().isRecord()) return fields;
        try {
            for (RecordComponent component : value.getClass().getRecordComponents())
                fields.put(component.getName(), component.getAccessor().invoke(value));
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException("Cannot inspect inventory scope", exception); }
        return fields;
    }

    private static Long number(Object value) { return value instanceof Number number ? number.longValue() : null; }
}
