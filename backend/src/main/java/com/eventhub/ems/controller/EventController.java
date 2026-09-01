package com.eventhub.ems.controller;

import com.eventhub.ems.dto.EventResponse;
import com.eventhub.ems.service.EventService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /** Public listing with optional search & filters (date, location, category). */
    @GetMapping
    public List<EventResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        return eventService.search(search, location, category, fromDate);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }
}
