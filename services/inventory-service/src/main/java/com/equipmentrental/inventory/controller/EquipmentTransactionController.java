package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.response.EquipmentTransactionResponse;
import com.equipmentrental.inventory.service.EquipmentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/inventory/equipment"
)
@RequiredArgsConstructor
public class EquipmentTransactionController {

    private final EquipmentTransactionService service;

    @GetMapping("/{id}/transactions")
    public List<EquipmentTransactionResponse> getHistory(
            @PathVariable Long id
    ) {

        return service.getHistory(id);
    }
}