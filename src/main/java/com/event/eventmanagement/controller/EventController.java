package com.event.eventmanagement.controller;

import com.event.eventmanagement.model.Event;
import com.event.eventmanagement.service.EventService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ✅ View All Events (USER + ADMIN)
    @GetMapping
    public String viewEvents(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "events";
    }

    // ✅ Show Create Form (ADMIN ONLY)
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("event", new Event());
        return "create-event";
    }

    // ✅ Save Event (ADMIN ONLY)
    @PostMapping("/save-event")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveEvent(@ModelAttribute Event event) {
        eventService.saveEvent(event);
        return "redirect:/events";
    }

    // ✅ Show Edit Form (ADMIN ONLY)
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        return "edit-event";
    }

    // ✅ Update Event (ADMIN ONLY)
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateEvent(@ModelAttribute Event event) {
        eventService.updateEvent(event);
        return "redirect:/events";
    }

    // ✅ Delete Event (ADMIN ONLY)
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return "redirect:/events";
    }
}
