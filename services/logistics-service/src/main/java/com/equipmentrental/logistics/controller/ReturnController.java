package com.equipmentrental.logistics.controller;

import com.equipmentrental.logistics.dto.request.CreateReturnInspectionRequest;
import com.equipmentrental.logistics.dto.request.CreateReturnRecordRequest;
import com.equipmentrental.logistics.dto.request.CreateReturnRequestRequest;
import com.equipmentrental.logistics.dto.response.ReturnInspectionResponse;
import com.equipmentrental.logistics.dto.response.ReturnRecordResponse;
import com.equipmentrental.logistics.dto.response.ReturnRequestResponse;
import com.equipmentrental.logistics.service.ReturnService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logistics/returns")
public class ReturnController {
    private final ReturnService service;

    public ReturnController(ReturnService service) {
        this.service = service;
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnRequestResponse createReturnRequest(@Valid @RequestBody CreateReturnRequestRequest request) {
        return service.createReturnRequest(request);
    }

    @PostMapping("/records")
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnRecordResponse createReturnRecord(@Valid @RequestBody CreateReturnRecordRequest request) {
        return service.createReturnRecord(request);
    }

    @GetMapping("/records/{id}")
    public ReturnRecordResponse getReturnRecord(@PathVariable Long id) {
        return service.getReturnRecord(id);
    }

    @PostMapping("/inspections")
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnInspectionResponse createInspection(@Valid @RequestBody CreateReturnInspectionRequest request) {
        return service.createInspection(request);
    }

    @GetMapping("/requests")
    public List<ReturnRequestResponse> getReturnRequests() {
        return service.getReturnRequests();
    }

    @GetMapping("/requests/{id}")
    public ReturnRequestResponse getReturnRequest(@PathVariable Long id) {
        return service.getReturnRequest(id);
    }

    @GetMapping("/inspections")
    public List<ReturnInspectionResponse> getInspections() {
        return service.getInspections();
    }

    @GetMapping("/inspections/{id}")
    public ReturnInspectionResponse getInspection(@PathVariable Long id) {
        return service.getInspection(id);
    }

    @GetMapping("/records")
    public List<ReturnRecordResponse> getReturnRecords() {
        return service.getReturnRecords();
    }
}
