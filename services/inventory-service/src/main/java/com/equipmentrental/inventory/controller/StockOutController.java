package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ConfirmStockOutRequest;
import com.equipmentrental.inventory.dto.request.CreateStockOutRequest;
import com.equipmentrental.inventory.dto.response.StockOutResponse;
import com.equipmentrental.inventory.service.StockOutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/inventory/stock-out"
)
@RequiredArgsConstructor
public class StockOutController {

    private final StockOutService service;

    @PostMapping
    public ResponseEntity<StockOutResponse> create(
            @Valid
            @RequestBody
            CreateStockOutRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }

    @GetMapping
    public List<StockOutResponse> findAll(

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
    public StockOutResponse findById(
            @PathVariable Long id
    ) {

        return service.findById(id);
    }

    @PostMapping("/{id}/confirm")
    public StockOutResponse confirm(

            @PathVariable Long id,

            @RequestBody(required = false)
            ConfirmStockOutRequest request
    ) {

        return service.confirm(
                id,
                request
        );
    }

    @PostMapping("/{id}/cancel")
    public StockOutResponse cancel(
            @PathVariable Long id
    ) {

        return service.cancel(id);
    }
}