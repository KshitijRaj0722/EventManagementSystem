package com.event.eventmanagement.controller;

import com.event.eventmanagement.model.User;
import com.event.eventmanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Show Signup Page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle Signup
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {

        userService.registerUser(user);

        return "redirect:/login";
    }

    // Show Login Page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}
