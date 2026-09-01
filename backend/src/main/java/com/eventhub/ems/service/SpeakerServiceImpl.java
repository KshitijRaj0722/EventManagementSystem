package com.eventhub.ems.service;

import com.eventhub.ems.dto.DtoMapper;
import com.eventhub.ems.dto.SpeakerRequest;
import com.eventhub.ems.dto.SpeakerResponse;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.model.Speaker;
import com.eventhub.ems.repository.SpeakerRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpeakerServiceImpl implements SpeakerService {

    private final SpeakerRepository speakerRepository;

    public SpeakerServiceImpl(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpeakerResponse> getAll() {
        return speakerRepository.findAll().stream()
                .sorted(Comparator.comparing(Speaker::getName))
                .map(DtoMapper::toSpeaker)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SpeakerResponse getById(Long id) {
        return DtoMapper.toSpeaker(getEntity(id));
    }

    @Override
    @Transactional
    public SpeakerResponse create(SpeakerRequest request) {
        Speaker speaker = new Speaker(request.name(), request.bio(), request.expertise());
        return DtoMapper.toSpeaker(speakerRepository.save(speaker));
    }

    @Override
    @Transactional
    public SpeakerResponse update(Long id, SpeakerRequest request) {
        Speaker speaker = getEntity(id);
        speaker.setName(request.name());
        speaker.setBio(request.bio());
        speaker.setExpertise(request.expertise());
        return DtoMapper.toSpeaker(speakerRepository.save(speaker));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Speaker speaker = getEntity(id);
        speakerRepository.delete(speaker);
    }

    private Speaker getEntity(Long id) {
        return speakerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speaker not found: " + id));
    }
}
