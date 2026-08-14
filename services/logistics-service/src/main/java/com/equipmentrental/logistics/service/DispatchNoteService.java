package com.equipmentrental.logistics.service;

import com.equipmentrental.logistics.dto.request.CreateDispatchNoteRequest;
import com.equipmentrental.logistics.dto.response.DispatchNoteItemResponse;
import com.equipmentrental.logistics.dto.response.DispatchNoteResponse;
import com.equipmentrental.logistics.entity.DispatchNote;
import com.equipmentrental.logistics.entity.DispatchNoteItem;
import com.equipmentrental.logistics.dto.request.UpdateDispatchNoteStatusRequest;
import com.equipmentrental.logistics.entity.enums.DispatchStatus;
import com.equipmentrental.logistics.exception.ResourceNotFoundException;
import com.equipmentrental.logistics.repository.DispatchNoteItemRepository;
import com.equipmentrental.logistics.repository.DispatchNoteRepository;
import com.equipmentrental.logistics.repository.DeliveryTaskRepository;
import com.equipmentrental.logistics.entity.DeliveryTask;
import com.equipmentrental.logistics.security.LogisticsDataScopeGuard;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispatchNoteService {

    private final DispatchNoteRepository repository;
    private final DispatchNoteItemRepository itemRepository;
    private final DeliveryTaskRepository taskRepository;
    private final LogisticsDataScopeGuard dataScopeGuard;

    public DispatchNoteService(
            DispatchNoteRepository repository,
            DispatchNoteItemRepository itemRepository,
            DeliveryTaskRepository taskRepository,
            LogisticsDataScopeGuard dataScopeGuard) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.taskRepository = taskRepository;
        this.dataScopeGuard = dataScopeGuard;
    }

    @Transactional
    public DispatchNoteResponse createDispatchNote(CreateDispatchNoteRequest request) {

        DeliveryTask task = taskRepository.findById(request.getDeliveryTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery task not found"));
        dataScopeGuard.requireBranch(task.getOrganizationId(), task.getBranchId());
        if (!task.getRentalOrderId().equals(request.getRentalOrderId())) {
            throw new IllegalArgumentException("Delivery task không thuộc đơn thuê đã chọn");
        }

        DispatchNote note = new DispatchNote();

        note.setOrganizationId(task.getOrganizationId());
        note.setBranchId(task.getBranchId());
        note.setDispatchCode(request.getDispatchCode());
        note.setRentalOrderId(request.getRentalOrderId());
        note.setCustomerId(request.getCustomerId());
        note.setDeliveryTaskId(request.getDeliveryTaskId());
        note.setStatus(DispatchStatus.PREPARED);

        DispatchNote savedNote = repository.save(note);

        for (Long equipmentId : request.getEquipmentIds()) {

            DispatchNoteItem item = new DispatchNoteItem();

            item.setDispatchNoteId(savedNote.getId());
            item.setEquipmentId(equipmentId);

            itemRepository.save(item);
        }

        return getDispatchNote(savedNote.getId());
    }

    @Transactional(readOnly = true)
    public DispatchNoteResponse getDispatchNote(Long id) {

        DispatchNote note = findNote(id);

        return mapToResponse(note);
    }

    private DispatchNoteResponse mapToResponse(DispatchNote note) {

        DispatchNoteResponse res = new DispatchNoteResponse();

        res.setId(note.getId());
        res.setDispatchCode(note.getDispatchCode());
        res.setRentalOrderId(note.getRentalOrderId());
        res.setCustomerId(note.getCustomerId());
        res.setDeliveryTaskId(note.getDeliveryTaskId());
        res.setStatus(note.getStatus());
        res.setCreatedAt(note.getCreatedAt());
        res.setUpdatedAt(note.getUpdatedAt());

        List<DispatchNoteItemResponse> itemResponses = itemRepository.findByDispatchNoteId(note.getId()).stream()
                .map(item -> {
                    DispatchNoteItemResponse itemRes = new DispatchNoteItemResponse();

                    itemRes.setId(item.getId());
                    itemRes.setEquipmentId(item.getEquipmentId());

                    return itemRes;
                })
                .collect(Collectors.toList());

        res.setItems(itemResponses);

        return res;
    }
    @Transactional(readOnly = true)
    public List<DispatchNoteResponse> getAllDispatchNotes() {
        return repository.findAll()
                .stream()
                .filter(note -> dataScopeGuard.canAccessBranch(note.getOrganizationId(), note.getBranchId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public DispatchNoteResponse updateStatus(
            Long id,
            UpdateDispatchNoteStatusRequest request
    ) {

        DispatchNote note = findNote(id);

        note.setStatus(request.getStatus());

        return mapToResponse(repository.save(note));
    }

    private DispatchNote findNote(Long id) {
        DispatchNote note = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch note not found: " + id));
        dataScopeGuard.requireBranch(note.getOrganizationId(), note.getBranchId());
        return note;
    }
}
