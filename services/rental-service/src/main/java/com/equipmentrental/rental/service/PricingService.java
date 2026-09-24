package com.equipmentrental.rental.service;

import com.equipmentrental.rental.dto.request.DiscountCodeRequest;
import com.equipmentrental.rental.dto.request.RentalPriceRequest;
import com.equipmentrental.rental.dto.response.DiscountCodeResponse;
import com.equipmentrental.rental.dto.response.RentalPriceResponse;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.mapper.RentalResponseMapper;
import com.equipmentrental.rental.repository.*;
import com.equipmentrental.rental.security.RentalDataScopeGuard;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PricingService {
    private final RentalPriceRepository prices;
    private final DiscountCodeRepository discounts;
    private final RentalDataScopeGuard dataScopeGuard;

    public PricingService(RentalPriceRepository p, DiscountCodeRepository d, RentalDataScopeGuard dataScopeGuard) {
        prices = p;
        discounts = d;
        this.dataScopeGuard = dataScopeGuard;
    }

    public RentalPriceResponse createPrice(RentalPriceRequest r) {
        if (r.validTo() != null && r.validTo().isBefore(r.validFrom()))
            throw new ApiException("validTo phải sau validFrom");
        validateDeposit(r.depositType(), r.depositValue());
        dataScopeGuard.requireBranch(r.organizationId(), r.branchId());
        RentalPrice e = new RentalPrice();
        e.setPriceName(r.priceName());
        e.setOrganizationId(r.organizationId());
        e.setBranchId(r.branchId());
        e.setEquipmentTypeId(r.equipmentTypeId());
        e.setRentalUnit(r.rentalUnit());
        e.setRentalPrice(r.rentalPrice());
        e.setDepositType(r.depositType());
        e.setDepositValue(r.depositValue());
        e.setLateFee(r.lateFee());
        e.setValidFrom(r.validFrom());
        e.setValidTo(r.validTo());
        e.setActive(r.active() == null ? true : r.active());
        e.setDescription(r.description());
        return RentalResponseMapper.price(prices.save(e));
    }

    @Transactional(readOnly = true)
    public List<RentalPriceResponse> getPrices(Long organizationId, Long branchId) {
        dataScopeGuard.requireBranch(organizationId, branchId);
        return prices.findByOrganizationIdAndBranchId(organizationId, branchId).stream()
                .map(RentalResponseMapper::price)
                .toList();
    }

    public RentalPriceResponse updatePrice(Long id, RentalPriceRequest r) {
        if (r.validTo() != null && r.validTo().isBefore(r.validFrom()))
            throw new ApiException("validTo phải sau validFrom");
        validateDeposit(r.depositType(), r.depositValue());
        RentalPrice e = prices.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy bảng giá"));
        dataScopeGuard.requireBranch(e.getOrganizationId(), e.getBranchId());
        if (!e.getOrganizationId().equals(r.organizationId())
                || !e.getBranchId().equals(r.branchId())) {
            throw new ApiException("Không được thay đổi organizationId hoặc branchId của bảng giá");
        }
        e.setPriceName(r.priceName());
        e.setOrganizationId(r.organizationId());
        e.setBranchId(r.branchId());
        e.setEquipmentTypeId(r.equipmentTypeId());
        e.setRentalUnit(r.rentalUnit());
        e.setRentalPrice(r.rentalPrice());
        e.setDepositType(r.depositType());
        e.setDepositValue(r.depositValue());
        e.setLateFee(r.lateFee());
        e.setValidFrom(r.validFrom());
        e.setValidTo(r.validTo());
        if (r.active() != null) e.setActive(r.active());
        e.setDescription(r.description());
        return RentalResponseMapper.price(prices.save(e));
    }

    private void validateDeposit(DepositType depositType, BigDecimal depositValue) {
        if (depositType == DepositType.PERCENT && depositValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException("Tiền cọc theo phần trăm phải nằm trong khoảng từ 0 đến 100");
        }
    }

    public DiscountCodeResponse createDiscount(DiscountCodeRequest r) {
        if (r.validTo().isBefore(r.validFrom())) throw new ApiException("Ngày giảm giá không hợp lệ");
        dataScopeGuard.requireBranch(r.organizationId(), r.branchId());
        DiscountCode e = new DiscountCode();
        e.setCode(r.code().toUpperCase());
        e.setName(r.name());
        e.setOrganizationId(r.organizationId());
        e.setBranchId(r.branchId());
        e.setDiscountType(r.discountType());
        e.setDiscountValue(r.discountValue());
        e.setMaxDiscount(r.maxDiscount());
        e.setMinOrderValue(r.minOrderValue());
        e.setCustomerGroup(r.customerGroup());
        e.setValidFrom(r.validFrom());
        e.setValidTo(r.validTo());
        e.setActive(r.active() == null ? true : r.active());
        return RentalResponseMapper.discount(discounts.save(e));
    }

    public DiscountCodeResponse updateDiscount(Long id, DiscountCodeRequest r) {
        if (r.validTo().isBefore(r.validFrom())) throw new ApiException("Ngày giảm giá không hợp lệ");
        DiscountCode discount =
                discounts.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy mã giảm giá"));
        dataScopeGuard.requireBranch(discount.getOrganizationId(), discount.getBranchId());
        if (!discount.getOrganizationId().equals(r.organizationId())
                || !discount.getBranchId().equals(r.branchId())) {
            throw new ApiException("Không được thay đổi organizationId hoặc branchId của mã giảm giá");
        }
        discount.setCode(r.code().toUpperCase());
        discount.setName(r.name());
        discount.setDiscountType(r.discountType());
        discount.setDiscountValue(r.discountValue());
        discount.setMaxDiscount(r.maxDiscount());
        discount.setMinOrderValue(r.minOrderValue());
        discount.setCustomerGroup(r.customerGroup());
        discount.setValidFrom(r.validFrom());
        discount.setValidTo(r.validTo());
        if (r.active() != null) discount.setActive(r.active());
        return RentalResponseMapper.discount(discounts.save(discount));
    }

    public DiscountCodeResponse setDiscountActive(Long id, boolean active) {
        DiscountCode discount =
                discounts.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy mã giảm giá"));
        dataScopeGuard.requireBranch(discount.getOrganizationId(), discount.getBranchId());
        discount.setActive(active);
        return RentalResponseMapper.discount(discounts.save(discount));
    }

    public void deleteDiscount(Long id) {
        DiscountCode discount =
                discounts.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy mã giảm giá"));
        dataScopeGuard.requireBranch(discount.getOrganizationId(), discount.getBranchId());
        discounts.delete(discount);
    }

    @Transactional(readOnly = true)
    public List<DiscountCodeResponse> getDiscounts(Long organizationId, Long branchId) {
        dataScopeGuard.requireBranch(organizationId, branchId);
        return discounts.findByOrganizationIdAndBranchId(organizationId, branchId).stream()
                .map(RentalResponseMapper::discount)
                .toList();
    }

    public BigDecimal calculateDiscount(String code, BigDecimal amount, Long organizationId, Long branchId) {
        if (code == null || code.isBlank()) return BigDecimal.ZERO;
        dataScopeGuard.requireBranch(organizationId, branchId);
        DiscountCode d = discounts
                .findByOrganizationIdAndBranchIdAndCodeIgnoreCase(organizationId, branchId, code)
                .orElseThrow(() -> ApiException.notFound("Mã giảm giá không tồn tại"));
        LocalDateTime now = LocalDateTime.now();
        if (!d.getActive() || now.isBefore(d.getValidFrom()) || now.isAfter(d.getValidTo()))
            throw new ApiException("Mã giảm giá không còn hiệu lực");
        if (d.getMinOrderValue() != null && amount.compareTo(d.getMinOrderValue()) < 0)
            throw new ApiException("Chưa đạt giá trị đơn tối thiểu");
        BigDecimal value = d.getDiscountType() == DiscountType.PERCENT
                ? amount.multiply(d.getDiscountValue()).divide(BigDecimal.valueOf(100))
                : d.getDiscountValue();
        return d.getMaxDiscount() != null && value.compareTo(d.getMaxDiscount()) > 0 ? d.getMaxDiscount() : value;
    }
}
