package com.eventhub.ems.service;

import com.eventhub.ems.dto.SpeakerRequest;
import com.eventhub.ems.dto.SpeakerResponse;
import java.util.List;

public interface SpeakerService {

    List<SpeakerResponse> getAll();

    SpeakerResponse getById(Long id);

    SpeakerResponse create(SpeakerRequest request);

    SpeakerResponse update(Long id, SpeakerRequest request);

    void delete(Long id);
}
