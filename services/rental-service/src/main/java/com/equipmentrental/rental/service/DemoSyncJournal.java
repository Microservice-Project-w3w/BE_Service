package com.equipmentrental.rental.service;
import com.equipmentrental.rental.entity.DemoSyncTask;
import com.equipmentrental.rental.repository.DemoSyncTaskRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
/** Independent journal survives a rollback or a process restart; never stores a bearer token. */
@Service
public class DemoSyncJournal {
    private final DemoSyncTaskRepository tasks;
    public DemoSyncJournal(DemoSyncTaskRepository tasks) { this.tasks=tasks; }
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public DemoSyncTask begin(Long orderId,Long org,Long branch,String kind,String payload) {
        if (tasks.existsByRentalOrderIdAndStatus(orderId,"PENDING"))
            throw new IllegalStateException("Đơn còn thao tác đồng bộ chưa xử lý; thử lại đồng bộ trước");
        var t=new DemoSyncTask(); t.id=UUID.randomUUID().toString(); t.rentalOrderId=orderId;
        t.organizationId=org; t.branchId=branch; t.kind=kind; t.payload=payload;
        return tasks.saveAndFlush(t);
    }
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void update(String id,String status,String error) {
        var t=tasks.findById(id).orElseThrow(); t.status=status; t.lastError=error;
        t.updatedAt=LocalDateTime.now(); t.attempts++; tasks.saveAndFlush(t);
    }
}
