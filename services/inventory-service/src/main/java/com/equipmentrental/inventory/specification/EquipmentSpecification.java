package com.equipmentrental.inventory.specification;

import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.EquipmentModel;
import com.equipmentrental.inventory.entity.EquipmentType;
import com.equipmentrental.inventory.entity.Brand;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public final class EquipmentSpecification {

    private EquipmentSpecification() {
    }

    public static Specification<Equipment> organizationId(
            Long organizationId
    ) {
        return (root, query, cb) ->
                organizationId == null
                        ? cb.conjunction()
                        : cb.equal(
                        root.get("organizationId"),
                        organizationId
                );
    }

    public static Specification<Equipment> branchId(
            Long branchId
    ) {
        return (root, query, cb) ->
                branchId == null
                        ? cb.conjunction()
                        : cb.equal(
                        root.get("branchId"),
                        branchId
                );
    }

    public static Specification<Equipment> warehouseId(
            Long warehouseId
    ) {
        return (root, query, cb) ->
                warehouseId == null
                        ? cb.conjunction()
                        : cb.equal(
                        root.get("warehouseId"),
                        warehouseId
                );
    }

    public static Specification<Equipment> modelId(
            Long modelId
    ) {
        return (root, query, cb) ->
                modelId == null
                        ? cb.conjunction()
                        : cb.equal(
                        root.get("modelId"),
                        modelId
                );
    }

    public static Specification<Equipment> status(
            EquipmentStatus status
    ) {
        return (root, query, cb) ->
                status == null
                        ? cb.conjunction()
                        : cb.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<Equipment> serialNumber(
            String serialNumber
    ) {
        return (root, query, cb) -> {

            if (serialNumber == null || serialNumber.isBlank()) {
                return cb.conjunction();
            }

            return cb.equal(
                    cb.upper(root.get("serialNumber")),
                    serialNumber.trim().toUpperCase()
            );
        };
    }

    public static Specification<Equipment> imei(
            String imei
    ) {
        return (root, query, cb) -> {

            if (imei == null || imei.isBlank()) {
                return cb.conjunction();
            }

            return cb.equal(
                    cb.upper(root.get("imei")),
                    imei.trim().toUpperCase()
            );
        };
    }

    public static Specification<Equipment> macAddress(
            String macAddress
    ) {
        return (root, query, cb) -> {

            if (macAddress == null || macAddress.isBlank()) {
                return cb.conjunction();
            }

            return cb.equal(
                    cb.upper(root.get("macAddress")),
                    macAddress.trim().toUpperCase()
            );
        };
    }
    public static Specification<Equipment> modelIds(
            java.util.List<Long> modelIds
    ) {

        return (root, query, cb) -> {

            if (modelIds == null) {
                return cb.conjunction();
            }

            if (modelIds.isEmpty()) {
                return cb.disjunction();
            }

            return root
                    .get("modelId")
                    .in(modelIds);
        };
    }
}