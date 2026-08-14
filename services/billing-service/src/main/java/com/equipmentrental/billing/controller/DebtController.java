package com.equipmentrental.billing.controller;

import com.equipmentrental.billing.entity.Debt;
import com.equipmentrental.billing.service.DebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;

    @PostMapping("/customers/{customerId}/add")
    public ResponseEntity<Debt> addDebt(@PathVariable Long customerId, @RequestParam BigDecimal amount) {
        Debt debt = debtService.addDebt(customerId, amount);
        return ResponseEntity.ok(debt);
    }

    @PostMapping("/customers/{customerId}/reduce")
    public ResponseEntity<Debt> reduceDebt(@PathVariable Long customerId, @RequestParam BigDecimal amount) {
        Debt debt = debtService.reduceDebt(customerId, amount);
        return ResponseEntity.ok(debt);
    }
}
