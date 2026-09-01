package com.eventhub.ems.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.Registration;
import com.eventhub.ems.model.Role;
import com.eventhub.ems.model.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class RegistrationRepositoryTest {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User jane;
    private User john;
    private Event event;
    private Event otherEvent;

    private Event newEvent(String title) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription("desc");
        e.setDateTime(LocalDateTime.now().plusDays(5).withNano(0));
        e.setVenue("Hall A");
        e.setLocation("Bengaluru");
        e.setCategory("Technology");
        e.setCapacity(100);
        return eventRepository.save(e);
    }

    @BeforeEach
    void setup() {
        jane = userRepository.save(new User("Jane", "jane@example.com", "ENC", Role.USER));
        john = userRepository.save(new User("John", "john@example.com", "ENC", Role.USER));
        event = newEvent("Spring Boot Masterclass");
        otherEvent = newEvent("AI Summit");
    }

    @Test
    void findByUser_returnsOnlyThatUsersRegistrations() {
        registrationRepository.save(new Registration(jane, event));
        registrationRepository.save(new Registration(jane, otherEvent));
        registrationRepository.save(new Registration(john, event));

        assertThat(registrationRepository.findByUser(jane)).hasSize(2);
        assertThat(registrationRepository.findByUser(john)).hasSize(1);
    }

    @Test
    void findByEvent_returnsAllAttendeesOfThatEvent() {
        registrationRepository.save(new Registration(jane, event));
        registrationRepository.save(new Registration(john, event));
        registrationRepository.save(new Registration(jane, otherEvent));

        assertThat(registrationRepository.findByEvent(event))
                .extracting(r -> r.getUser().getEmail())
                .containsExactlyInAnyOrder("jane@example.com", "john@example.com");
    }

    @Test
    void findByUserAndEvent_findsTheMatchAndIsEmptyOtherwise() {
        registrationRepository.save(new Registration(jane, event));

        assertThat(registrationRepository.findByUserAndEvent(jane, event)).isPresent();
        assertThat(registrationRepository.findByUserAndEvent(jane, otherEvent)).isEmpty();
    }

    @Test
    void existsByUserAndEvent_reflectsWhetherRegistered() {
        assertThat(registrationRepository.existsByUserAndEvent(jane, event)).isFalse();

        registrationRepository.save(new Registration(jane, event));

        assertThat(registrationRepository.existsByUserAndEvent(jane, event)).isTrue();
    }

    @Test
    void countByEvent_countsOnlyThatEvent() {
        registrationRepository.save(new Registration(jane, event));
        registrationRepository.save(new Registration(john, event));
        registrationRepository.save(new Registration(jane, otherEvent));

        assertThat(registrationRepository.countByEvent(event)).isEqualTo(2);
        assertThat(registrationRepository.countByEvent(otherEvent)).isEqualTo(1);
    }

    @Test
    void uniqueConstraint_preventsDuplicateRegistration() {
        registrationRepository.saveAndFlush(new Registration(jane, event));

        assertThatThrownBy(() -> registrationRepository.saveAndFlush(new Registration(jane, event)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void attendanceFlagDefaultsToFalseAndPersists() {
        Registration saved = registrationRepository.saveAndFlush(new Registration(jane, event));
        assertThat(saved.isAttended()).isFalse();

        saved.setAttended(true);
        registrationRepository.saveAndFlush(saved);

        assertThat(registrationRepository.findById(saved.getId()))
                .get()
                .extracting(Registration::isAttended)
                .isEqualTo(true);
    }
}
