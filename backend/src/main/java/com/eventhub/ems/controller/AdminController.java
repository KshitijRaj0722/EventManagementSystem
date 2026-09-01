package com.eventhub.ems.controller;

import com.eventhub.ems.dto.EventRequest;
import com.eventhub.ems.dto.EventResponse;
import com.eventhub.ems.dto.RegistrationResponse;
import com.eventhub.ems.dto.SpeakerRequest;
import com.eventhub.ems.dto.SpeakerResponse;
import com.eventhub.ems.service.EventService;
import com.eventhub.ems.service.RegistrationService;
import com.eventhub.ems.service.SpeakerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only management endpoints. Secured by hasRole('ADMIN') in SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final RegistrationService registrationService;

    public AdminController(EventService eventService,
                          SpeakerService speakerService,
                          RegistrationService registrationService) {
        this.eventService = eventService;
        this.speakerService = speakerService;
        this.registrationService = registrationService;
    }

    // ----- Events -----

    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @PutMapping("/events/{id}")
    public EventResponse updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ----- Speakers -----

    @PostMapping("/speakers")
    public ResponseEntity<SpeakerResponse> createSpeaker(@Valid @RequestBody SpeakerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(speakerService.create(request));
    }

    @PutMapping("/speakers/{id}")
    public SpeakerResponse updateSpeaker(@PathVariable Long id, @Valid @RequestBody SpeakerRequest request) {
        return speakerService.update(id, request);
    }

    @DeleteMapping("/speakers/{id}")
    public ResponseEntity<Void> deleteSpeaker(@PathVariable Long id) {
        speakerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ----- Attendance / registrations -----

    @GetMapping("/events/{id}/registrations")
    public List<RegistrationResponse> eventRegistrations(@PathVariable Long id) {
        return registrationService.getEventRegistrations(id);
    }

    @PutMapping("/registrations/{id}/attendance")
    public RegistrationResponse setAttendance(@PathVariable Long id,
                                              @RequestParam boolean attended) {
        return registrationService.setAttendance(id, attended);
    }
}
