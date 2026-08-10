package com.equipmentrental.rental.service;

import com.equipmentrental.rental.client.InventoryClient;
import com.equipmentrental.rental.dto.QuotationCreate;
import com.equipmentrental.rental.dto.request.CancelOrderRequest;
import com.equipmentrental.rental.dto.request.QuotationUpdate;
import com.equipmentrental.rental.dto.request.RentalRequestCreate;
import com.equipmentrental.rental.dto.request.RentalRequestUpdate;
import com.equipmentrental.rental.dto.request.ReserveOrderRequest;
import com.equipmentrental.rental.dto.response.QuotationResponse;
import com.equipmentrental.rental.dto.response.RentalOrderResponse;
import com.equipmentrental.rental.dto.response.RentalRequestResponse;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.mapper.RentalResponseMapper;
import com.equipmentrental.rental.repository.*;
import com.equipmentrental.rental.security.RentalDataScopeGuard;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RentalWorkflowService {
    private final RentalRequestRepository requests;
    private final QuotationRepository quotations;
    private final RentalOrderRepository orders;
    private final PricingService pricing;
    private final RentalDataScopeGuard dataScopeGuard;
    private final InventoryClient inventoryClient;

    public RentalWorkflowService(
            RentalRequestRepository r,
            QuotationRepository q,
            RentalOrderRepository o,
            PricingService p,
            RentalDataScopeGuard dataScopeGuard,
            InventoryClient inventoryClient) {
        requests = r;
        quotations = q;
        orders = o;
        pricing = p;
        this.dataScopeGuard = dataScopeGuard;
        this.inventoryClient = inventoryClient;
    }

    public RentalRequestResponse createRequest(RentalRequestCreate r) {
        if (!r.endAt().isAfter(r.startAt())) throw new ApiException("Thời gian trả phải sau thời gian nhận");
        dataScopeGuard.requireBranch(r.organizationId(), r.branchId());
        RentalRequest e = new RentalRequest();
        e.setRequestCode("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        e.setOrganizationId(r.organizationId());
        e.setBranchId(r.branchId());
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
        return RentalResponseMapper.request(requests.save(e));
    }

    @Transactional(readOnly = true)
    public List<RentalRequestResponse> getRequests(Long organizationId, Long branchId) {
        dataScopeGuard.requireBranch(organizationId, branchId);
        return requests.findByOrganizationIdAndBranchId(organizationId, branchId).stream()
                .map(RentalResponseMapper::request)
                .toList();
    }

    @Transactional(readOnly = true)
    public RentalRequestResponse getRequest(Long id) {
        return RentalResponseMapper.request(findRequest(id));
    }

    public RentalRequestResponse cancelRequest(Long id) {
        RentalRequest e = findRequest(id);
        if (e.getStatus() != RequestStatus.DRAFT && e.getStatus() != RequestStatus.SUBMITTED) {
            throw ApiException.invalidStatus("Chỉ được hủy yêu cầu ở trạng thái DRAFT hoặc SUBMITTED");
        }
        e.setStatus(RequestStatus.CANCELLED);
        return RentalResponseMapper.request(requests.save(e));
    }

    public JsonNode availability(
            Long organizationId,
            Long branchId,
            Long equipmentTypeId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer requiredQuantity) {
        if (!endAt.isAfter(startAt)) throw new ApiException("Khoảng thời gian không hợp lệ");
        dataScopeGuard.requireBranch(organizationId, branchId);
        return inventoryClient.availability(
                organizationId, branchId, equipmentTypeId, startAt, endAt, requiredQuantity);
    }

    public RentalRequestResponse updateRequest(Long id, RentalRequestUpdate request) {
        RentalRequest rentalRequest = findRequest(id);
        if (rentalRequest.getStatus() != RequestStatus.DRAFT && rentalRequest.getStatus() != RequestStatus.SUBMITTED) {
            throw ApiException.invalidStatus("Không thể sửa yêu cầu đã được xử lý");
        }
        if (!request.endAt().isAfter(request.startAt()))
            throw new ApiException("Thời gian trả phải sau thời gian nhận");
        rentalRequest.setStartAt(request.startAt());
        rentalRequest.setEndAt(request.endAt());
        rentalRequest.setDeliveryAddress(request.deliveryAddress());
        rentalRequest.setNote(request.note());
        List<RentalRequestItem> items = request.items().stream()
                .map(value -> {
                    RentalRequestItem item = new RentalRequestItem();
                    item.setEquipmentTypeId(value.equipmentTypeId());
                    item.setQuantity(value.quantity());
                    return item;
                })
                .toList();
        rentalRequest.replaceItems(items);
        return RentalResponseMapper.request(requests.save(rentalRequest));
    }

    public QuotationResponse createQuotation(QuotationCreate r) {
        RentalRequest req = findRequest(r.rentalRequestId());
        if (r.validUntil().isBefore(LocalDateTime.now())) throw new ApiException("Hạn báo giá phải ở tương lai");
        BigDecimal discount = pricing.calculateDiscount(
                r.discountCode(), r.rentalAmount().add(r.deliveryFee()), req.getOrganizationId(), req.getBranchId());
        BigDecimal total = r.rentalAmount().add(r.deliveryFee()).subtract(discount);
        Quotation q = new Quotation();
        q.setQuotationCode("QUO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        q.setOrganizationId(req.getOrganizationId());
        q.setBranchId(req.getBranchId());
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
        return RentalResponseMapper.quotation(quotations.save(q));
    }

    @Transactional(readOnly = true)
    public List<QuotationResponse> getQuotations(Long organizationId, Long branchId) {
        dataScopeGuard.requireBranch(organizationId, branchId);
        return quotations.findByOrganizationIdAndBranchId(organizationId, branchId).stream()
                .map(RentalResponseMapper::quotation)
                .toList();
    }

    public QuotationResponse sendQuotation(Long id) {
        Quotation q = findQuotation(id);
        if (q.getStatus() != QuotationStatus.DRAFT) throw ApiException.invalidStatus("Chỉ gửi được báo giá DRAFT");
        q.setStatus(QuotationStatus.SENT);
        return RentalResponseMapper.quotation(quotations.save(q));
    }

    public QuotationResponse approveQuotation(Long id) {
        Quotation q = findQuotation(id);
        if (q.getStatus() != QuotationStatus.SENT)
            throw ApiException.invalidStatus("Chỉ phê duyệt được báo giá đã gửi");
        q.setStatus(QuotationStatus.APPROVED);
        return RentalResponseMapper.quotation(quotations.save(q));
    }

    public QuotationResponse acceptQuotation(Long id) {
        Quotation q = findQuotation(id);
        if (q.getStatus() != QuotationStatus.SENT && q.getStatus() != QuotationStatus.APPROVED)
            throw ApiException.invalidStatus("Báo giá chưa sẵn sàng để chấp nhận");
        q.setStatus(QuotationStatus.ACCEPTED);
        return RentalResponseMapper.quotation(quotations.save(q));
    }

    public QuotationResponse rejectQuotation(Long id) {
        Quotation q = findQuotation(id);
        if (q.getStatus() != QuotationStatus.SENT && q.getStatus() != QuotationStatus.APPROVED)
            throw ApiException.invalidStatus("Báo giá không thể từ chối ở trạng thái hiện tại");
        q.setStatus(QuotationStatus.REJECTED);
        return RentalResponseMapper.quotation(quotations.save(q));
    }

    public QuotationResponse updateQuotation(Long id, QuotationUpdate update) {
        Quotation quotation = findQuotation(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT)
            throw ApiException.invalidStatus("Chỉ sửa được báo giá DRAFT");
        if (update.validUntil().isBefore(LocalDateTime.now())) throw new ApiException("Hạn báo giá phải ở tương lai");
        BigDecimal discount = pricing.calculateDiscount(
                update.discountCode(),
                update.rentalAmount().add(update.deliveryFee()),
                quotation.getOrganizationId(),
                quotation.getBranchId());
        quotation.setRentalAmount(update.rentalAmount());
        quotation.setDepositAmount(update.depositAmount());
        quotation.setDeliveryFee(update.deliveryFee());
        quotation.setDiscountCode(update.discountCode());
        quotation.setDiscountAmount(discount);
        quotation.setTotalAmount(update.rentalAmount().add(update.deliveryFee()).subtract(discount));
        quotation.setValidUntil(update.validUntil());
        quotation.setSpecialTerms(update.specialTerms());
        return RentalResponseMapper.quotation(quotations.save(quotation));
    }

    public RentalOrderResponse convertToOrder(Long id) {
        Quotation q = findQuotation(id);
        if (q.getStatus() != QuotationStatus.ACCEPTED)
            throw ApiException.invalidStatus("Báo giá chưa được khách hàng chấp nhận");
        RentalRequest req = findRequest(q.getRentalRequestId());
        RentalOrder o = new RentalOrder();
        o.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        o.setOrganizationId(q.getOrganizationId());
        o.setBranchId(q.getBranchId());
        o.setQuotationId(q.getId());
        o.setCustomerId(q.getCustomerId());
        o.setStartAt(req.getStartAt());
        o.setEndAt(req.getEndAt());
        o.setTotalAmount(q.getTotalAmount());
        q.setStatus(QuotationStatus.CONVERTED);
        quotations.save(q);
        return RentalResponseMapper.order(orders.save(o));
    }

    public RentalOrderResponse reserve(Long id, ReserveOrderRequest r) {
        RentalOrder o = findOrder(id);
        if (o.getStatus() != OrderStatus.PENDING) throw ApiException.invalidStatus("Chỉ giữ chỗ đơn PENDING");
        if (!r.reservedUntil().isAfter(LocalDateTime.now()))
            throw new ApiException("Thời hạn giữ chỗ phải ở tương lai");
        String reservationId = inventoryClient.createReservation(o, r.reservedUntil());
        o.setInventoryReservationId(reservationId);
        o.setReservedUntil(r.reservedUntil());
        o.setStatus(OrderStatus.RESERVED);
        return RentalResponseMapper.order(orders.save(o));
    }

    public RentalOrderResponse cancelOrder(Long id, CancelOrderRequest r) {
        RentalOrder o = findOrder(id);
        if (o.getStatus() == OrderStatus.CANCELLED) throw ApiException.invalidStatus("Đơn đã bị hủy");
        if (o.getInventoryReservationId() != null) inventoryClient.releaseReservation(o.getInventoryReservationId());
        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(r.reason());
        return RentalResponseMapper.order(orders.save(o));
    }

    public RentalOrderResponse confirmOrder(Long id) {
        RentalOrder order = findOrder(id);
        if (order.getStatus() != OrderStatus.RESERVED || order.getInventoryReservationId() == null)
            throw ApiException.invalidStatus("Đơn chưa có giữ chỗ hợp lệ để xác nhận");
        inventoryClient.confirmReservation(order.getInventoryReservationId());
        order.setStatus(OrderStatus.CONFIRMED);
        return RentalResponseMapper.order(orders.save(order));
    }

    @Transactional(readOnly = true)
    public List<RentalOrderResponse> getOrders(Long organizationId, Long branchId) {
        dataScopeGuard.requireBranch(organizationId, branchId);
        return orders.findByOrganizationIdAndBranchId(organizationId, branchId).stream()
                .map(RentalResponseMapper::order)
                .toList();
    }

    private RentalRequest findRequest(Long id) {
        RentalRequest request =
                requests.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy yêu cầu thuê"));
        dataScopeGuard.requireBranch(request.getOrganizationId(), request.getBranchId());
        return request;
    }

    private Quotation findQuotation(Long id) {
        Quotation quotation =
                quotations.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy báo giá"));
        dataScopeGuard.requireBranch(quotation.getOrganizationId(), quotation.getBranchId());
        return quotation;
    }

    private RentalOrder findOrder(Long id) {
        RentalOrder order = orders.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn thuê"));
        dataScopeGuard.requireBranch(order.getOrganizationId(), order.getBranchId());
        return order;
    }
}
