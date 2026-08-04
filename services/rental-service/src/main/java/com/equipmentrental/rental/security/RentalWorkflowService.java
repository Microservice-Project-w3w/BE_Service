package com.equipmentrental.rental.service;

import com.equipmentrental.rental.dto.*;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class RentalWorkflowService {
    private final RentalRequestRepository requests;
    private final QuotationRepository quotations;
    private final RentalOrderRepository orders;
    private final PricingService pricing;

    public RentalWorkflowService(RentalRequestRepository r, QuotationRepository q, RentalOrderRepository o,
                                 PricingService p) {
        requests = r;
        quotations = q;
        orders = o;
        pricing = p;
    }

    public RentalRequest createRequest(RentalRequestCreate r) {
        if (!r.endAt().isAfter(r.startAt()))
            throw new ApiException("Thời gian trả phải sau thời gian nhận");
        RentalRequest e = new RentalRequest();
        e.setRequestCode("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        e.setCustomerId(r.customerId());
        e.setStartAt(r.startAt());
        e.setEndAt(r.endAt());
        e.setDeliveryAddress(r.deliveryAddress());
        e.setNote(r.note());
        r.items().forEach(i -> {
            RentalRequestItem item = new RentalRequestItem();
            item.setEquipmentTypeId(i.equipmentTypeId());
            item.setQuantity(i.quantity());
            e.addItem(item);
        });
        return requests.save(e);
    }

    public List<RentalRequest> getRequests() {
        return requests.findAll();
    }

    public RentalRequest getRequest(Long id) {
        return requests.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu thuê"));
    }

    public RentalRequest cancelRequest(Long id) {
        RentalRequest e = getRequest(id);
        e.setStatus(RequestStatus.CANCELLED);
        return requests.save(e);
    }

    public Map<String, Object> availability(Long equipmentTypeId, LocalDateTime startAt, LocalDateTime endAt,
                                            Integer requiredQuantity) {
        if (!endAt.isAfter(startAt))
            throw new ApiException("Khoảng thời gian không hợp lệ");
        return Map.of("equipmentTypeId", equipmentTypeId, "startAt", startAt, "endAt", endAt, "requiredQuantity",
                requiredQuantity,
                "available", true, "note", "Bản hiện tại chưa gọi inventory-service; cần tích hợp số lượng thực tế.");
    }

    public Quotation createQuotation(QuotationCreate r) {
        RentalRequest req = getRequest(r.rentalRequestId());
        BigDecimal discount = pricing.calculateDiscount(r.discountCode(), r.rentalAmount().add(r.deliveryFee()));
        BigDecimal total = r.rentalAmount().add(r.deliveryFee()).subtract(discount);
        Quotation q = new Quotation();
        q.setQuotationCode("QUO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        q.setRentalRequestId(req.getId());
        q.setCustomerId(req.getCustomerId());
        q.setRentalAmount(r.rentalAmount());
        q.setDepositAmount(r.depositAmount());
        q.setDeliveryFee(r.deliveryFee());
        q.setDiscountCode(r.discountCode());
        q.setDiscountAmount(discount);
        q.setTotalAmount(total);
        q.setValidUntil(r.validUntil());
        q.setSpecialTerms(r.specialTerms());
        req.setStatus(RequestStatus.QUOTED);
        requests.save(req);
        return quotations.save(q);
    }

    public List<Quotation> getQuotations() {
        return quotations.findAll();
    }

    public Quotation sendQuotation(Long id) {
        Quotation q = findQuotation(id);
        q.setStatus(QuotationStatus.SENT);
        return quotations.save(q);
    }

    public Quotation approveQuotation(Long id) {
        Quotation q = findQuotation(id);
        q.setStatus(QuotationStatus.APPROVED);
        return quotations.save(q);
    }

    public Quotation acceptQuotation(Long id) {
        Quotation q = findQuotation(id);
        q.setStatus(QuotationStatus.ACCEPTED);
        return quotations.save(q);
    }

    public RentalOrder convertToOrder(Long id) {
        Quotation q = findQuotation(id);
        if (q.getStatus() != QuotationStatus.ACCEPTED && q.getStatus() != QuotationStatus.APPROVED)
            throw new ApiException("Báo giá chưa được chấp nhận/phê duyệt");
        RentalRequest req = getRequest(q.getRentalRequestId());
        RentalOrder o = new RentalOrder();
        o.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        o.setQuotationId(q.getId());
        o.setCustomerId(q.getCustomerId());
        o.setStartAt(req.getStartAt());
        o.setEndAt(req.getEndAt());
        o.setTotalAmount(q.getTotalAmount());
        q.setStatus(QuotationStatus.CONVERTED);
        quotations.save(q);
        return orders.save(o);
    }

    public RentalOrder reserve(Long id, ReserveOrderRequest r) {
        RentalOrder o = findOrder(id);
        if (!r.reservedUntil().isAfter(LocalDateTime.now()))
            throw new ApiException("Thời hạn giữ chỗ phải ở tương lai");
        o.setReservedUntil(r.reservedUntil());
        o.setStatus(OrderStatus.RESERVED);
        return orders.save(o);
    }

    public RentalOrder cancelOrder(Long id, CancelOrderRequest r) {
        RentalOrder o = findOrder(id);
        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(r.reason());
        return orders.save(o);
    }

    public List<RentalOrder> getOrders() {
        return orders.findAll();
    }

    private Quotation findQuotation(Long id) {
        return quotations.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy báo giá"));
    }

    private RentalOrder findOrder(Long id) {
        return orders.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy đơn thuê"));
    }
}
