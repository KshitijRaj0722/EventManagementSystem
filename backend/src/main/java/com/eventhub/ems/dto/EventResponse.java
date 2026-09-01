package com.eventhub.ems.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime dateTime,
        String venue,
        String location,
        String category,
        Integer capacity,
        long registeredCount,
        List<SpeakerResponse> speakers) {
}
