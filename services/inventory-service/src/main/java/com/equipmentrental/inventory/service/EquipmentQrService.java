package com.equipmentrental.inventory.service;


public interface EquipmentQrService {


    Object create(Long equipmentId);


    Object get(Long equipmentId);


    Object findByQr(String qrCode);


    Object regenerate(Long equipmentId);

}