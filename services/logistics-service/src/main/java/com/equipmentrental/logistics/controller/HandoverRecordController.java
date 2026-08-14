package com.equipmentrental.logistics.controller;

import com.equipmentrental.logistics.dto.request.CreateHandoverRecordRequest;
import com.equipmentrental.logistics.dto.request.HandoverPhotoRequest;
import com.equipmentrental.logistics.dto.response.HandoverPhotoResponse;
import com.equipmentrental.logistics.dto.response.HandoverRecordResponse;
import com.equipmentrental.logistics.service.HandoverRecordService;
import com.equipmentrental.logistics.dto.response.HandoverChecklistResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/logistics/handover-records")
public class HandoverRecordController {

    private final HandoverRecordService service;

    public HandoverRecordController(HandoverRecordService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('logistics.handover.create')")
    @ResponseStatus(HttpStatus.CREATED)
    public HandoverRecordResponse createHandoverRecord(@Valid @RequestBody CreateHandoverRecordRequest request) {
        return service.createHandoverRecord(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('logistics.handover.read')")
    public HandoverRecordResponse getHandoverRecord(@PathVariable Long id) {
        return service.getHandoverRecord(id);
    }

    @PostMapping("/{handoverRecordId}/photos")
    @PreAuthorize("hasAuthority('logistics.handover.create')")
    @ResponseStatus(HttpStatus.CREATED)
    public HandoverPhotoResponse addPhoto(
            @PathVariable Long handoverRecordId, @Valid @RequestBody HandoverPhotoRequest request) {
        return service.addPhoto(handoverRecordId, request);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('logistics.delivery.confirm')")
    public HandoverRecordResponse confirmHandoverRecord(@PathVariable Long id) {
        return service.confirmHandoverRecord(id);
    }

    @GetMapping("/{id}/photos")
    @PreAuthorize("hasAuthority('logistics.handover.read')")
    public List<HandoverPhotoResponse> getPhotos(@PathVariable Long id) {
        return service.getPhotos(id);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('logistics.handover.read')")
    public List<HandoverRecordResponse> getHandoverRecords() {
        return service.getHandoverRecords();
    }

    @GetMapping("/{id}/checklists")
    @PreAuthorize("hasAuthority('logistics.handover.read')")
    public List<HandoverChecklistResponse> getChecklists(
            @PathVariable Long id
    ) {
        return service.getChecklists(id);
    }
}
