package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.ReturnRecordItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRecordItemRepository extends JpaRepository<ReturnRecordItem, Long> {
    List<ReturnRecordItem> findByReturnRecordId(Long returnRecordId);
}
