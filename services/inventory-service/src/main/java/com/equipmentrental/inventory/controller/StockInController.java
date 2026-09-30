package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ConfirmStockInRequest;
import com.equipmentrental.inventory.dto.request.CreateStockInRequest;
import com.equipmentrental.inventory.dto.response.StockInResponse;
import com.equipmentrental.inventory.service.StockInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/inventory/stock-in"
)
@RequiredArgsConstructor
public class StockInController {

    private final StockInService service;

    @PostMapping
    public ResponseEntity<StockInResponse> create(
            @Valid
            @RequestBody
            CreateStockInRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }

    @GetMapping
    public List<StockInResponse> findAll(

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
    public StockInResponse findById(
            @PathVariable Long id
    ) {

        return service.findById(id);
    }

    @PostMapping("/{id}/confirm")
    public StockInResponse confirm(

            @PathVariable Long id,

            @RequestBody(required = false)
            ConfirmStockInRequest request
    ) {

        return service.confirm(
                id,
                request
        );
    }

    @PostMapping("/{id}/cancel")
    public StockInResponse cancel(
            @PathVariable Long id
    ) {

        return service.cancel(id);
    }
}