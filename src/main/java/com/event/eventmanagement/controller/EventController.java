package com.event.eventmanagement.controller;

import com.event.eventmanagement.model.Event;
import com.event.eventmanagement.service.EventService;
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

    // ✅ SHOW CREATE FORM
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("event", new Event());
        return "create-event";
    }


    // ✅ SAVE EVENT
    @PostMapping("/save-event")
    public String saveEvent(@ModelAttribute Event event) {
        eventService.saveEvent(event);
        return "redirect:/dashboard";
    }

    // View All Events
    @GetMapping
    public String viewEvents(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "events";
    }

    // Edit
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        return "edit-event";
    }

    // Update
    @PostMapping("/update")
    public String updateEvent(@ModelAttribute Event event) {
        eventService.updateEvent(event);
        return "redirect:/dashboard";
    }

    // Delete
    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return "redirect:/dashboard";
    }
}
