package com.eventhub.ems.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventhub.ems.dto.SpeakerRequest;
import com.eventhub.ems.dto.SpeakerResponse;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.model.Speaker;
import com.eventhub.ems.repository.SpeakerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpeakerServiceTest {

    @Mock
    private SpeakerRepository speakerRepository;

    @InjectMocks
    private SpeakerServiceImpl speakerService;

    private Speaker speaker(Long id, String name) {
        Speaker s = new Speaker(name, "bio of " + name, "Cloud");
        s.setId(id);
        return s;
    }

    @Test
    void getAll_returnsSpeakersSortedByName() {
        when(speakerRepository.findAll()).thenReturn(List.of(
                speaker(1L, "Mia Chen"), speaker(2L, "Grace Park"), speaker(3L, "Leo Martins")));

        assertThat(speakerService.getAll())
                .extracting(SpeakerResponse::name)
                .containsExactly("Grace Park", "Leo Martins", "Mia Chen");
    }

    @Test
    void getById_returnsMappedSpeaker() {
        when(speakerRepository.findById(1L)).thenReturn(Optional.of(speaker(1L, "Grace Park")));

        SpeakerResponse response = speakerService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Grace Park");
        assertThat(response.expertise()).isEqualTo("Cloud");
    }

    @Test
    void getById_throwsWhenMissing() {
        when(speakerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> speakerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesSpeakerFromRequest() {
        when(speakerRepository.save(any(Speaker.class))).thenAnswer(inv -> {
            Speaker s = inv.getArgument(0);
            s.setId(7L);
            return s;
        });

        SpeakerResponse response = speakerService.create(new SpeakerRequest("New Speaker", "bio", "AI"));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.name()).isEqualTo("New Speaker");
        assertThat(response.expertise()).isEqualTo("AI");
    }

    @Test
    void update_overwritesFieldsOnExistingSpeaker() {
        Speaker existing = speaker(1L, "Grace Park");
        when(speakerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(speakerRepository.save(any(Speaker.class))).thenAnswer(inv -> inv.getArgument(0));

        SpeakerResponse response = speakerService.update(1L, new SpeakerRequest("Grace P.", "new bio", "DevOps"));

        assertThat(response.name()).isEqualTo("Grace P.");
        assertThat(existing.getBio()).isEqualTo("new bio");
        assertThat(existing.getExpertise()).isEqualTo("DevOps");
    }

    @Test
    void update_throwsWhenMissing() {
        when(speakerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> speakerService.update(99L, new SpeakerRequest("X", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(speakerRepository, never()).save(any());
    }

    @Test
    void delete_removesExistingSpeaker() {
        Speaker existing = speaker(1L, "Grace Park");
        when(speakerRepository.findById(1L)).thenReturn(Optional.of(existing));

        speakerService.delete(1L);

        verify(speakerRepository).delete(existing);
    }
}
