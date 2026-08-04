package com.equipmentrental.rental.mapper;

import com.equipmentrental.rental.dto.response.*;
import com.equipmentrental.rental.entity.*;
import java.util.List;

public final class RentalResponseMapper {
    private RentalResponseMapper() {
    }

    public static RentalPriceResponse price(RentalPrice source) {
        return new RentalPriceResponse(source.getId(), source.getOrganizationId(), source.getBranchId(),
                source.getPriceName(), source.getEquipmentTypeId(), source.getRentalUnit(), source.getRentalPrice(),
                source.getDepositType(), source.getDepositValue(), source.getLateFee(), source.getValidFrom(),
                source.getValidTo(), source.getActive(), source.getDescription(), source.getCreatedAt(), source.getUpdatedAt());
    }

    public static DiscountCodeResponse discount(DiscountCode source) {
        return new DiscountCodeResponse(source.getId(), source.getOrganizationId(), source.getBranchId(), source.getCode(),
                source.getName(), source.getDiscountType(), source.getDiscountValue(), source.getMaxDiscount(),
                source.getMinOrderValue(), source.getCustomerGroup(), source.getValidFrom(), source.getValidTo(), source.getActive());
    }

    public static RentalRequestResponse request(RentalRequest source) {
        List<RentalRequestItemResponse> items = source.getItems().stream()
                .map(item -> new RentalRequestItemResponse(item.getId(), item.getEquipmentTypeId(), item.getQuantity()))
                .toList();
        return new RentalRequestResponse(source.getId(), source.getOrganizationId(), source.getBranchId(),
                source.getRequestCode(), source.getCustomerId(), source.getStartAt(), source.getEndAt(),
                source.getDeliveryAddress(), source.getNote(), source.getStatus(), items, source.getCreatedAt());
    }

    public static QuotationResponse quotation(Quotation source) {
        return new QuotationResponse(source.getId(), source.getOrganizationId(), source.getBranchId(),
                source.getQuotationCode(), source.getRentalRequestId(), source.getCustomerId(), source.getRentalAmount(),
                source.getDepositAmount(), source.getDeliveryFee(), source.getDiscountAmount(), source.getTotalAmount(),
                source.getDiscountCode(), source.getStatus(), source.getValidUntil(), source.getSpecialTerms());
    }

    public static RentalOrderResponse order(RentalOrder source) {
        return new RentalOrderResponse(source.getId(), source.getOrganizationId(), source.getBranchId(), source.getOrderCode(),
                source.getQuotationId(), source.getCustomerId(), source.getStartAt(), source.getEndAt(), source.getTotalAmount(),
                source.getStatus(), source.getReservedUntil(), source.getInventoryReservationId(), source.getCancelReason());
    }

    public static RentalContractResponse contract(RentalContract source) {
        return new RentalContractResponse(source.getId(), source.getOrganizationId(), source.getBranchId(), source.getContractCode(),
                source.getRentalOrderId(), source.getCustomerId(), source.getStartAt(), source.getEndAt(), source.getTotalAmount(),
                source.getStatus(), source.getTerms(), source.getApprovedAt(), source.getSignedAt(), source.getLiquidatedAt(), source.getCancelReason());
    }

    public static ContractAppendixResponse appendix(ContractAppendix source) {
        return new ContractAppendixResponse(source.getId(), source.getOrganizationId(), source.getBranchId(), source.getContractId(),
                source.getAppendixCode(), source.getAppendixType(), source.getStatus(), source.getNewEndAt(), source.getTerms(),
                source.getApprovedAt(), source.getSignedAt());
    }
}
