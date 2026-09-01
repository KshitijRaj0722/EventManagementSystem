package com.eventhub.ems.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventhub.ems.dto.RegistrationResponse;
import com.eventhub.ems.exception.ConflictException;
import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.Registration;
import com.eventhub.ems.model.Role;
import com.eventhub.ems.model.User;
import com.eventhub.ems.repository.EventRepository;
import com.eventhub.ems.repository.RegistrationRepository;
import com.eventhub.ems.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private User user;
    private Event event;

    @BeforeEach
    void setup() {
        user = new User("Jane", "jane@example.com", "ENC", Role.USER);
        user.setId(1L);
        event = new Event();
        event.setId(2L);
        event.setTitle("Spring Boot Masterclass");
        event.setDateTime(LocalDateTime.now().plusDays(5));
        event.setVenue("Hall A");
        event.setLocation("Bengaluru");
        event.setCategory("Technology");
        event.setCapacity(2);
    }

    @Test
    void registerForEvent_savesAndSendsConfirmationEmail() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
        when(registrationRepository.countByEvent(event)).thenReturn(0L);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> {
            Registration r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        RegistrationResponse response = registrationService.registerForEvent("jane@example.com", 2L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.eventId()).isEqualTo(2L);
        assertThat(response.attended()).isFalse();
        verify(emailService).sendRegistrationConfirmation(user, event);
    }

    @Test
    void registerForEvent_throwsConflictWhenAlreadyRegistered() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerForEvent("jane@example.com", 2L))
                .isInstanceOf(ConflictException.class);

        verify(registrationRepository, never()).save(any());
        verify(emailService, never()).sendRegistrationConfirmation(any(), any());
    }

    @Test
    void registerForEvent_throwsConflictWhenEventFull() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
        when(registrationRepository.countByEvent(event)).thenReturn(2L); // capacity is 2

        assertThatThrownBy(() -> registrationService.registerForEvent("jane@example.com", 2L))
                .isInstanceOf(ConflictException.class);

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void setAttendance_marksRegistrationAttended() {
        Registration reg = new Registration(user, event);
        reg.setId(10L);
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrationResponse response = registrationService.setAttendance(10L, true);

        assertThat(response.attended()).isTrue();
        assertThat(reg.isAttended()).isTrue();
    }

    @Test
    void getMyRegistrations_returnsUsersRegistrations() {
        Registration reg = new Registration(user, event);
        reg.setId(10L);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(registrationRepository.findByUser(user)).thenReturn(List.of(reg));

        List<RegistrationResponse> result = registrationService.getMyRegistrations("jane@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userEmail()).isEqualTo("jane@example.com");
    }
}
