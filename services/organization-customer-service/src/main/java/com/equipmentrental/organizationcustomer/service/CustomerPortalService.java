package com.equipmentrental.organizationcustomer.service;

import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import com.equipmentrental.organizationcustomer.entity.Customer;
import com.equipmentrental.organizationcustomer.entity.CustomerPortalAccount;
import com.equipmentrental.organizationcustomer.enums.CustomerStatus;
import com.equipmentrental.organizationcustomer.exception.BadRequestException;
import com.equipmentrental.organizationcustomer.exception.ConflictException;
import com.equipmentrental.organizationcustomer.exception.NotFoundException;
import com.equipmentrental.organizationcustomer.repository.CustomerPortalAccountRepository;
import com.equipmentrental.organizationcustomer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@Transactional
public class CustomerPortalService {
    private final CustomerPortalAccountRepository links;
    private final CustomerRepository customers;
    private final CurrentUserProvider users;
    private final DataScopeAuthorizer scope;
    private final RestClient identity;

    public CustomerPortalService(CustomerPortalAccountRepository links, CustomerRepository customers,
            CurrentUserProvider users, DataScopeAuthorizer scope,
            @Value("${app.integration.identity-base-url:http://localhost:8081}") String identityUrl) {
        this.links = links; this.customers = customers; this.users = users; this.scope = scope;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
        this.identity = RestClient.builder().baseUrl(identityUrl).requestFactory(factory).build();
    }

    public record Context(Long userId, Long customerId, Long organizationId, Long branchId, String displayName) {}

    @Transactional(readOnly = true)
    public Context me(Long organizationId) {
        var user = users.getCurrentUser();
        if (!user.roles().contains("CUSTOMER") && !user.roles().contains("ROLE_CUSTOMER")) throw denied();
        Long userId;
        try { userId = Long.valueOf(user.userId()); } catch (RuntimeException ex) { throw denied(); }
        var link = links.findById(userId).orElseThrow(() -> new BusinessException(
                CommonErrorCode.AUTH_DATA_SCOPE_DENIED,
                "Tài khoản chưa được liên kết với hồ sơ khách hàng. Liên hệ Admin để cấp quyền thuê."));
        var customer = customers.findByIdAndOrganizationIdAndDeletedAtIsNull(link.getCustomerId(), organizationId)
                .orElseThrow(this::denied);
        if (customer.getStatus() != CustomerStatus.ACTIVE
                || !scope.canAccessBranch(user, organizationId, customer.getBranchId())) throw denied();
        return context(userId, customer);
    }

    public Context bind(Long organizationId, Long customerId, Long userId) {
        var customer = customers.findByIdAndOrganizationIdAndDeletedAtIsNull(customerId, organizationId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng"));
        if (!scope.canAccessBranch(users.getCurrentUser(), organizationId, customer.getBranchId())) throw denied();
        if (customer.getStatus() != CustomerStatus.ACTIVE || customer.getBranchId() == null)
            throw new BadRequestException("Khách hàng phải hoạt động và thuộc một chi nhánh");
        validateAccount(userId, organizationId, customer.getBranchId());
        var existingUser = links.findById(userId);
        if (existingUser.isPresent() && !customerId.equals(existingUser.get().getCustomerId()))
            throw new ConflictException("Tài khoản đã liên kết với khách hàng khác");
        var existingCustomer = links.findByCustomerId(customerId);
        if (existingCustomer.isPresent() && !userId.equals(existingCustomer.get().getUserId()))
            throw new ConflictException("Khách hàng đã có tài khoản. Gỡ liên kết cũ trước khi thay đổi.");
        links.save(new CustomerPortalAccount(userId, customerId));
        return context(userId, customer);
    }

    public void unbind(Long organizationId, Long customerId) {
        var customer = customers.findByIdAndOrganizationIdAndDeletedAtIsNull(customerId, organizationId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng"));
        if (!scope.canAccessBranch(users.getCurrentUser(), organizationId, customer.getBranchId())) throw denied();
        links.findByCustomerId(customerId).ifPresent(links::delete);
    }

    private void validateAccount(Long userId, Long organizationId, Long branchId) {
        JsonNode response;
        try {
            response = identity.get().uri("/api/v1/users/{id}", userId)
                    .headers(headers -> {
                        var auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth instanceof JwtAuthenticationToken jwt) headers.setBearerAuth(jwt.getToken().getTokenValue());
                    }).retrieve().body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) throw new BadRequestException("Không thể đọc tài khoản cần liên kết");
            throw new BusinessException(CommonErrorCode.INTEGRATION_SERVICE_UNAVAILABLE);
        } catch (RestClientException ex) { throw new BusinessException(CommonErrorCode.INTEGRATION_SERVICE_UNAVAILABLE); }
        JsonNode account = response == null ? null : response.get("data");
        boolean hasBranch = false;
        if (account != null) for (var id : account.path("branchIds")) if (id.asLong() == branchId) hasBranch = true;
        if (account == null || account.path("id").asLong() != userId
                || !"CUSTOMER".equals(account.path("roleCode").asText())
                || !"ACTIVE".equals(account.path("status").asText())
                || account.path("organizationId").asLong() != organizationId || !hasBranch)
            throw new BadRequestException("Chọn tài khoản CUSTOMER hoạt động, cùng organization và được cấp chi nhánh này");
    }

    private Context context(Long userId, Customer c) {
        return new Context(userId, c.getId(), c.getOrganizationId(), c.getBranchId(), c.getDisplayName());
    }
    private BusinessException denied() { return new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED); }
}
