package com.eventhub.ems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

public record EventRequest(
        @NotBlank String title,
        String description,
        @NotNull LocalDateTime dateTime,
        @NotBlank String venue,
        @NotBlank String location,
        @NotBlank String category,
        Integer capacity,
        Set<Long> speakerIds) {
}
