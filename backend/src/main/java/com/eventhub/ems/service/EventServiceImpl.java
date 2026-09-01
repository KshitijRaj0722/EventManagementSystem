package com.eventhub.ems.service;

import com.eventhub.ems.dto.DtoMapper;
import com.eventhub.ems.dto.EventRequest;
import com.eventhub.ems.dto.EventResponse;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.Speaker;
import com.eventhub.ems.repository.EventRepository;
import com.eventhub.ems.repository.RegistrationRepository;
import com.eventhub.ems.repository.SpeakerRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final SpeakerRepository speakerRepository;
    private final RegistrationRepository registrationRepository;

    public EventServiceImpl(EventRepository eventRepository,
                            SpeakerRepository speakerRepository,
                            RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.speakerRepository = speakerRepository;
        this.registrationRepository = registrationRepository;
    }

    /** Disabled-filter sentinel for the date: any event is on/after this. */
    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> search(String search, String location, String category, LocalDate fromDate) {
        // Empty string / epoch act as "no filter" (see EventRepository.search for why).
        String s = StringUtils.hasText(search) ? search.trim() : "";
        String loc = StringUtils.hasText(location) ? location.trim() : "";
        String cat = StringUtils.hasText(category) ? category.trim() : "";
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : EPOCH;

        return eventRepository.search(s, loc, cat, from).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEntity(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    @Override
    @Transactional
    public EventResponse create(EventRequest request) {
        Event event = new Event();
        apply(event, request);
        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = getEntity(id);
        apply(event, request);
        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Event event = getEntity(id);
        // Remove dependent registrations first to satisfy FK constraints.
        registrationRepository.deleteAll(registrationRepository.findByEvent(event));
        eventRepository.delete(event);
    }

    private void apply(Event event, EventRequest request) {
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDateTime(request.dateTime());
        event.setVenue(request.venue());
        event.setLocation(request.location());
        event.setCategory(request.category());
        event.setCapacity(request.capacity());
        event.setSpeakers(resolveSpeakers(request.speakerIds()));
    }

    private Set<Speaker> resolveSpeakers(Set<Long> ids) {
        Set<Speaker> speakers = new HashSet<>();
        if (ids == null) {
            return speakers;
        }
        for (Long id : ids) {
            speakers.add(speakerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Speaker not found: " + id)));
        }
        return speakers;
    }

    private EventResponse toResponse(Event event) {
        long count = registrationRepository.countByEvent(event);
        return DtoMapper.toEvent(event, count);
    }
}
