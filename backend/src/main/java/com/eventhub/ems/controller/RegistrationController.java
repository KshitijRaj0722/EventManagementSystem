package com.eventhub.ems.controller;

import com.eventhub.ems.dto.RegistrationResponse;
import com.eventhub.ems.service.RegistrationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints for the logged-in user to manage their own event registrations. */
@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/events/{eventId}/register")
    public ResponseEntity<RegistrationResponse> register(@PathVariable Long eventId,
                                                         Authentication authentication) {
        RegistrationResponse response = registrationService.registerForEvent(authentication.getName(), eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/events/{eventId}/register")
    public ResponseEntity<Void> cancel(@PathVariable Long eventId, Authentication authentication) {
        registrationService.cancelRegistration(authentication.getName(), eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/registrations/me")
    public List<RegistrationResponse> myRegistrations(Authentication authentication) {
        return registrationService.getMyRegistrations(authentication.getName());
    }
}
