package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.*;
import com.equipmentrental.inventory.dto.response.StockAuditResponse;
import com.equipmentrental.inventory.service.StockAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/inventory/stock-audits"
)
@RequiredArgsConstructor
public class StockAuditController {

    private final StockAuditService service;

    @PostMapping
    public ResponseEntity<StockAuditResponse> create(
            @Valid
            @RequestBody
            CreateStockAuditRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }

    @GetMapping
    public List<StockAuditResponse> findAll(

            @RequestParam(required = false)
            Long organizationId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long warehouseId
    ) {

        return service.findAll(
                organizationId,
                branchId,
                warehouseId
        );
    }

    @GetMapping("/{id}")
    public StockAuditResponse findById(
            @PathVariable Long id
    ) {

        return service.findById(id);
    }

    @PostMapping("/{id}/start")
    public StockAuditResponse start(

            @PathVariable Long id,

            @RequestBody(required = false)
            StartStockAuditRequest request
    ) {

        return service.start(
                id,
                request
        );
    }

    @PostMapping("/{id}/items")
    public StockAuditResponse recordItem(

            @PathVariable Long id,

            @Valid
            @RequestBody
            RecordStockAuditItemRequest request
    ) {

        return service.recordItem(
                id,
                request
        );
    }

    @PostMapping("/{id}/complete")
    public StockAuditResponse complete(

            @PathVariable Long id,

            @RequestBody(required = false)
            CompleteStockAuditRequest request
    ) {

        return service.complete(
                id,
                request
        );
    }

    @PostMapping("/{id}/cancel")
    public StockAuditResponse cancel(
            @PathVariable Long id
    ) {

        return service.cancel(id);
    }
}