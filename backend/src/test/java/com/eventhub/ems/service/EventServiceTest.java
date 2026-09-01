package com.eventhub.ems.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventhub.ems.dto.EventRequest;
import com.eventhub.ems.dto.EventResponse;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.model.Event;
import com.eventhub.ems.repository.EventRepository;
import com.eventhub.ems.repository.RegistrationRepository;
import com.eventhub.ems.repository.SpeakerRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SpeakerRepository speakerRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event sampleEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Spring Boot Masterclass");
        event.setDescription("Hands-on Spring Boot");
        event.setDateTime(LocalDateTime.now().plusDays(5));
        event.setVenue("Hall A");
        event.setLocation("Bengaluru");
        event.setCategory("Technology");
        event.setCapacity(100);
        return event;
    }

    @Test
    void getById_returnsMappedResponseWithRegisteredCount() {
        Event event = sampleEvent();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(registrationRepository.countByEvent(event)).thenReturn(7L);

        EventResponse response = eventService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Spring Boot Masterclass");
        assertThat(response.registeredCount()).isEqualTo(7L);
    }

    @Test
    void getById_throwsWhenMissing() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_persistsEventWithProvidedFields() {
        EventRequest request = new EventRequest(
                "New Event", "desc", LocalDateTime.now().plusDays(2),
                "Venue X", "Pune", "Networking", 30, null);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(5L);
            return e;
        });
        when(registrationRepository.countByEvent(any(Event.class))).thenReturn(0L);

        EventResponse response = eventService.create(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.location()).isEqualTo("Pune");
        assertThat(response.category()).isEqualTo("Networking");
    }

    @Test
    void delete_removesRegistrationsThenEvent() {
        Event event = sampleEvent();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(registrationRepository.findByEvent(event)).thenReturn(List.of());

        eventService.delete(1L);

        verify(registrationRepository).deleteAll(List.of());
        verify(eventRepository).delete(event);
    }
}
