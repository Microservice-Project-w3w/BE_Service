package com.equipmentrental.logistics.controller;

import com.equipmentrental.logistics.dto.request.CreateDispatchNoteRequest;
import com.equipmentrental.logistics.dto.response.DispatchNoteResponse;
import com.equipmentrental.logistics.service.DispatchNoteService;
import com.equipmentrental.logistics.dto.request.UpdateDispatchNoteStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logistics/dispatch-notes")
public class DispatchNoteController {
    private final DispatchNoteService service;

    public DispatchNoteController(DispatchNoteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DispatchNoteResponse createDispatchNote(@Valid @RequestBody CreateDispatchNoteRequest request) {
        return service.createDispatchNote(request);
    }

    @GetMapping("/{id}")
    public DispatchNoteResponse getDispatchNote(@PathVariable Long id) {
        return service.getDispatchNote(id);
    }

    @GetMapping
    public List<DispatchNoteResponse> getAllDispatchNotes() {
        return service.getAllDispatchNotes();
    }

    @PatchMapping("/{id}/status")
    public DispatchNoteResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDispatchNoteStatusRequest request
    ) {
        return service.updateStatus(id, request);
    }

}
