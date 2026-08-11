package com.equipmentrental.organizationcustomer.entity;

import com.equipmentrental.organizationcustomer.enums.RestrictionStatus;
import com.equipmentrental.organizationcustomer.enums.RestrictionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "restricted_customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictedCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Doanh nghiệp sở hữu dữ liệu này
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    // Khách hàng bị hạn chế
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Loại hạn chế
    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false)
    private RestrictionType restrictionType;

    // Lý do hạn chế
    @Column(nullable = false, length = 1000)
    private String reason;

    // Trạng thái hạn chế
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestrictionStatus status;

    // Thời điểm bắt đầu có hiệu lực
    @Column(name = "restricted_from", nullable = false)
    private LocalDateTime restrictedFrom;

    // Thời điểm hết hạn
    // null = không xác định ngày hết hạn
    @Column(name = "restricted_until")
    private LocalDateTime restrictedUntil;

    // userId của người thực hiện hạn chế
    // Chỉ tham chiếu sang Identity Service
    @Column(name = "restricted_by_user_id", nullable = false)
    private Long restrictedByUserId;

    // Thời điểm gỡ hạn chế
    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    // userId của người gỡ hạn chế
    @Column(name = "removed_by_user_id")
    private Long removedByUserId;

    // Lý do gỡ hạn chế
    @Column(name = "removed_reason", length = 1000)
    private String removedReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}