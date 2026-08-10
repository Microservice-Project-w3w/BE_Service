package com.equipmentrental.logistics.dto.response;

import com.equipmentrental.logistics.entity.enums.PhotoType;
import lombok.Data;

@Data
public class HandoverPhotoResponse {

    private Long id;

    private String photoUrl;

    private PhotoType photoType;

    private Integer sortOrder;

    private String status;
}
