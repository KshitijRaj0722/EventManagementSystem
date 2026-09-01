package com.eventhub.ems.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.eventhub.ems.model.Role;
import com.eventhub.ems.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.save(new User("Jane", "jane@example.com", "ENCODED", Role.USER));
    }

    @Test
    void findByEmail_returnsTheUser() {
        assertThat(userRepository.findByEmail("jane@example.com"))
                .get()
                .extracting(User::getName, User::getRole)
                .containsExactly("Jane", Role.USER);
    }

    @Test
    void findByEmail_isEmptyForUnknownAddress() {
        assertThat(userRepository.findByEmail("ghost@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_reflectsPresence() {
        assertThat(userRepository.existsByEmail("jane@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("ghost@example.com")).isFalse();
    }

    @Test
    void emailIsUnique() {
        assertThatThrownBy(() -> userRepository
                .saveAndFlush(new User("Impostor", "jane@example.com", "ENCODED", Role.USER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void adminRoleIsPersistedAsString() {
        User admin = userRepository.saveAndFlush(
                new User("Admin", "admin@example.com", "ENCODED", Role.ADMIN));

        assertThat(userRepository.findById(admin.getId()))
                .get()
                .extracting(User::getRole)
                .isEqualTo(Role.ADMIN);
    }
}
