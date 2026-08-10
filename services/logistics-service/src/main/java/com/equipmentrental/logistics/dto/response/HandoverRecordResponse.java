package com.equipmentrental.logistics.dto.response;

import com.equipmentrental.logistics.entity.enums.HandoverStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class HandoverRecordResponse {
    private Long id;
    private Long dispatchNoteId;
    private LocalDateTime handoverTime;
    private String receiverName;
    private String receiverPhone;
    private String customerSignatureUrl;
    private String notes;
    private HandoverStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<HandoverChecklistResponse> checklists;
    private List<HandoverPhotoResponse> photos;
}
