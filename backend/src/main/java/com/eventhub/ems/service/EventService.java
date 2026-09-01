package com.eventhub.ems.service;

import com.eventhub.ems.dto.EventRequest;
import com.eventhub.ems.dto.EventResponse;
import com.eventhub.ems.model.Event;
import java.time.LocalDate;
import java.util.List;

public interface EventService {

    List<EventResponse> search(String search, String location, String category, LocalDate fromDate);

    EventResponse getById(Long id);

    Event getEntity(Long id);

    EventResponse create(EventRequest request);

    EventResponse update(Long id, EventRequest request);

    void delete(Long id);
}
