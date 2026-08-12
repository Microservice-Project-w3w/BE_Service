package com.equipmentrental.logistics.repository;

import com.equipmentrental.logistics.entity.DispatchNoteItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DispatchNoteItemRepository extends JpaRepository<DispatchNoteItem, Long> {
    List<DispatchNoteItem> findByDispatchNoteId(Long dispatchNoteId);
}
