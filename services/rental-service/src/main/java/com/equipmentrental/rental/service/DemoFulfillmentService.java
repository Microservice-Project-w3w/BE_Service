package com.equipmentrental.rental.service;

import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.repository.*;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.client.InventoryClient;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;

@Service
@Transactional
public class DemoFulfillmentService {
    private final DemoBillingService billing;
    private final DemoRentalEventRepository events;
    private final RentalContractRepository contracts;
    private final ContractService contractService;
    private final InventoryClient inventory;
    private final DemoSyncJournal journal;
    private final DemoSyncTaskRepository tasks;
    private final CurrentUserProvider users;
    private final ObjectMapper mapper;
    public DemoFulfillmentService(DemoBillingService billing,DemoRentalEventRepository events,
            RentalContractRepository contracts,ContractService contractService,InventoryClient inventory,
            DemoSyncJournal journal,DemoSyncTaskRepository tasks,CurrentUserProvider users,ObjectMapper mapper) {
        this.billing=billing; this.events=events; this.contracts=contracts; this.contractService=contractService;
        this.inventory=inventory; this.journal=journal; this.tasks=tasks; this.users=users; this.mapper=mapper;
    }
    public record HandoverInput(@NotBlank @Size(max=80) String requestKey,@NotBlank String condition,
            @NotBlank @Size(max=1000) String note,@NotBlank @Size(max=150) String receiverName,
            @NotNull @AssertTrue Boolean confirmed) {}
    public record SignatureInput(@NotBlank @Size(max=150) String signerName,@NotNull @AssertTrue Boolean accepted) {}
    public DemoBillingService.Summary sign(Long orderId,SignatureInput input) {
        var order=billing.order(orderId,true); DemoBillingService.requireConfirmed(order);
        if (!Boolean.TRUE.equals(input.accepted())) throw new ApiException("Cần đồng ý điều khoản hợp đồng");
        var contract=contracts.findByRentalOrderId(orderId).orElseThrow(() -> ApiException.notFound("Đơn chưa có hợp đồng"));
        if (contract.getStatus()!=ContractStatus.APPROVED) throw new ApiException("Hợp đồng chưa được duyệt để ký");
        String document=contract.getContractCode()+"|"+contract.getTerms()+"|"+contract.getTotalAmount()+"|"+contract.getStartAt()+"|"+contract.getEndAt();
        String hash;
        try { hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(document.getBytes(StandardCharsets.UTF_8))); }
        catch(Exception exception) { throw new IllegalStateException(exception); }
        var details=mapper.createObjectNode().put("contractId",contract.getId()).put("signerName",input.signerName().trim())
                .put("documentSha256",hash).put("actorUserId",users.getCurrentUser().userId())
                .put("type","DEMO_TYPED_ACCEPTANCE");
        contractService.sign(contract.getId());
        billing.append(orderId,"signature-"+contract.getId(),"SIGNATURE",BigDecimal.ZERO,null,
                "Xác nhận điện tử nội bộ; không phải chữ ký số chứng thư",details.toString());
        return billing.get(orderId);
    }
    public DemoBillingService.Summary handover(Long orderId,String kind,HandoverInput input) {
        var order=billing.order(orderId,true); DemoBillingService.requireConfirmed(order);
        if (!Set.of("DELIVERED","RETURNED").contains(kind)) throw new ApiException("Loại biên bản không hợp lệ");
        if (!Boolean.TRUE.equals(input.confirmed())) throw new ApiException("Cần xác nhận biên bản");
        if (!Set.of("NEW","GOOD","FAIR","POOR","DAMAGED").contains(input.condition())) throw new ApiException("Tình trạng thiết bị không hợp lệ");
        var previous=events.findByRentalOrderIdAndRequestKey(orderId,input.requestKey());
        if (previous.isPresent()) {
            var p=previous.get(); var old=read(p.details);
            if (!p.kind.equals(kind) || !Objects.equals(p.note,input.note())
                    || !old.path("receiverName").asText().equals(input.receiverName())
                    || !old.path("condition").asText().equals(input.condition())) throw new ApiException("requestKey đã dùng cho biên bản khác");
            return billing.get(orderId);
        }
        validateHandover(order,kind);
        var payload=mapper.createObjectNode().put("organizationId",order.getOrganizationId()).put("branchId",order.getBranchId())
                .put("rentalOrderId",orderId).put("reservationId",Long.valueOf(order.getInventoryReservationId()))
                .put("requestKey",input.requestKey()).put("kind",kind).put("condition",input.condition())
                .put("note",input.note()).put("receiverName",input.receiverName());
        var task=journal.begin(orderId,order.getOrganizationId(),order.getBranchId(),kind,payload.toString());
        finishHandover(task,payload);
        return billing.get(orderId);
    }
    private void validateHandover(RentalOrder order,String kind) {
        var summary=billing.get(order.getId());
        var history=summary.history();
        if (history.stream().anyMatch(e -> e.kind.equals(kind))) throw new ApiException("Đơn đã có biên bản loại này");
        if (kind.equals("DELIVERED")) {
            var contract=contracts.findByRentalOrderId(order.getId()).orElseThrow(() -> new ApiException("Chưa có hợp đồng"));
            if (!Set.of(ContractStatus.SIGNED,ContractStatus.ACTIVE,ContractStatus.EXTENDED).contains(contract.getStatus())) throw new ApiException("Hợp đồng chưa ký");
            if (summary.collectedDeposit().compareTo(summary.requiredDeposit())<0) throw new ApiException("Cần thu đủ cọc trước khi bàn giao");
            if (summary.invoiceCode()==null || summary.invoiceStatus().equals("CANCELLED")) throw new ApiException("Cần phát hành hóa đơn trước bàn giao");
        } else if (history.stream().noneMatch(e -> e.kind.equals("DELIVERED"))) throw new ApiException("Chưa bàn giao thiết bị");
    }
    public void liquidate(Long orderId) {
        billing.order(orderId,true);
        var contract=contracts.findByRentalOrderId(orderId).orElseThrow(() -> new ApiException("Chưa có hợp đồng"));
        contractService.liquidate(contract.getId());
    }
    private void finishHandover(DemoSyncTask task,JsonNode payload) {
        // Forward reconciliation: Inventory commits the whole batch once, retry returns the same report.
        var response=inventory.handover(payload);
        var details=payload.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode)details).set("inventoryReport",response);
        billing.append(task.rentalOrderId,payload.path("requestKey").asText(),task.kind,BigDecimal.ZERO,null,payload.path("note").asText(),details.toString());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status==STATUS_COMMITTED) {
                    try { journal.update(task.id,"DONE",null); } catch(RuntimeException ignored) { /* Retry can detect local event. */ }
                }
            }
        });
    }
    @Transactional(readOnly=true)
    public List<DemoSyncTask> tasks(Long orderId) { billing.order(orderId,false); return tasks.findByRentalOrderIdOrderByCreatedAtAsc(orderId); }
    public void recover(Long orderId,String taskId) {
        var order=billing.order(orderId,true);
        var task=tasks.findById(taskId).orElseThrow(() -> ApiException.notFound("Không tìm thấy thao tác đồng bộ"));
        if (!task.rentalOrderId.equals(orderId)) throw new ApiException("Thao tác không thuộc đơn này");
        if (!task.status.equals("PENDING")) return;
        var payload=read(task.payload);
        if (Set.of("DELIVERED","RETURNED").contains(task.kind)) {
            if (events.findByRentalOrderIdAndRequestKey(orderId,payload.path("requestKey").asText()).isPresent()) {
                journal.update(task.id,"DONE",null); return;
            }
            DemoBillingService.requireConfirmed(order); validateHandover(order,task.kind);
            finishHandover(task,payload); return;
        }
        boolean committed=switch(task.kind) {
            case "CREATE" -> order.getInventoryReservationId()!=null && Objects.equals(order.getReservedUntil(),
                    java.time.LocalDateTime.parse(payload.path("requestReference").asText().substring(order.getOrderCode().length()+1)));
            case "CONFIRM" -> order.getStatus()==OrderStatus.CONFIRMED;
            case "RELEASE" -> order.getStatus()==OrderStatus.CANCELLED;
            case "EXTEND" -> order.getEndAt().equals(java.time.LocalDateTime.parse(payload.path("newEndAt").asText()));
            default -> throw new ApiException("Loại đồng bộ không hỗ trợ");
        };
        if (committed) journal.update(task.id,"DONE",null);
        else { inventory.compensate(payload); journal.update(task.id,"COMPENSATED",null); }
    }
    private JsonNode read(String value) { try { return mapper.readTree(value); } catch(Exception e) { throw new ApiException("Dữ liệu đồng bộ không hợp lệ"); } }
}
