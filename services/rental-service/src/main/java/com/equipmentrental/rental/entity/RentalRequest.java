package com.equipmentrental.rental.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_requests")
public class RentalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String requestCode;
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private LocalDateTime startAt;
    @Column(nullable = false)
    private LocalDateTime endAt;
    @Column(length = 500)
    private String deliveryAddress;
    @Column(length = 1000)
    private String note;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestStatus status = RequestStatus.SUBMITTED;
    @OneToMany(mappedBy = "rentalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalRequestItem> items = new ArrayList<>();
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void pre() {
        createdAt = LocalDateTime.now();
    }

    public void addItem(RentalRequestItem item) {
        item.setRentalRequest(this);
        items.add(item);
    }

    public Long getId() {
        return id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String v) {
        requestCode = v;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long v) {
        customerId = v;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime v) {
        startAt = v;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime v) {
        endAt = v;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String v) {
        deliveryAddress = v;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String v) {
        note = v;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus v) {
        status = v;
    }

    public List<RentalRequestItem> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
