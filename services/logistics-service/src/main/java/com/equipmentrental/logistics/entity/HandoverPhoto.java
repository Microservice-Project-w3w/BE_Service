package com.equipmentrental.logistics.entity;

import com.equipmentrental.logistics.entity.enums.PhotoType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "handover_photos")
public class HandoverPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "handover_record_id", nullable = false)
    private Long handoverRecordId;

    // Chỉ lưu URL hoặc object key
    @Column(name = "photo_url", nullable = false, length = 1000)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_type", nullable = false, length = 30)
    private PhotoType photoType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (sortOrder == null) {
            sortOrder = 0;
        }

        if (status == null) {
            status = "ACTIVE";
        }
    }

    public Long getId() {
        return id;
    }

    public Long getHandoverRecordId() {
        return handoverRecordId;
    }

    public void setHandoverRecordId(Long handoverRecordId) {
        this.handoverRecordId = handoverRecordId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public PhotoType getPhotoType() {
        return photoType;
    }

    public void setPhotoType(PhotoType photoType) {
        this.photoType = photoType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
