package com.equipmentrental.logistics.dto.request;

import com.equipmentrental.logistics.entity.enums.PhotoType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandoverPhotoRequest {

    @NotBlank
    private String photoUrl;

    @NotNull
    private PhotoType photoType;

    private Integer sortOrder;

    private String status;
}
