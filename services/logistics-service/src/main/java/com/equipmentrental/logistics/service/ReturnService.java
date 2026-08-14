package com.equipmentrental.logistics.service;

import com.equipmentrental.logistics.dto.request.CreateReturnInspectionRequest;
import com.equipmentrental.logistics.dto.request.CreateReturnRecordRequest;
import com.equipmentrental.logistics.dto.request.CreateReturnRequestRequest;
import com.equipmentrental.logistics.dto.response.ReturnInspectionResponse;
import com.equipmentrental.logistics.dto.response.ReturnRecordItemResponse;
import com.equipmentrental.logistics.dto.response.ReturnRecordResponse;
import com.equipmentrental.logistics.dto.response.ReturnRequestResponse;
import com.equipmentrental.logistics.entity.ReturnInspection;
import com.equipmentrental.logistics.entity.ReturnRecord;
import com.equipmentrental.logistics.entity.ReturnRecordItem;
import com.equipmentrental.logistics.entity.ReturnRequest;
import com.equipmentrental.logistics.entity.enums.ReturnInspectionStatus;
import com.equipmentrental.logistics.entity.enums.ReturnRecordStatus;
import com.equipmentrental.logistics.entity.enums.ReturnRequestStatus;
import com.equipmentrental.logistics.exception.ResourceNotFoundException;
import com.equipmentrental.logistics.repository.ReturnInspectionRepository;
import com.equipmentrental.logistics.repository.ReturnRecordItemRepository;
import com.equipmentrental.logistics.repository.ReturnRecordRepository;
import com.equipmentrental.logistics.repository.ReturnRequestRepository;
import com.equipmentrental.logistics.security.LogisticsDataScopeGuard;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnService {

    private final ReturnRequestRepository requestRepository;
    private final ReturnRecordRepository recordRepository;
    private final ReturnRecordItemRepository itemRepository;
    private final ReturnInspectionRepository inspectionRepository;
    private final LogisticsDataScopeGuard dataScopeGuard;

    public ReturnService(
            ReturnRequestRepository requestRepository,
            ReturnRecordRepository recordRepository,
            ReturnRecordItemRepository itemRepository,
            ReturnInspectionRepository inspectionRepository,
            LogisticsDataScopeGuard dataScopeGuard) {
        this.requestRepository = requestRepository;
        this.recordRepository = recordRepository;
        this.itemRepository = itemRepository;
        this.inspectionRepository = inspectionRepository;
        this.dataScopeGuard = dataScopeGuard;
    }

    // =========================================================
    // TẠO YÊU CẦU TRẢ
    // =========================================================

    @Transactional
    public ReturnRequestResponse createReturnRequest(CreateReturnRequestRequest req) {

        dataScopeGuard.requireCustomer(req.getOrganizationId(), req.getBranchId(), req.getCustomerId());

        ReturnRequest returnReq = new ReturnRequest();

        returnReq.setOrganizationId(req.getOrganizationId());
        returnReq.setBranchId(req.getBranchId());
        returnReq.setRentalOrderId(req.getRentalOrderId());
        returnReq.setCustomerId(req.getCustomerId());
        returnReq.setRequestedReturnDate(req.getRequestedReturnDate());

        returnReq.setStatus(ReturnRequestStatus.PENDING);

        return mapToRequestResponse(requestRepository.save(returnReq));
    }

    // =========================================================
    // KIỂM TRA KHI TRẢ
    // =========================================================

    @Transactional
    public ReturnInspectionResponse createInspection(CreateReturnInspectionRequest req) {

        ReturnRequest returnRequest = findRequest(req.getReturnRequestId());

        if (returnRequest.getStatus() == ReturnRequestStatus.COMPLETED) {

            throw new IllegalStateException("Return Request has already been completed");
        }

        ReturnInspection inspection = new ReturnInspection();

        inspection.setReturnRequestId(returnRequest.getId());

        inspection.setEquipmentId(req.getEquipmentId());

        inspection.setInspectedByUserId(dataScopeGuard.currentUserId());

        inspection.setInspectedAt(req.getInspectedAt());

        inspection.setConditionStatus(req.getConditionStatus());

        inspection.setMissingAccessories(req.getMissingAccessories());

        inspection.setDamageDescription(req.getDamageDescription());

        inspection.setDamaged(req.getDamaged() != null ? req.getDamaged() : false);

        inspection.setLate(req.getLate() != null ? req.getLate() : false);

        inspection.setLateMinutes(req.getLateMinutes() != null ? req.getLateMinutes() : 0L);

        inspection.setStatus(ReturnInspectionStatus.COMPLETED);

        ReturnInspection saved = inspectionRepository.save(inspection);

        return mapToInspectionResponse(saved);
    }

    // =========================================================
    // TẠO BIÊN BẢN NHẬN TRẢ
    // =========================================================

    @Transactional
    public ReturnRecordResponse createReturnRecord(CreateReturnRecordRequest req) {

        ReturnRequest request = findRequest(req.getReturnRequestId());

        if (request.getStatus() == ReturnRequestStatus.COMPLETED) {

            throw new IllegalStateException("Return Request has already been completed");
        }

        ReturnRecord record = new ReturnRecord();

        record.setReturnRequestId(req.getReturnRequestId());

        record.setRentalOrderId(req.getRentalOrderId());

        record.setInspectorStaffUserId(dataScopeGuard.currentUserId());

        record.setActualReturnTime(req.getActualReturnTime());

        record.setIsLateReturn(req.getIsLateReturn());

        record.setMissingAccessoriesDescription(req.getMissingAccessoriesDescription());

        record.setConditionDamageDescription(req.getConditionDamageDescription());

        record.setStatus(ReturnRecordStatus.CONFIRMED);

        record = recordRepository.save(record);

        final Long recordId = record.getId();

        if (req.getItems() != null) {

            List<ReturnRecordItem> itemsToSave = req.getItems().stream()
                    .map(i -> {
                        ReturnRecordItem item = new ReturnRecordItem();

                        item.setReturnRecordId(recordId);

                        item.setEquipmentId(i.getEquipmentId());

                        item.setReturnedCondition(i.getReturnedCondition());

                        item.setNotes(i.getNotes());

                        return item;
                    })
                    .collect(Collectors.toList());

            itemRepository.saveAll(itemsToSave);
        }

        request.setStatus(ReturnRequestStatus.COMPLETED);

        requestRepository.save(request);

        return getReturnRecord(recordId);
    }

    // =========================================================
    // LẤY BIÊN BẢN NHẬN TRẢ
    // =========================================================

    @Transactional(readOnly = true)
    public ReturnRecordResponse getReturnRecord(Long id) {

        ReturnRecord record = recordRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return Record not found"));

        findRequest(record.getReturnRequestId());

        ReturnRecordResponse res = new ReturnRecordResponse();

        res.setId(record.getId());

        res.setReturnRequestId(record.getReturnRequestId());

        res.setRentalOrderId(record.getRentalOrderId());

        res.setInspectorStaffUserId(record.getInspectorStaffUserId());

        res.setActualReturnTime(record.getActualReturnTime());

        res.setIsLateReturn(record.getIsLateReturn());

        res.setMissingAccessoriesDescription(record.getMissingAccessoriesDescription());

        res.setConditionDamageDescription(record.getConditionDamageDescription());

        res.setStatus(record.getStatus());

        res.setCreatedAt(record.getCreatedAt());

        res.setUpdatedAt(record.getUpdatedAt());

        List<ReturnRecordItemResponse> items = itemRepository.findByReturnRecordId(id).stream()
                .map(i -> {
                    ReturnRecordItemResponse itemRes = new ReturnRecordItemResponse();

                    itemRes.setId(i.getId());

                    itemRes.setEquipmentId(i.getEquipmentId());

                    itemRes.setReturnedCondition(i.getReturnedCondition());

                    itemRes.setNotes(i.getNotes());

                    return itemRes;
                })
                .collect(Collectors.toList());

        res.setItems(items);

        return res;
    }

    // =========================================================
    // MAP RETURN REQUEST
    // =========================================================

    private ReturnRequestResponse mapToRequestResponse(ReturnRequest req) {

        ReturnRequestResponse res = new ReturnRequestResponse();

        res.setId(req.getId());

        res.setRentalOrderId(req.getRentalOrderId());

        res.setCustomerId(req.getCustomerId());

        res.setRequestedReturnDate(req.getRequestedReturnDate());

        res.setStatus(req.getStatus());

        res.setCreatedAt(req.getCreatedAt());

        res.setUpdatedAt(req.getUpdatedAt());

        return res;
    }

    // =========================================================
    // MAP RETURN INSPECTION
    // =========================================================

    private ReturnInspectionResponse mapToInspectionResponse(ReturnInspection inspection) {

        ReturnInspectionResponse res = new ReturnInspectionResponse();

        res.setId(inspection.getId());

        res.setReturnRequestId(inspection.getReturnRequestId());

        res.setEquipmentId(inspection.getEquipmentId());

        res.setInspectedByUserId(inspection.getInspectedByUserId());

        res.setInspectedAt(inspection.getInspectedAt());

        res.setConditionStatus(inspection.getConditionStatus());

        res.setMissingAccessories(inspection.getMissingAccessories());

        res.setDamageDescription(inspection.getDamageDescription());

        res.setDamaged(inspection.getDamaged());

        res.setLate(inspection.getLate());

        res.setLateMinutes(inspection.getLateMinutes());

        res.setStatus(inspection.getStatus());

        res.setCreatedAt(inspection.getCreatedAt());

        return res;
    }

    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> getReturnRequests() {
        return requestRepository.findAll().stream()
                .filter(req -> dataScopeGuard.canAccessCustomer(
                        req.getOrganizationId(), req.getBranchId(), req.getCustomerId()))
                .map(this::mapToRequestResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReturnRequestResponse getReturnRequest(Long id) {

        ReturnRequest request = findRequest(id);

        return mapToRequestResponse(request);
    }

    @Transactional(readOnly = true)
    public List<ReturnInspectionResponse> getInspections() {

        return inspectionRepository.findAll().stream()
                .filter(inspection -> canAccessRequest(inspection.getReturnRequestId()))
                .map(this::mapToInspectionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReturnInspectionResponse getInspection(Long id) {

        ReturnInspection inspection = inspectionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return Inspection not found"));

        findRequest(inspection.getReturnRequestId());

        return mapToInspectionResponse(inspection);
    }

    @Transactional(readOnly = true)
    public List<ReturnRecordResponse> getReturnRecords() {

        return recordRepository.findAll().stream()
                .filter(record -> canAccessRequest(record.getReturnRequestId()))
                .map(record -> getReturnRecord(record.getId()))
                .collect(Collectors.toList());
    }

    private ReturnRequest findRequest(Long id) {
        ReturnRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return Request not found: " + id));
        dataScopeGuard.requireCustomer(
                request.getOrganizationId(), request.getBranchId(), request.getCustomerId());
        return request;
    }

    private boolean canAccessRequest(Long id) {
        return requestRepository.findById(id)
                .map(request -> dataScopeGuard.canAccessCustomer(
                        request.getOrganizationId(), request.getBranchId(), request.getCustomerId()))
                .orElse(false);
    }
}
