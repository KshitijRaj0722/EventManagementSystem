package com.eventhub.ems.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eventhub.ems.dto.RegistrationResponse;
import com.eventhub.ems.exception.ConflictException;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.security.CustomUserDetailsService;
import com.eventhub.ems.security.JwtAuthenticationFilter;
import com.eventhub.ems.security.JwtService;
import com.eventhub.ems.security.SecurityConfig;
import com.eventhub.ems.service.RegistrationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RegistrationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private RegistrationResponse response(boolean attended) {
        return new RegistrationResponse(10L, 1L, "Spring Boot Masterclass",
                LocalDateTime.of(2030, 5, 1, 10, 0), "Hall A",
                2L, "Jane", "jane@example.com", LocalDateTime.now(), attended);
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void register_usesAuthenticatedPrincipalAndReturns201() throws Exception {
        when(registrationService.registerForEvent("jane@example.com", 1L)).thenReturn(response(false));

        // CSRF is disabled in SecurityConfig, so no csrf() post-processor is needed.
        mockMvc.perform(post("/api/events/1/register"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.userEmail").value("jane@example.com"));

        verify(registrationService).registerForEvent("jane@example.com", 1L);
    }

    @Test
    void register_anonymous_returns401() throws Exception {
        mockMvc.perform(post("/api/events/1/register"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void register_whenAlreadyRegistered_returns409() throws Exception {
        when(registrationService.registerForEvent("jane@example.com", 1L))
                .thenThrow(new ConflictException("You are already registered for this event"));

        mockMvc.perform(post("/api/events/1/register"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You are already registered for this event"));
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void register_whenEventFull_returns409() throws Exception {
        when(registrationService.registerForEvent("jane@example.com", 1L))
                .thenThrow(new ConflictException("This event is full"));

        mockMvc.perform(post("/api/events/1/register"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This event is full"));
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void cancel_returns204() throws Exception {
        mockMvc.perform(delete("/api/events/1/register"))
                .andExpect(status().isNoContent());

        verify(registrationService).cancelRegistration("jane@example.com", 1L);
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void cancel_whenNotRegistered_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("You are not registered for this event"))
                .when(registrationService).cancelRegistration("jane@example.com", 1L);

        mockMvc.perform(delete("/api/events/1/register"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void myRegistrations_returnsList() throws Exception {
        when(registrationService.getMyRegistrations("jane@example.com"))
                .thenReturn(List.of(response(true)));

        mockMvc.perform(get("/api/registrations/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventTitle").value("Spring Boot Masterclass"))
                .andExpect(jsonPath("$[0].attended").value(true));
    }

    @Test
    void myRegistrations_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/api/registrations/me"))
                .andExpect(status().isUnauthorized());
    }
}
