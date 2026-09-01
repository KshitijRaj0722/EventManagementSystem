package com.eventhub.ems.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventhub.ems.dto.RegisterRequest;
import com.eventhub.ems.dto.UserResponse;
import com.eventhub.ems.exception.ConflictException;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.model.Role;
import com.eventhub.ems.model.User;
import com.eventhub.ems.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_savesUserWithEncodedPasswordAndUserRole() {
        RegisterRequest request = new RegisterRequest("Jane", "jane@example.com", "secret123");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("jane@example.com");
        assertThat(response.role()).isEqualTo(Role.USER.name());
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void register_throwsConflictWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Jane", "jane@example.com", "secret123");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getByEmail_returnsUserWhenFound() {
        User user = new User("Jane", "jane@example.com", "ENCODED", Role.USER);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        assertThat(userService.getByEmail("jane@example.com")).isSameAs(user);
    }

    @Test
    void getByEmail_throwsWhenMissing() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("ghost@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
