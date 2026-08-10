package com.equipmentrental.logistics.service;

import com.equipmentrental.logistics.dto.request.CreateHandoverRecordRequest;
import com.equipmentrental.logistics.dto.request.HandoverPhotoRequest;
import com.equipmentrental.logistics.dto.response.HandoverChecklistResponse;
import com.equipmentrental.logistics.dto.response.HandoverPhotoResponse;
import com.equipmentrental.logistics.dto.response.HandoverRecordResponse;
import com.equipmentrental.logistics.entity.HandoverChecklist;
import com.equipmentrental.logistics.entity.HandoverPhoto;
import com.equipmentrental.logistics.entity.HandoverRecord;
import com.equipmentrental.logistics.entity.enums.HandoverStatus;
import com.equipmentrental.logistics.exception.ResourceNotFoundException;
import com.equipmentrental.logistics.repository.HandoverChecklistRepository;
import com.equipmentrental.logistics.repository.HandoverPhotoRepository;
import com.equipmentrental.logistics.repository.HandoverRecordRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HandoverRecordService {

    private final HandoverRecordRepository repository;
    private final HandoverChecklistRepository checklistRepository;
    private final HandoverPhotoRepository photoRepository;

    public HandoverRecordService(
            HandoverRecordRepository repository,
            HandoverChecklistRepository checklistRepository,
            HandoverPhotoRepository photoRepository) {
        this.repository = repository;
        this.checklistRepository = checklistRepository;
        this.photoRepository = photoRepository;
    }

    @Transactional
    public HandoverRecordResponse createHandoverRecord(CreateHandoverRecordRequest request) {

        HandoverRecord record = new HandoverRecord();

        record.setDispatchNoteId(request.getDispatchNoteId());
        record.setHandoverTime(request.getHandoverTime());
        record.setReceiverName(request.getReceiverName());
        record.setReceiverPhone(request.getReceiverPhone());
        record.setCustomerSignatureUrl(request.getCustomerSignatureUrl());
        record.setNotes(request.getNotes());

        record.setStatus(HandoverStatus.PENDING);

        record = repository.save(record);

        final Long recordId = record.getId();

        if (request.getChecklists() != null) {

            request.getChecklists().forEach(c -> {
                HandoverChecklist checklist = new HandoverChecklist();

                checklist.setHandoverRecordId(recordId);
                checklist.setCheckpointName(c.getCheckpointName());

                checklist.setSortOrder(c.getSortOrder() != null ? c.getSortOrder() : 0);

                checklist.setStatus(c.getStatus() != null ? c.getStatus() : "PENDING");

                checklist.setIsPassed(c.getIsPassed() != null ? c.getIsPassed() : false);

                checklist.setRemarks(c.getRemarks());

                checklistRepository.save(checklist);
            });
        }

        if (request.getPhotos() != null) {

            request.getPhotos().forEach(p -> {
                HandoverPhoto photo = new HandoverPhoto();

                photo.setHandoverRecordId(recordId);
                photo.setPhotoUrl(p.getPhotoUrl());
                photo.setPhotoType(p.getPhotoType());

                photo.setSortOrder(p.getSortOrder() != null ? p.getSortOrder() : 0);

                photo.setStatus(p.getStatus() != null ? p.getStatus() : "ACTIVE");

                photoRepository.save(photo);
            });
        }

        return getHandoverRecord(recordId);
    }

    @Transactional
    public HandoverPhotoResponse addPhoto(Long handoverRecordId, HandoverPhotoRequest request) {

        repository
                .findById(handoverRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Handover Record not found"));

        HandoverPhoto photo = new HandoverPhoto();

        photo.setHandoverRecordId(handoverRecordId);
        photo.setPhotoUrl(request.getPhotoUrl());
        photo.setPhotoType(request.getPhotoType());

        photo.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        photo.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        HandoverPhoto saved = photoRepository.save(photo);

        HandoverPhotoResponse response = new HandoverPhotoResponse();

        response.setId(saved.getId());
        response.setPhotoUrl(saved.getPhotoUrl());
        response.setPhotoType(saved.getPhotoType());
        response.setSortOrder(saved.getSortOrder());
        response.setStatus(saved.getStatus());

        return response;
    }

    @Transactional(readOnly = true)
    public HandoverRecordResponse getHandoverRecord(Long id) {

        HandoverRecord record =
                repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Handover Record not found"));

        return mapToResponse(record);
    }

    private HandoverRecordResponse mapToResponse(HandoverRecord record) {

        HandoverRecordResponse res = new HandoverRecordResponse();

        res.setId(record.getId());
        res.setDispatchNoteId(record.getDispatchNoteId());
        res.setHandoverTime(record.getHandoverTime());
        res.setReceiverName(record.getReceiverName());
        res.setReceiverPhone(record.getReceiverPhone());

        res.setCustomerSignatureUrl(record.getCustomerSignatureUrl());

        res.setNotes(record.getNotes());
        res.setStatus(record.getStatus());
        res.setCreatedAt(record.getCreatedAt());
        res.setUpdatedAt(record.getUpdatedAt());

        List<HandoverChecklistResponse> checklistResponses =
                checklistRepository.findByHandoverRecordId(record.getId()).stream()
                        .map(c -> {
                            HandoverChecklistResponse cr = new HandoverChecklistResponse();

                            cr.setId(c.getId());
                            cr.setCheckpointName(c.getCheckpointName());

                            cr.setSortOrder(c.getSortOrder());
                            cr.setStatus(c.getStatus());
                            cr.setIsPassed(c.getIsPassed());
                            cr.setRemarks(c.getRemarks());

                            return cr;
                        })
                        .collect(Collectors.toList());

        res.setChecklists(checklistResponses);

        List<HandoverPhotoResponse> photoResponses = photoRepository.findByHandoverRecordId(record.getId()).stream()
                .map(p -> {
                    HandoverPhotoResponse pr = new HandoverPhotoResponse();

                    pr.setId(p.getId());
                    pr.setPhotoUrl(p.getPhotoUrl());
                    pr.setPhotoType(p.getPhotoType());
                    pr.setSortOrder(p.getSortOrder());
                    pr.setStatus(p.getStatus());

                    return pr;
                })
                .collect(Collectors.toList());

        res.setPhotos(photoResponses);

        return res;
    }

    @Transactional
    public HandoverRecordResponse confirmHandoverRecord(Long id) {

        HandoverRecord record =
                repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Handover Record not found"));

        if (record.getStatus() == HandoverStatus.CONFIRMED) {
            return mapToResponse(record);
        }

        if (record.getStatus() == HandoverStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled handover record");
        }

        record.setStatus(HandoverStatus.CONFIRMED);

        record = repository.save(record);

        return mapToResponse(record);
    }

    @Transactional(readOnly = true)
    public List<HandoverPhotoResponse> getPhotos(Long handoverRecordId) {

        if (!repository.existsById(handoverRecordId)) {
            throw new ResourceNotFoundException("Handover Record not found");
        }

        return photoRepository.findByHandoverRecordId(handoverRecordId).stream()
                .map(p -> {
                    HandoverPhotoResponse res = new HandoverPhotoResponse();

                    res.setId(p.getId());
                    res.setPhotoUrl(p.getPhotoUrl());
                    res.setPhotoType(p.getPhotoType());
                    res.setSortOrder(p.getSortOrder());
                    res.setStatus(p.getStatus());

                    return res;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HandoverRecordResponse> getHandoverRecords() {

        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}
