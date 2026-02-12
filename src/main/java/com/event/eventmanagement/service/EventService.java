package com.event.eventmanagement.service;

import com.event.eventmanagement.model.Event;
import com.event.eventmanagement.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Save Event
    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

    // Get All Events
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Get Event By ID
    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    // Delete Event
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
    // Update Event
    public void updateEvent(Event event) {
        eventRepository.save(event);
    }

}
