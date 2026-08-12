package com.equipmentrental.logistics.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CreateHandoverRecordRequest {

    @NotNull
    private Long dispatchNoteId;

    @NotNull
    private LocalDateTime handoverTime;

    @NotBlank
    private String receiverName;

    @NotBlank
    private String receiverPhone;

    private String customerSignatureUrl;

    private String notes;

    @Valid
    private List<HandoverChecklistRequest> checklists;

    @Valid
    private List<HandoverPhotoRequest> photos;
}
