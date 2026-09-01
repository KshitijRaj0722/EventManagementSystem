package com.eventhub.ems.controller;

import com.eventhub.ems.dto.SpeakerResponse;
import com.eventhub.ems.service.SpeakerService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/speakers")
public class SpeakerController {

    private final SpeakerService speakerService;

    public SpeakerController(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    @GetMapping
    public List<SpeakerResponse> list() {
        return speakerService.getAll();
    }

    @GetMapping("/{id}")
    public SpeakerResponse getOne(@PathVariable Long id) {
        return speakerService.getById(id);
    }
}
