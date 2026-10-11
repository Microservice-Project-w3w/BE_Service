package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.entity.*;
import com.equipmentrental.inventory.enums.*;
import com.equipmentrental.inventory.repository.*;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemoReservationRecoveryService {
    private final EquipmentReservationRepository reservations;
    private final EquipmentReservationItemRepository items;
    private final EquipmentRepository equipment;
    private final DemoInventoryOperationRepository operations;
    private final InventoryDataScopeGuard scope;
    @org.springframework.beans.factory.annotation.Value("${RENTAL_SERVICE_URL:http://localhost:8084}")
    private String rentalUrl;
    public DemoReservationRecoveryService(EquipmentReservationRepository reservations,
            EquipmentReservationItemRepository items,EquipmentRepository equipment,
            DemoInventoryOperationRepository operations,InventoryDataScopeGuard scope) {
        this.reservations=reservations; this.items=items; this.equipment=equipment; this.operations=operations; this.scope=scope;
    }
    @Transactional(readOnly=true)
    public EquipmentReservation snapshot(Long id) {
        var r=reservations.findById(id).orElseThrow(); scope.requireScope(r.getOrganizationId(),r.getBranchId());
        requireOwnCustomerOrder(r); return r;
    }
    public void compensate(JsonNode input) {
        Long org=input.path("organizationId").asLong(); Long branch=input.path("branchId").asLong();
        Long orderId=input.path("rentalOrderId").asLong(); String kind=input.path("kind").asText();
        scope.requireScope(org,branch);
        if (!Set.of("CREATE","CONFIRM","RELEASE","EXTEND").contains(kind)) throw invalid("Loại bù trừ không hợp lệ");
        var candidate=kind.equals("CREATE") ? reservations.findDemoIdByReference(org,input.path("requestReference").asText())
                : Optional.of(input.path("id").asLong());
        if (candidate.isEmpty() && kind.equals("CREATE")) return; // Remote creation never committed.
        var r=reservations.findForDemoUpdate(candidate.orElseThrow()).orElseThrow();
        if (!org.equals(r.getOrganizationId()) || !branch.equals(r.getBranchId()) || !orderId.equals(r.getRentalOrderId())) throw invalid("Giữ chỗ khác phạm vi");
        requireOwnCustomerOrder(r);
        if (!kind.equals("EXTEND") && operations.findByRentalOrderIdAndKind(orderId,"DELIVERED").isPresent()) throw invalid("Đã bàn giao thực tế; không bù trừ giữ chỗ tự động");
        if (kind.equals("CREATE")) {
            if (r.getStatus()==ReservationStatus.RELEASED || r.getStatus()==ReservationStatus.EXPIRED) return;
            if (r.getStatus()!=ReservationStatus.HELD) throw invalid("Giữ chỗ đã được xử lý tiếp, cần kiểm tra thủ công");
            r.setStatus(ReservationStatus.RELEASED);
        } else {
            var oldStatus=ReservationStatus.valueOf(input.path("status").asText());
            var oldEnd=LocalDateTime.parse(input.path("endAt").asText());
            if (r.getStatus()==oldStatus && r.getEndAt().equals(oldEnd)) return;
            boolean expected=kind.equals("CONFIRM") && r.getStatus()==ReservationStatus.CONFIRMED && oldStatus==ReservationStatus.HELD
                    || kind.equals("RELEASE") && r.getStatus()==ReservationStatus.RELEASED
                    || kind.equals("EXTEND") && r.getStatus()==oldStatus && r.getEndAt().equals(LocalDateTime.parse(input.path("newEndAt").asText()));
            if (!expected) throw invalid("Giữ chỗ đã thay đổi ngoài thao tác cần bù trừ");
            if (!Set.of(ReservationStatus.HELD,ReservationStatus.CONFIRMED).contains(oldStatus)) throw invalid("Không khôi phục trạng thái này");
            var rows=items.findByReservationId(r.getId());
            for (Long id:rows.stream().map(EquipmentReservationItem::getEquipmentId).sorted().toList()) {
                var e=equipment.lockForReservation(id,org).orElseThrow();
                if (!kind.equals("EXTEND") && e.getStatus()!=EquipmentStatus.AVAILABLE) throw invalid("Thiết bị không còn sẵn sàng để khôi phục giữ chỗ");
            }
            for (var row:rows) {
                var blocking=items.findBlockingItems(org,branch,row.getEquipmentTypeId(),List.of(row.getEquipmentId()),
                        List.of(ReservationStatus.HELD,ReservationStatus.CONFIRMED),r.getStartAt(),oldEnd,LocalDateTime.now());
                if (blocking.stream().anyMatch(b -> !b.getReservationId().equals(r.getId()))) throw invalid("Không khôi phục do trùng giữ chỗ mới");
                row.setEndAt(oldEnd);
            }
            r.setEndAt(oldEnd); r.setStatus(oldStatus);
            if (oldStatus==ReservationStatus.HELD && r.getExpiresAt()!=null && !r.getExpiresAt().isAfter(LocalDateTime.now())) r.setStatus(ReservationStatus.EXPIRED);
            items.saveAll(rows);
        }
        reservations.saveAndFlush(r);
    }
    private IllegalStateException invalid(String message) { return new IllegalStateException(message); }
    private void requireOwnCustomerOrder(EquipmentReservation reservation) {
        var auth=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwt
                && jwt.getToken().getClaimAsStringList("roles")!=null && jwt.getToken().getClaimAsStringList("roles").contains("CUSTOMER")) {
            var factory=new org.springframework.http.client.SimpleClientHttpRequestFactory();factory.setConnectTimeout(1500);factory.setReadTimeout(2500);
            try {
                org.springframework.web.client.RestClient.builder().baseUrl(rentalUrl).requestFactory(factory).build().get()
                        .uri("/api/v1/rental-orders/{id}",reservation.getRentalOrderId())
                        .headers(h->h.setBearerAuth(jwt.getToken().getTokenValue())).retrieve().toBodilessEntity();
            } catch(org.springframework.web.client.RestClientException exception) {
                throw new org.springframework.security.access.AccessDeniedException("Không xác minh được quyền sở hữu đơn thuê");
            }
        }
    }
}
