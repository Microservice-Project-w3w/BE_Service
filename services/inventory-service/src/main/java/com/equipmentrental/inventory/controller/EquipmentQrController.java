package com.equipmentrental.inventory.controller;


import com.equipmentrental.inventory.service.EquipmentQrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/inventory/equipment")
@RequiredArgsConstructor
public class EquipmentQrController {


    private final EquipmentQrService qrService;


    // Tạo QR
    @PostMapping("/{id}/qr")
    public Object createQr(
            @PathVariable Long id
    ){
        return qrService.create(id);
    }



    // Lấy QR
    @GetMapping("/{id}/qr")
    public Object getQr(
            @PathVariable Long id
    ){
        return qrService.get(id);
    }



    // Tra cứu bằng QR
    @GetMapping("/qr/{qrCode}")
    public Object findByQr(
            @PathVariable String qrCode
    ){
        return qrService.findByQr(qrCode);
    }



    // Tạo lại QR
    @PostMapping("/{id}/qr/regenerate")
    public Object regenerate(
            @PathVariable Long id
    ){
        return qrService.regenerate(id);
    }

}