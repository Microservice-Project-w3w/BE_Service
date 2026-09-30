package com.equipmentrental.rental.client;

import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import com.equipmentrental.rental.entity.RentalOrder;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class InventoryClient {
    private final RestClient restClient;

    public InventoryClient(@Value("${app.integration.inventory-base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public JsonNode availability(
            Long organizationId,
            Long branchId,
            Long equipmentTypeId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer quantity) {
        try {
            JsonNode response = restClient
                    .get()
                    .uri(builder -> builder.path("/internal/equipment/availability")
                            .queryParam("organizationId", organizationId)
                            .queryParam("branchId", branchId)
                            .queryParam("equipmentTypeId", equipmentTypeId)
                            .queryParam("startAt", startAt)
                            .queryParam("endAt", endAt)
                            .queryParam("quantity", quantity)
                            .build())
                    .headers(this::forwardBearerToken)
                    .retrieve()
                    .body(JsonNode.class);
            return data(response);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    public String createReservation(RentalOrder order, LocalDateTime reservedUntil, List<Long> equipmentIds) {
        try {
            JsonNode response = restClient
                    .post().uri("/internal/reservations")
                    .headers(this::forwardBearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReservationRequest(
                            order.getOrderCode(),
                            order.getOrganizationId(),
                            order.getBranchId(),
                            order.getId(),
                            order.getStartAt(),
                            order.getEndAt(),
                            reservedUntil,
                            equipmentIds.stream().map(ReservationItem::new).toList()))
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode data = data(response);
            JsonNode id = data.path("reservationId");
            if (id.isMissingNode() || id.asText().isBlank()) id = data.path("id");
            if (id.isMissingNode() || id.asText().isBlank()) {
                throw new BusinessException(
                        CommonErrorCode.INTEGRATION_SERVICE_UNAVAILABLE, "Inventory không trả reservationId");
            }
            return id.asText();
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    public void confirmReservation(String reservationId) {
        postWithoutBody("/internal/reservations/" + reservationId + "/confirm");
    }

    public void releaseReservation(String reservationId, String reason) {
        try {
            restClient.post().uri("/internal/reservations/" + reservationId + "/release")
                    .headers(this::forwardBearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReleaseReservationRequest(reason, currentUserId()))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private void postWithoutBody(String path) {
        try {
            restClient.post().uri(path).headers(this::forwardBearerToken).retrieve().toBodilessEntity();
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private JsonNode data(JsonNode response) {
        if (response == null)
            throw new BusinessException(CommonErrorCode.INTEGRATION_SERVICE_UNAVAILABLE, "Inventory không trả dữ liệu");
        return response.has("data") ? response.path("data") : response;
    }

    private BusinessException unavailable(Exception exception) {
        return new BusinessException(
                CommonErrorCode.INTEGRATION_SERVICE_UNAVAILABLE,
                "Không thể kết nối inventory-service: " + exception.getMessage());
    }

    private void forwardBearerToken(HttpHeaders headers) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            headers.setBearerAuth(jwtAuthentication.getToken().getTokenValue());
        }
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            try {
                return Long.valueOf(jwtAuthentication.getToken().getSubject());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private record ReservationRequest(
            String requestReference,
            Long organizationId,
            Long branchId,
            Long rentalOrderId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime expiresAt,
            List<ReservationItem> items) {}

    private record ReservationItem(Long equipmentId) {}
    private record ReleaseReservationRequest(String reason, Long actorUserId) {}
}
