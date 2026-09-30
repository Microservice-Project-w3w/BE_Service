package com.equipmentrental.inventory.service;


import com.equipmentrental.inventory.dto.request.CreateEquipmentImageRequest;
import com.equipmentrental.inventory.dto.response.EquipmentImageResponse;
import com.equipmentrental.inventory.entity.EquipmentImage;
import com.equipmentrental.inventory.repository.EquipmentImageRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EquipmentImageService {


    private final EquipmentImageRepository repository;



    public EquipmentImageResponse create(
            CreateEquipmentImageRequest request
    ){

        EquipmentImage image =
                EquipmentImage.builder()
                        .equipmentId(request.equipmentId())
                        .imageUrl(request.imageUrl())
                        .primaryImage(request.primaryImage())
                        .displayOrder(request.displayOrder())
                        .build();


        return map(
                repository.save(image)
        );
    }



    public List<EquipmentImageResponse> getByEquipment(
            Long equipmentId
    ){

        return repository
                .findByEquipmentIdOrderByDisplayOrderAsc(
                        equipmentId
                )
                .stream()
                .map(this::map)
                .toList();
    }



    public void delete(Long id){

        repository.deleteById(id);

    }



    private EquipmentImageResponse map(
            EquipmentImage image
    ){

        return new EquipmentImageResponse(
                image.getId(),
                image.getEquipmentId(),
                image.getImageUrl(),
                image.getPrimaryImage(),
                image.getDisplayOrder()
        );
    }
}