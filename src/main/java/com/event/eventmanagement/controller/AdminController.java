package com.event.eventmanagement.controller;

import com.event.eventmanagement.service.EventService;
import com.event.eventmanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class AdminController {

    private final UserService userService;
    private final EventService eventService;

    public AdminController(UserService userService,
                           EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        int userCount = userService.getAllUsers().size();
        int eventCount = eventService.getAllEvents().size();

        System.out.println("Users Count: " + userCount);
        System.out.println("Events Count: " + eventCount);

        model.addAttribute("totalUsers", userCount);
        model.addAttribute("totalEvents", eventCount);

        return "admin-dashboard";
    }

}
