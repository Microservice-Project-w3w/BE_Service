package com.equipmentrental.rental.service;

import com.equipmentrental.rental.dto.RentalPriceRequest;
import com.equipmentrental.rental.dto.RentalPriceResponse;
import com.equipmentrental.rental.entity.RentalPrice;
import com.equipmentrental.rental.exception.BadRequestException;
import com.equipmentrental.rental.exception.ResourceNotFoundException;
import com.equipmentrental.rental.repository.RentalPriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RentalPriceService {

    private final RentalPriceRepository rentalPriceRepository;

    public RentalPriceService(RentalPriceRepository rentalPriceRepository) {
        this.rentalPriceRepository = rentalPriceRepository;
    }

    public RentalPriceResponse create(RentalPriceRequest request) {
        validateDate(request);

        rentalPriceRepository
                .findByEquipmentTypeIdAndRentalUnit(
                        request.equipmentTypeId(),
                        request.rentalUnit()
                )
                .ifPresent(existing -> {
                    throw new BadRequestException(
                            "Loại thiết bị này đã có bảng giá theo đơn vị "
                                    + request.rentalUnit()
                    );
                });

        RentalPrice entity = new RentalPrice();
        mapRequestToEntity(request, entity);

        return toResponse(rentalPriceRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<RentalPriceResponse> getAll(Boolean active) {
        List<RentalPrice> result;

        if (Boolean.TRUE.equals(active)) {
            result = rentalPriceRepository
                    .findByActiveTrueOrderByCreatedAtDesc();
        } else {
            result = rentalPriceRepository
                    .findAllByOrderByCreatedAtDesc();
        }

        return result.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RentalPriceResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<RentalPriceResponse> getByEquipmentTypeId(
            Long equipmentTypeId
    ) {
        return rentalPriceRepository
                .findByEquipmentTypeIdOrderByCreatedAtDesc(equipmentTypeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RentalPriceResponse update(
            Long id,
            RentalPriceRequest request
    ) {
        validateDate(request);

        RentalPrice entity = findById(id);

        rentalPriceRepository
                .findByEquipmentTypeIdAndRentalUnit(
                        request.equipmentTypeId(),
                        request.rentalUnit()
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException(
                            "Bảng giá cùng loại thiết bị và đơn vị đã tồn tại"
                    );
                });

        mapRequestToEntity(request, entity);

        return toResponse(rentalPriceRepository.save(entity));
    }

    public RentalPriceResponse updateStatus(
            Long id,
            boolean active
    ) {
        RentalPrice entity = findById(id);
        entity.setActive(active);

        return toResponse(rentalPriceRepository.save(entity));
    }

    public void delete(Long id) {
        RentalPrice entity = findById(id);
        rentalPriceRepository.delete(entity);
    }

    private RentalPrice findById(Long id) {
        return rentalPriceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy bảng giá có id = " + id
                        )
                );
    }

    private void validateDate(RentalPriceRequest request) {
        if (request.validTo() != null
                && request.validTo().isBefore(request.validFrom())) {
            throw new BadRequestException(
                    "Ngày kết thúc không được trước ngày bắt đầu"
            );
        }
    }

    private void mapRequestToEntity(
            RentalPriceRequest request,
            RentalPrice entity
    ) {
        entity.setPriceName(request.priceName().trim());
        entity.setEquipmentTypeId(request.equipmentTypeId());
        entity.setRentalUnit(request.rentalUnit());
        entity.setRentalPrice(request.rentalPrice());
        entity.setDepositAmount(request.depositAmount());
        entity.setLateFee(request.lateFee());
        entity.setValidFrom(request.validFrom());
        entity.setValidTo(request.validTo());
        entity.setDescription(request.description());

        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }

    private RentalPriceResponse toResponse(RentalPrice entity) {
        return new RentalPriceResponse(
                entity.getId(),
                entity.getPriceName(),
                entity.getEquipmentTypeId(),
                entity.getRentalUnit(),
                entity.getRentalPrice(),
                entity.getDepositAmount(),
                entity.getLateFee(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getActive(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}