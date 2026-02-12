package com.event.eventmanagement.controller;

import com.event.eventmanagement.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EventService eventService;

    public DashboardController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("events", eventService.getAllEvents());

        return "dashboard";
    }
}
