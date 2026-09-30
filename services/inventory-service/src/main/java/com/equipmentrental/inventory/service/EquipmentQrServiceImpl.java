package com.equipmentrental.inventory.service;


import org.springframework.stereotype.Service;


@Service
public class EquipmentQrServiceImpl implements EquipmentQrService {


    @Override
    public Object create(Long equipmentId) {
        return "create qr " + equipmentId;
    }


    @Override
    public Object get(Long equipmentId) {
        return "get qr " + equipmentId;
    }


    @Override
    public Object findByQr(String qrCode) {
        return "find qr " + qrCode;
    }


    @Override
    public Object regenerate(Long equipmentId) {
        return "regenerate qr " + equipmentId;
    }

}