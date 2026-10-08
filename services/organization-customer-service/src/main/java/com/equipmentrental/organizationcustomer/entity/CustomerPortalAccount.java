package com.equipmentrental.organizationcustomer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_portal_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CustomerPortalAccount {
    @Id
    private Long userId;
    @Column(nullable = false, unique = true)
    private Long customerId;
}
