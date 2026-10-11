package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.entity.*;
import com.equipmentrental.inventory.enums.*;
import com.equipmentrental.inventory.repository.*;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemoInventoryService {
    private final EquipmentRepository equipment;
    private final EquipmentReservationRepository reservations;
    private final EquipmentReservationItemRepository items;
    private final WarehouseRepository warehouses;
    private final DemoInventoryOperationRepository operations;
    private final DemoMaintenanceTicketRepository tickets;
    private final InventoryDataScopeGuard scope;
    private final CurrentUserProvider users;
    private final EquipmentTransactionService transactions;
    private final ObjectMapper mapper;
    public DemoInventoryService(EquipmentRepository equipment, EquipmentReservationRepository reservations,
            EquipmentReservationItemRepository items, WarehouseRepository warehouses,
            DemoInventoryOperationRepository operations, DemoMaintenanceTicketRepository tickets,
            InventoryDataScopeGuard scope, CurrentUserProvider users, EquipmentTransactionService transactions,
            ObjectMapper mapper) {
        this.equipment=equipment; this.reservations=reservations; this.items=items; this.warehouses=warehouses;
        this.operations=operations; this.tickets=tickets; this.scope=scope; this.users=users;
        this.transactions=transactions; this.mapper=mapper;
    }
    public record HandoverInput(@NotNull Long organizationId,@NotNull Long branchId,
            @NotNull Long rentalOrderId,@NotNull Long reservationId,@NotBlank @Size(max=80) String requestKey,
            @NotBlank String kind,@NotNull EquipmentCondition condition,@NotBlank @Size(max=1000) String note) {}
    public record Item(Long equipmentId, Long warehouseId, EquipmentCondition condition) {}
    public record Report(List<Item> items, EquipmentCondition condition, String note) {}
    public record MaintenanceInput(@NotNull Long organizationId,@NotNull Long branchId,
            @NotNull Long equipmentId,@NotNull LocalDateTime scheduledAt,@NotBlank @Size(max=1000) String reason) {}
    public record MaintenanceResult(@NotBlank @Size(max=1000) String result,
            @NotNull @DecimalMin("0") @Digits(integer=13,fraction=2) BigDecimal cost) {}

    public DemoInventoryOperation handover(HandoverInput input) {
        scope.requireScope(input.organizationId(),input.branchId());
        if (!Set.of("DELIVERED","RETURNED").contains(input.kind())) throw invalid("Loại bàn giao không hợp lệ");
        var reservation=reservations.findForDemoUpdate(input.reservationId()).orElseThrow(() -> invalid("Không tìm thấy giữ chỗ"));
        if (!Objects.equals(reservation.getOrganizationId(),input.organizationId())
                || !Objects.equals(reservation.getBranchId(),input.branchId())
                || !Objects.equals(reservation.getRentalOrderId(),input.rentalOrderId())) throw invalid("Giữ chỗ không thuộc đơn/chi nhánh");
        var existing=operations.findByRequestKey(input.requestKey());
        if (existing.isPresent()) {
            var old=existing.get(); var report=read(old.details);
            if (!old.rentalOrderId.equals(input.rentalOrderId()) || !old.kind.equals(input.kind())
                    || report.condition()!=input.condition() || !report.note().equals(input.note())) throw invalid("requestKey đã dùng cho nội dung khác");
            return old;
        }
        if (operations.findByRentalOrderIdAndKind(input.rentalOrderId(),input.kind()).isPresent())
            throw invalid("Đơn đã có biên bản loại này, không được thực hiện lần hai");
        if (reservation.getStatus()!=ReservationStatus.CONFIRMED) throw invalid("Giữ chỗ chưa xác nhận hoặc đã kết thúc");
        var ids=items.findByReservationId(reservation.getId()).stream().map(EquipmentReservationItem::getEquipmentId).sorted().toList();
        if (ids.isEmpty()) throw invalid("Giữ chỗ không có thiết bị");
        var previous=input.kind().equals("RETURNED") ? operations.findByRentalOrderIdAndKind(input.rentalOrderId(),"DELIVERED")
                .map(o -> read(o.details)).orElseThrow(() -> invalid("Chưa có bàn giao để nhận trả")) : null;
        var snapshots=new ArrayList<Item>();
        for (Long id:ids) {
            var e=equipment.lockForReservation(id,input.organizationId()).orElseThrow(() -> invalid("Không tìm thấy thiết bị"));
            if (!input.branchId().equals(e.getBranchId())) throw invalid("Thiết bị khác chi nhánh");
            var oldStatus=e.getStatus(); var oldWarehouse=e.getWarehouseId();
            if (input.kind().equals("DELIVERED")) {
                if (oldStatus!=EquipmentStatus.AVAILABLE || oldWarehouse==null) throw invalid("Thiết bị chưa sẵn sàng trong kho");
                warehouse(oldWarehouse,input.organizationId(),input.branchId());
                snapshots.add(new Item(id,oldWarehouse,e.getConditionStatus()));
                e.setStatus(EquipmentStatus.CHECKED_OUT); e.setWarehouseId(null); e.setWarehouseLocationId(null);
            } else {
                if (oldStatus!=EquipmentStatus.CHECKED_OUT) throw invalid("Thiết bị không đang cho thuê");
                Long warehouseId=previous.items().stream().filter(i -> i.equipmentId().equals(id)).findFirst()
                        .orElseThrow(() -> invalid("Thiết bị không nằm trong biên bản bàn giao")).warehouseId();
                warehouse(warehouseId,input.organizationId(),input.branchId());
                e.setWarehouseId(warehouseId); e.setConditionStatus(input.condition());
                e.setStatus(input.condition()==EquipmentCondition.DAMAGED || input.condition()==EquipmentCondition.POOR
                        ? EquipmentStatus.INSPECTION : EquipmentStatus.AVAILABLE);
                snapshots.add(new Item(id,warehouseId,input.condition()));
            }
            equipment.save(e);
            transactions.record(e,input.kind().equals("DELIVERED") ? EquipmentTransactionType.CHECKOUT : EquipmentTransactionType.CHECKIN,
                    oldWarehouse,e.getWarehouseId(),"RENTAL_ORDER",input.rentalOrderId(),input.requestKey(),
                    oldStatus.name(),e.getStatus().name(),Long.valueOf(users.getCurrentUser().userId()),input.note());
        }
        if (input.kind().equals("RETURNED")) { reservation.setStatus(ReservationStatus.RELEASED); reservations.save(reservation); }
        var operation=new DemoInventoryOperation(); operation.requestKey=input.requestKey(); operation.kind=input.kind();
        operation.organizationId=input.organizationId(); operation.branchId=input.branchId(); operation.rentalOrderId=input.rentalOrderId();
        operation.reservationId=input.reservationId(); operation.actorUserId=users.getCurrentUser().userId();
        operation.details=write(new Report(snapshots,input.condition(),input.note()));
        return operations.saveAndFlush(operation);
    }
    private void warehouse(Long id,Long org,Long branch) {
        var w=warehouses.findById(id).orElseThrow(() -> invalid("Kho không tồn tại"));
        if (!org.equals(w.getOrganizationId()) || !branch.equals(w.getBranchId()) || !Boolean.TRUE.equals(w.getActive()))
            throw invalid("Kho không hoạt động hoặc khác phạm vi");
    }
    @Transactional(readOnly=true)
    public List<DemoMaintenanceTicket> list(Long organizationId,Long branchId) {
        scope.requireScope(organizationId,branchId);
        return tickets.findByOrganizationIdAndBranchIdOrderByScheduledAtAsc(organizationId,branchId);
    }
    public DemoMaintenanceTicket plan(MaintenanceInput input) {
        scope.requireScope(input.organizationId(),input.branchId());
        var e=equipment.lockForReservation(input.equipmentId(),input.organizationId()).orElseThrow(() -> invalid("Không tìm thấy thiết bị"));
        if (!input.branchId().equals(e.getBranchId())) throw invalid("Thiết bị khác chi nhánh");
        if (tickets.existsByEquipmentIdAndStatusIn(e.getId(),List.of("PLANNED","IN_PROGRESS"))) throw invalid("Thiết bị đã có phiếu bảo trì chưa xong");
        var t=new DemoMaintenanceTicket(); t.organizationId=input.organizationId(); t.branchId=input.branchId();
        t.equipmentId=e.getId(); t.scheduledAt=input.scheduledAt(); t.reason=input.reason(); t.createdBy=users.getCurrentUser().userId();
        return tickets.saveAndFlush(t);
    }
    public DemoMaintenanceTicket transition(Long id,String action,MaintenanceResult result) {
        Long equipmentId=tickets.findEquipmentId(id).orElseThrow(() -> invalid("Không tìm thấy phiếu bảo trì"));
        var organizationId=equipment.findDemoOrganizationId(equipmentId).orElseThrow();
        scope.requireScope(organizationId,null);
        var e=equipment.lockForReservation(equipmentId,organizationId).orElseThrow();
        scope.requireScope(e.getOrganizationId(),e.getBranchId());
        // Reload only after the asset lock to serialize concurrent actions on the same ticket.
        var t=tickets.findById(id).orElseThrow();
        if (action.equals("start")) {
            if (!t.status.equals("PLANNED")) throw invalid("Phiếu không ở trạng thái đã lên lịch");
            if (!Set.of(EquipmentStatus.AVAILABLE,EquipmentStatus.INSPECTION,EquipmentStatus.DAMAGED).contains(e.getStatus()) || e.getWarehouseId()==null)
                throw invalid("Thiết bị phải ở kho và chưa cho thuê/chuyển kho");
            if (items.countFutureBlocking(e.getId(),LocalDateTime.now())>0) throw invalid("Thiết bị còn giữ chỗ, cần xử lý đơn trước khi bảo trì");
            e.setStatus(EquipmentStatus.MAINTENANCE); t.status="IN_PROGRESS"; t.startedAt=LocalDateTime.now();
        } else if (action.equals("complete")) {
            if (!t.status.equals("IN_PROGRESS") || e.getStatus()!=EquipmentStatus.MAINTENANCE) throw invalid("Thiết bị chưa trong phiên bảo trì này");
            if (result==null || result.result()==null || result.result().isBlank() || result.cost()==null || result.cost().signum()<0)
                throw invalid("Cần kết quả và chi phí bảo trì hợp lệ");
            e.setStatus(EquipmentStatus.AVAILABLE); e.setConditionStatus(EquipmentCondition.GOOD);
            t.status="COMPLETED"; t.result=result.result(); t.cost=result.cost(); t.completedAt=LocalDateTime.now(); t.completedBy=users.getCurrentUser().userId();
        } else if (action.equals("cancel")) {
            if (!t.status.equals("PLANNED")) throw invalid("Chỉ hủy phiếu chưa bắt đầu");
            t.status="CANCELLED";
        } else throw invalid("Thao tác bảo trì không hợp lệ");
        equipment.save(e); return tickets.saveAndFlush(t);
    }
    private Report read(String value) { try { return mapper.readValue(value,Report.class); } catch(Exception e) { throw invalid("Biên bản lưu không hợp lệ"); } }
    private String write(Object value) { try { return mapper.writeValueAsString(value); } catch(Exception e) { throw invalid("Không lưu được biên bản"); } }
    private IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
