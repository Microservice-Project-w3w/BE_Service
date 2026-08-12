package com.equipmentrental.logistics.dto.request;

import com.equipmentrental.logistics.entity.enums.DispatchStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDispatchNoteStatusRequest {

    @NotNull
    private DispatchStatus status;
}