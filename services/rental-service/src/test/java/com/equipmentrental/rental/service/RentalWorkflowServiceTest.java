package com.equipmentrental.rental.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.equipmentrental.rental.client.InventoryClient;
import com.equipmentrental.rental.dto.request.RentalPriceRequest;
import com.equipmentrental.rental.entity.Quotation;
import com.equipmentrental.rental.entity.QuotationStatus;
import com.equipmentrental.rental.entity.DepositType;
import com.equipmentrental.rental.entity.RentalUnit;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.repository.QuotationRepository;
import com.equipmentrental.rental.repository.RentalOrderRepository;
import com.equipmentrental.rental.repository.RentalRequestRepository;
import com.equipmentrental.rental.repository.RentalPriceRepository;
import com.equipmentrental.rental.repository.DiscountCodeRepository;
import com.equipmentrental.rental.security.RentalDataScopeGuard;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class RentalWorkflowServiceTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customerCannotAcceptQuotationBeforeManagerApproval() {
        Quotation quotation = new Quotation();
        quotation.setOrganizationId(1L);
        quotation.setBranchId(2L);
        quotation.setCustomerId(3L);
        quotation.setStatus(QuotationStatus.SENT);

        RentalRequestRepository requests = repository(RentalRequestRepository.class, null);
        QuotationRepository quotations = repository(QuotationRepository.class, quotation);
        RentalOrderRepository orders = repository(RentalOrderRepository.class, null);
        RentalDataScopeGuard guard = new RentalDataScopeGuard(new CurrentUserProvider(), new DataScopeAuthorizer());
        PricingService pricing = new PricingService(
                repository(RentalPriceRepository.class, null),
                repository(DiscountCodeRepository.class, null),
                guard);
        InventoryClient inventory = new InventoryClient("http://localhost");
        RentalWorkflowService service =
                new RentalWorkflowService(requests, quotations, orders, pricing, guard, inventory);
        authenticateManager();

        assertThatThrownBy(() -> service.acceptQuotation(10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("phê duyệt");
    }

    @Test
    void percentageDepositCannotExceedOneHundred() {
        RentalDataScopeGuard guard = new RentalDataScopeGuard(new CurrentUserProvider(), new DataScopeAuthorizer());
        PricingService service = new PricingService(
                repository(RentalPriceRepository.class, null),
                repository(DiscountCodeRepository.class, null),
                guard);
        authenticateManager();

        RentalPriceRequest request = new RentalPriceRequest(
                "Giá ngày",
                1L,
                2L,
                3L,
                RentalUnit.DAY,
                BigDecimal.valueOf(100),
                DepositType.PERCENT,
                BigDecimal.valueOf(100.01),
                BigDecimal.ZERO,
                LocalDateTime.now(),
                null,
                true,
                null);

        assertThatThrownBy(() -> service.createPrice(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("0 đến 100");
    }

    @SuppressWarnings("unchecked")
    private <T> T repository(Class<T> type, Object findByIdValue) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
            if ("findById".equals(method.getName())) {
                return java.util.Optional.ofNullable(findByIdValue);
            }
            if (method.getReturnType().equals(boolean.class)) {
                return false;
            }
            if (method.getReturnType().equals(int.class)) {
                return 0;
            }
            if (method.getReturnType().equals(long.class)) {
                return 0L;
            }
            return null;
        });
    }

    private void authenticateManager() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("4")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("organizationId", 1L)
                .claim("branchIds", Set.of(2L))
                .claim("roles", Set.of("MANAGER"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
