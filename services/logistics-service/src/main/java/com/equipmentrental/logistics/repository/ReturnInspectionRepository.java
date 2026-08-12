package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.ReturnInspection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnInspectionRepository extends JpaRepository<ReturnInspection, Long> {

    List<ReturnInspection> findByReturnRequestId(Long returnRequestId);
}
