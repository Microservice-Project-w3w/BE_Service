package com.equipmentrental.organizationcustomer.entity;

import com.equipmentrental.organizationcustomer.enums.CustomerStatus;
import com.equipmentrental.organizationcustomer.enums.CustomerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    private Long branchId;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "customer_code", nullable = false)
    private String customerCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String email;
    private String phone;
    private String address;

    /* Cá nhân */

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "identity_number")
    private String identityNumber;

    /* Doanh nghiệp */

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "representative_phone")
    private String representativePhone;

    @Column(name = "representative_email")
    private String representativeEmail;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    private String note;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}