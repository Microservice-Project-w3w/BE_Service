package com.equipmentrental.rental.service;

import com.equipmentrental.rental.dto.*;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PricingService {
    private final RentalPriceRepository prices;
    private final DeliveryFeeRuleRepository fees;
    private final DiscountCodeRepository discounts;

    public PricingService(RentalPriceRepository p, DeliveryFeeRuleRepository f, DiscountCodeRepository d) {
        prices = p;
        fees = f;
        discounts = d;
    }

    public RentalPrice createPrice(RentalPriceRequest r) {
        if (r.validTo() != null && r.validTo().isBefore(r.validFrom()))
            throw new ApiException("validTo phải sau validFrom");
        RentalPrice e = new RentalPrice();
        e.setPriceName(r.priceName());
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
        return prices.save(e);
    }

    public List<RentalPrice> getPrices() {
        return prices.findAll();
    }

    public RentalPrice updatePrice(Long id, RentalPriceRequest r) {
        RentalPrice e = prices.findById(id).orElseThrow(() -> new ApiException("Không tìm thấy bảng giá"));
        e.setPriceName(r.priceName());
        e.setEquipmentTypeId(r.equipmentTypeId());
        e.setRentalUnit(r.rentalUnit());
        e.setRentalPrice(r.rentalPrice());
        e.setDepositType(r.depositType());
        e.setDepositValue(r.depositValue());
        e.setLateFee(r.lateFee());
        e.setValidFrom(r.validFrom());
        e.setValidTo(r.validTo());
        if (r.active() != null)
            e.setActive(r.active());
        e.setDescription(r.description());
        return prices.save(e);
    }

    public DeliveryFeeRule createFee(DeliveryFeeRuleRequest r) {
        DeliveryFeeRule e = new DeliveryFeeRule();
        e.setName(r.name());
        e.setArea(r.area());
        e.setBaseFee(r.baseFee());
        e.setFeePerKm(r.feePerKm());
        e.setEquipmentTypeId(r.equipmentTypeId());
        e.setActive(r.active() == null ? true : r.active());
        return fees.save(e);
    }

    public List<DeliveryFeeRule> getFees() {
        return fees.findAll();
    }

    public DiscountCode createDiscount(DiscountCodeRequest r) {
        if (r.validTo().isBefore(r.validFrom()))
            throw new ApiException("Ngày giảm giá không hợp lệ");
        DiscountCode e = new DiscountCode();
        e.setCode(r.code().toUpperCase());
        e.setName(r.name());
        e.setDiscountType(r.discountType());
        e.setDiscountValue(r.discountValue());
        e.setMaxDiscount(r.maxDiscount());
        e.setMinOrderValue(r.minOrderValue());
        e.setCustomerGroup(r.customerGroup());
        e.setValidFrom(r.validFrom());
        e.setValidTo(r.validTo());
        e.setActive(r.active() == null ? true : r.active());
        return discounts.save(e);
    }

    public List<DiscountCode> getDiscounts() {
        return discounts.findAll();
    }

    public BigDecimal calculateDiscount(String code, BigDecimal amount) {
        if (code == null || code.isBlank())
            return BigDecimal.ZERO;
        DiscountCode d = discounts.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ApiException("Mã giảm giá không tồn tại"));
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
