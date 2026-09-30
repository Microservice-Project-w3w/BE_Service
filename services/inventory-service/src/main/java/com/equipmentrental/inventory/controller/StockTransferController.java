package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ApproveStockTransferRequest;
import com.equipmentrental.inventory.dto.request.CreateStockTransferRequest;
import com.equipmentrental.inventory.dto.request.ReceiveStockTransferRequest;
import com.equipmentrental.inventory.dto.response.StockTransferResponse;
import com.equipmentrental.inventory.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/inventory/transfers"
)
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService service;

    @PostMapping
    public ResponseEntity<StockTransferResponse> create(
            @Valid
            @RequestBody
            CreateStockTransferRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }

    @GetMapping
    public List<StockTransferResponse> findAll(

            @RequestParam(required = false)
            Long organizationId,

            @RequestParam(required = false)
            Long branchId
    ) {

        return service.findAll(
                organizationId,
                branchId
        );
    }

    @GetMapping("/{id}")
    public StockTransferResponse findById(
            @PathVariable Long id
    ) {

        return service.findById(id);
    }

    @PostMapping("/{id}/approve")
    public StockTransferResponse approve(

            @PathVariable Long id,

            @RequestBody(required = false)
            ApproveStockTransferRequest request
    ) {

        return service.approve(
                id,
                request
        );
    }

    @PostMapping("/{id}/dispatch")
    public StockTransferResponse dispatch(
            @PathVariable Long id
    ) {

        return service.dispatch(id);
    }

    @PostMapping("/{id}/receive")
    public StockTransferResponse receive(

            @PathVariable Long id,

            @RequestBody(required = false)
            ReceiveStockTransferRequest request
    ) {

        return service.receive(
                id,
                request
        );
    }

    @PostMapping("/{id}/cancel")
    public StockTransferResponse cancel(
            @PathVariable Long id
    ) {

        return service.cancel(id);
    }
}