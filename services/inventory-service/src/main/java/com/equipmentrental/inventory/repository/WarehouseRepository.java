package com.equipmentrental.inventory.repository;


import com.equipmentrental.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface WarehouseRepository
        extends JpaRepository<Warehouse,Long> {



    List<Warehouse> findByOrganizationId(
            Long organizationId
    );


    List<Warehouse> findByOrganizationIdAndBranchId(
            Long organizationId,
            Long branchId
    );

}