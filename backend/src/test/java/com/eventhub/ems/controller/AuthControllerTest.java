package com.eventhub.ems.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eventhub.ems.dto.RegisterRequest;
import com.eventhub.ems.dto.UserResponse;
import com.eventhub.ems.exception.ConflictException;
import com.eventhub.ems.model.Role;
import com.eventhub.ems.model.User;
import com.eventhub.ems.security.CustomUserDetailsService;
import com.eventhub.ems.security.JwtAuthenticationFilter;
import com.eventhub.ems.security.JwtService;
import com.eventhub.ems.security.SecurityConfig;
import com.eventhub.ems.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtService jwtService;

    private UserDetails principal(String email) {
        return new org.springframework.security.core.userdetails.User(
                email, "ENCODED", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void register_returns201WithTokenAndUser() throws Exception {
        UserResponse created = new UserResponse(1L, "Jane", "jane@example.com", "USER");
        when(userService.register(any(RegisterRequest.class))).thenReturn(created);
        when(userDetailsService.loadUserByUsername("jane@example.com")).thenReturn(principal("jane@example.com"));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Jane", "jane@example.com", "secret123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("jane@example.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("An account with this email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Jane", "jane@example.com", "secret123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists"));
    }

    @Test
    void register_invalidPayload_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("", "not-an-email", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(userService, never()).register(any());
    }

    @Test
    void login_returnsTokenForValidCredentials() throws Exception {
        when(userDetailsService.loadUserByUsername("jane@example.com")).thenReturn(principal("jane@example.com"));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(userService.getByEmail("jane@example.com"))
                .thenReturn(new User("Jane", "jane@example.com", "ENCODED", Role.USER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.name").value("Jane"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void me_returnsCurrentUser() throws Exception {
        when(userService.getByEmail("jane@example.com"))
                .thenReturn(new User("Jane", "jane@example.com", "ENCODED", Role.USER));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void me_withoutAuthentication_returns401NotN403() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required — please log in"));
    }
}
