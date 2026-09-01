package com.eventhub.ems.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eventhub.ems.dto.EventRequest;
import com.eventhub.ems.dto.EventResponse;
import com.eventhub.ems.dto.RegistrationResponse;
import com.eventhub.ems.dto.SpeakerRequest;
import com.eventhub.ems.dto.SpeakerResponse;
import com.eventhub.ems.security.CustomUserDetailsService;
import com.eventhub.ems.security.JwtAuthenticationFilter;
import com.eventhub.ems.security.JwtService;
import com.eventhub.ems.security.SecurityConfig;
import com.eventhub.ems.service.EventService;
import com.eventhub.ems.service.RegistrationService;
import com.eventhub.ems.service.SpeakerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Admin controller: CRUD wiring plus the role-based access rules from SecurityConfig. */
@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private SpeakerService speakerService;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private EventRequest eventRequest() {
        return new EventRequest("New Event", "desc", LocalDateTime.of(2030, 5, 1, 10, 0),
                "Venue Z", "Chennai", "Testing", 50, Set.of(3L));
    }

    private EventResponse eventResponse() {
        return new EventResponse(9L, "New Event", "desc", LocalDateTime.of(2030, 5, 1, 10, 0),
                "Venue Z", "Chennai", "Testing", 50, 0L, List.of());
    }

    // ----- Access control -----

    @Test
    void adminEndpoints_rejectAnonymousWith401() throws Exception {
        mockMvc.perform(get("/api/admin/events/1/registrations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_rejectNonAdminWith403() throws Exception {
        mockMvc.perform(get("/api/admin/events/1/registrations"))
                .andExpect(status().isForbidden());

        verify(registrationService, never()).getEventRegistrations(any());
    }

    // ----- Events -----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEvent_returns201() throws Exception {
        when(eventService.create(any(EventRequest.class))).thenReturn(eventResponse());

        mockMvc.perform(post("/api/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.title").value("New Event"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEvent_validatesRequiredFields() throws Exception {
        String invalid = """
                {"title":"","dateTime":null,"venue":"","location":"","category":""}
                """;

        mockMvc.perform(post("/api/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.dateTime").exists());

        verify(eventService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEvent_delegatesToService() throws Exception {
        when(eventService.update(eq(9L), any(EventRequest.class))).thenReturn(eventResponse());

        mockMvc.perform(put("/api/admin/events/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest())))
                .andExpect(status().isOk());

        verify(eventService).update(eq(9L), any(EventRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEvent_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/events/9"))
                .andExpect(status().isNoContent());

        verify(eventService).delete(9L);
    }

    // ----- Speakers -----

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSpeaker_returns201() throws Exception {
        when(speakerService.create(any(SpeakerRequest.class)))
                .thenReturn(new SpeakerResponse(4L, "Grace Park", "bio", "Cloud"));

        mockMvc.perform(post("/api/admin/speakers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SpeakerRequest("Grace Park", "bio", "Cloud"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Grace Park"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSpeaker_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/speakers/4"))
                .andExpect(status().isNoContent());

        verify(speakerService).delete(4L);
    }

    // ----- Attendance -----

    @Test
    @WithMockUser(roles = "ADMIN")
    void eventRegistrations_returnsAttendeeList() throws Exception {
        when(registrationService.getEventRegistrations(1L)).thenReturn(List.of(
                new RegistrationResponse(10L, 1L, "Spring Boot Masterclass",
                        LocalDateTime.of(2030, 5, 1, 10, 0), "Hall A",
                        2L, "Jane", "jane@example.com", LocalDateTime.now(), false)));

        mockMvc.perform(get("/api/admin/events/1/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userEmail").value("jane@example.com"))
                .andExpect(jsonPath("$[0].attended").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setAttendance_passesQueryFlagToService() throws Exception {
        when(registrationService.setAttendance(10L, true)).thenReturn(
                new RegistrationResponse(10L, 1L, "Spring Boot Masterclass",
                        LocalDateTime.of(2030, 5, 1, 10, 0), "Hall A",
                        2L, "Jane", "jane@example.com", LocalDateTime.now(), true));

        mockMvc.perform(put("/api/admin/registrations/10/attendance").param("attended", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attended").value(true));

        verify(registrationService).setAttendance(10L, true);
    }
}
