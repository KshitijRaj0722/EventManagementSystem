package com.eventhub.ems.dto;

import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.Registration;
import com.eventhub.ems.model.Speaker;
import com.eventhub.ems.model.User;
import java.util.Comparator;
import java.util.List;

/** Maps JPA entities to API response DTOs. */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserResponse toUser(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public static SpeakerResponse toSpeaker(Speaker speaker) {
        return new SpeakerResponse(speaker.getId(), speaker.getName(), speaker.getBio(), speaker.getExpertise());
    }

    public static EventResponse toEvent(Event event, long registeredCount) {
        List<SpeakerResponse> speakers = event.getSpeakers().stream()
                .map(DtoMapper::toSpeaker)
                .sorted(Comparator.comparing(SpeakerResponse::name))
                .toList();
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getVenue(),
                event.getLocation(),
                event.getCategory(),
                event.getCapacity(),
                registeredCount,
                speakers);
    }

    public static RegistrationResponse toRegistration(Registration reg) {
        Event event = reg.getEvent();
        User user = reg.getUser();
        return new RegistrationResponse(
                reg.getId(),
                event.getId(),
                event.getTitle(),
                event.getDateTime(),
                event.getVenue(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                reg.getRegisteredAt(),
                reg.isAttended());
    }
}
