package com.eventhub.ems.dto;

import java.time.LocalDateTime;

public record RegistrationResponse(
        Long id,
        Long eventId,
        String eventTitle,
        LocalDateTime eventDateTime,
        String venue,
        Long userId,
        String userName,
        String userEmail,
        LocalDateTime registeredAt,
        boolean attended) {
}
