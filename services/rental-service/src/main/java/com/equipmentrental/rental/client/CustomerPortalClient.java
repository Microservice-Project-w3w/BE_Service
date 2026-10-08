package com.equipmentrental.rental.client;

import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class CustomerPortalClient {
    private final RestClient client;
    public record Context(Long userId, Long customerId, Long organizationId, Long branchId, String displayName) {}

    public CustomerPortalClient(@Value("${app.integration.organization-customer-base-url:http://localhost:8082}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public Context me(Long organizationId) {
        try {
            var context = client.get().uri("/api/v1/organizations/{id}/customers/me", organizationId)
                    .headers(headers -> {
                        var auth = SecurityContextHolder.getContext().getAuthentication();
                        if (!(auth instanceof JwtAuthenticationToken jwt))
                            throw new BusinessException(CommonErrorCode.AUTH_UNAUTHENTICATED);
                        headers.setBearerAuth(jwt.getToken().getTokenValue());
                    }).retrieve().body(Context.class);
            if (context == null || context.customerId() == null || context.userId() == null
                    || context.branchId() == null || !organizationId.equals(context.organizationId()))
                throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
            return context;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) throw new BusinessException(CommonErrorCode.AUTH_DATA_SCOPE_DENIED);
            throw new BusinessException(CommonErrorCode.INTEGRATION_SERVICE_UNAVAILABLE);
        } catch (RestClientException ex) { throw new BusinessException(CommonErrorCode.INTEGRATION_SERVICE_UNAVAILABLE); }
    }
}
