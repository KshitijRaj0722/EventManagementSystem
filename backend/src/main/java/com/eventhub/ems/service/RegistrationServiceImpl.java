package com.eventhub.ems.service;

import com.eventhub.ems.dto.DtoMapper;
import com.eventhub.ems.dto.RegistrationResponse;
import com.eventhub.ems.exception.ConflictException;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.Registration;
import com.eventhub.ems.model.User;
import com.eventhub.ems.repository.EventRepository;
import com.eventhub.ems.repository.RegistrationRepository;
import com.eventhub.ems.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   EventRepository eventRepository,
                                   UserRepository userRepository,
                                   EmailService emailService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public RegistrationResponse registerForEvent(String userEmail, Long eventId) {
        User user = getUser(userEmail);
        Event event = getEvent(eventId);

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new ConflictException("You are already registered for this event");
        }
        if (event.getCapacity() != null) {
            long count = registrationRepository.countByEvent(event);
            if (count >= event.getCapacity()) {
                throw new ConflictException("This event is full");
            }
        }

        Registration registration = registrationRepository.save(new Registration(user, event));
        emailService.sendRegistrationConfirmation(user, event);
        return DtoMapper.toRegistration(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations(String userEmail) {
        User user = getUser(userEmail);
        return registrationRepository.findByUser(user).stream()
                .sorted(Comparator.comparing((Registration r) -> r.getEvent().getDateTime()))
                .map(DtoMapper::toRegistration)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getEventRegistrations(Long eventId) {
        Event event = getEvent(eventId);
        return registrationRepository.findByEvent(event).stream()
                .sorted(Comparator.comparing((Registration r) -> r.getUser().getName()))
                .map(DtoMapper::toRegistration)
                .toList();
    }

    @Override
    @Transactional
    public RegistrationResponse setAttendance(Long registrationId, boolean attended) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + registrationId));
        registration.setAttended(attended);
        return DtoMapper.toRegistration(registrationRepository.save(registration));
    }

    @Override
    @Transactional
    public void cancelRegistration(String userEmail, Long eventId) {
        User user = getUser(userEmail);
        Event event = getEvent(eventId);
        Registration registration = registrationRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ResourceNotFoundException("You are not registered for this event"));
        registrationRepository.delete(registration);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }
}
