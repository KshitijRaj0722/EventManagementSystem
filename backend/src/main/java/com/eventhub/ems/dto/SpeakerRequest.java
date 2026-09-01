package com.eventhub.ems.dto;

import jakarta.validation.constraints.NotBlank;

public record SpeakerRequest(
        @NotBlank String name,
        String bio,
        String expertise) {
}
