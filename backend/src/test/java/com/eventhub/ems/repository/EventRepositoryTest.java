package com.eventhub.ems.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.Speaker;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** Repository-layer tests for the search/filter query and the reminder-window lookup. */
@DataJpaTest
class EventRepositoryTest {

    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SpeakerRepository speakerRepository;

    private LocalDateTime base;

    private Event newEvent(String title, String description, LocalDateTime when,
                           String location, String category) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setDateTime(when);
        event.setVenue("Venue for " + title);
        event.setLocation(location);
        event.setCategory(category);
        event.setCapacity(50);
        return eventRepository.save(event);
    }

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        base = LocalDateTime.now().plusDays(10).withNano(0);
        newEvent("Spring Boot Masterclass", "Deep dive into REST APIs", base, "Bengaluru", "Technology");
        newEvent("AI Summit", "Applied machine learning talks", base.plusDays(2), "Hyderabad", "Artificial Intelligence");
        newEvent("Startup Networking Night", "Meet founders and investors", base.plusDays(4), "Bengaluru", "Networking");
    }

    @Test
    void search_withNoFilters_returnsAllOrderedByDate() {
        List<Event> results = eventRepository.search("", "", "", EPOCH);

        assertThat(results).hasSize(3);
        assertThat(results).extracting(Event::getTitle)
                .containsExactly("Spring Boot Masterclass", "AI Summit", "Startup Networking Night");
    }

    @Test
    void search_matchesTitleCaseInsensitively() {
        assertThat(eventRepository.search("spring BOOT", "", "", EPOCH))
                .extracting(Event::getTitle)
                .containsExactly("Spring Boot Masterclass");
    }

    @Test
    void search_matchesDescriptionText() {
        assertThat(eventRepository.search("machine learning", "", "", EPOCH))
                .extracting(Event::getTitle)
                .containsExactly("AI Summit");
    }

    @Test
    void search_filtersByLocationCaseInsensitively() {
        assertThat(eventRepository.search("", "bengaluru", "", EPOCH))
                .extracting(Event::getTitle)
                .containsExactly("Spring Boot Masterclass", "Startup Networking Night");
    }

    @Test
    void search_filtersByCategory() {
        assertThat(eventRepository.search("", "", "Networking", EPOCH))
                .extracting(Event::getTitle)
                .containsExactly("Startup Networking Night");
    }

    @Test
    void search_filtersByFromDateInclusiveOfLaterEvents() {
        assertThat(eventRepository.search("", "", "", base.plusDays(2)))
                .extracting(Event::getTitle)
                .containsExactly("AI Summit", "Startup Networking Night");
    }

    @Test
    void search_combinesFiltersAndReturnsEmptyWhenNothingMatches() {
        assertThat(eventRepository.search("Spring", "Hyderabad", "", EPOCH)).isEmpty();
    }

    @Test
    void search_returnsSingleRowForEventWithMultipleSpeakers() {
        Speaker a = speakerRepository.save(new Speaker("Grace Park", "bio", "Cloud"));
        Speaker b = speakerRepository.save(new Speaker("Leo Martins", "bio", "Web"));
        Event event = newEvent("Joint Workshop", "Two speakers", base.plusDays(6), "Pune", "Technology");
        // Mutable set: Hibernate manages the collection in place on a persistent entity.
        event.setSpeakers(new HashSet<>(Set.of(a, b)));
        eventRepository.saveAndFlush(event);

        // DISTINCT in the query must collapse the join-table fan-out to one row.
        assertThat(eventRepository.search("Joint Workshop", "", "", EPOCH)).hasSize(1);
    }

    @Test
    void findByDateTimeBetween_returnsOnlyEventsInsideTheWindow() {
        List<Event> window = eventRepository.findByDateTimeBetween(base.minusHours(1), base.plusDays(2));

        assertThat(window).extracting(Event::getTitle)
                .containsExactlyInAnyOrder("Spring Boot Masterclass", "AI Summit");
    }
}
