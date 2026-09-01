package com.eventhub.ems.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eventhub.ems.dto.EventResponse;
import com.eventhub.ems.dto.SpeakerResponse;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.security.CustomUserDetailsService;
import com.eventhub.ems.security.JwtAuthenticationFilter;
import com.eventhub.ems.security.JwtService;
import com.eventhub.ems.security.SecurityConfig;
import com.eventhub.ems.service.EventService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/** Controller-layer tests: request mapping, param binding and error translation. */
@WebMvcTest(controllers = EventController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    // Collaborators of the imported security chain — not exercised here.
    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private EventResponse sampleResponse() {
        return new EventResponse(
                1L, "Spring Boot Masterclass", "Deep dive",
                LocalDateTime.of(2030, 5, 1, 10, 0),
                "Hall A", "Bengaluru", "Technology", 100, 7L,
                List.of(new SpeakerResponse(3L, "Grace Park", "bio", "Cloud")));
    }

    @Test
    void listEvents_isPublicAndReturnsEvents() throws Exception {
        when(eventService.search(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Boot Masterclass"))
                .andExpect(jsonPath("$[0].registeredCount").value(7))
                .andExpect(jsonPath("$[0].speakers[0].name").value("Grace Park"));
    }

    @Test
    void listEvents_bindsSearchLocationCategoryAndFromDate() throws Exception {
        when(eventService.search(eq("spring"), eq("Bengaluru"), eq("Technology"), eq(LocalDate.of(2030, 1, 15))))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/events")
                        .param("search", "spring")
                        .param("location", "Bengaluru")
                        .param("category", "Technology")
                        .param("fromDate", "2030-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(eventService).search("spring", "Bengaluru", "Technology", LocalDate.of(2030, 1, 15));
    }

    @Test
    void listEvents_rejectsMalformedFromDate() throws Exception {
        mockMvc.perform(get("/api/events").param("fromDate", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEvent_isPublicAndReturnsDetail() throws Exception {
        when(eventService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue").value("Hall A"))
                .andExpect(jsonPath("$.capacity").value(100));
    }

    @Test
    void getEvent_missing_isTranslatedTo404() throws Exception {
        when(eventService.getById(99L)).thenThrow(new ResourceNotFoundException("Event not found: 99"));

        mockMvc.perform(get("/api/events/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found: 99"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
