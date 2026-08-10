package com.equipmentrental.logistics.controller;

import com.equipmentrental.logistics.dto.request.CreateHandoverRecordRequest;
import com.equipmentrental.logistics.dto.request.HandoverPhotoRequest;
import com.equipmentrental.logistics.dto.response.HandoverPhotoResponse;
import com.equipmentrental.logistics.dto.response.HandoverRecordResponse;
import com.equipmentrental.logistics.service.HandoverRecordService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logistics/handover-records")
public class HandoverRecordController {

    private final HandoverRecordService service;

    public HandoverRecordController(HandoverRecordService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HandoverRecordResponse createHandoverRecord(@Valid @RequestBody CreateHandoverRecordRequest request) {
        return service.createHandoverRecord(request);
    }

    @GetMapping("/{id}")
    public HandoverRecordResponse getHandoverRecord(@PathVariable Long id) {
        return service.getHandoverRecord(id);
    }

    @PostMapping("/{handoverRecordId}/photos")
    @ResponseStatus(HttpStatus.CREATED)
    public HandoverPhotoResponse addPhoto(
            @PathVariable Long handoverRecordId, @Valid @RequestBody HandoverPhotoRequest request) {
        return service.addPhoto(handoverRecordId, request);
    }

    @PostMapping("/{id}/confirm")
    public HandoverRecordResponse confirmHandoverRecord(@PathVariable Long id) {
        return service.confirmHandoverRecord(id);
    }

    @GetMapping("/{id}/photos")
    public List<HandoverPhotoResponse> getPhotos(@PathVariable Long id) {
        return service.getPhotos(id);
    }

    @GetMapping
    public List<HandoverRecordResponse> getHandoverRecords() {
        return service.getHandoverRecords();
    }
}
