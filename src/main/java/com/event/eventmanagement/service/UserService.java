package com.event.eventmanagement.service;

import com.event.eventmanagement.model.User;
import com.event.eventmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;   // ✅ IMPORTANT
import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // IMPORTANT
        user.setRole("ROLE_USER");

        userRepository.save(user);
    }

    public void createAdminIfNotExists() {

        if (userRepository.findByUsername("admin").isEmpty()) {

            User admin = new User();

            admin.setUsername("admin");

            // IMPORTANT
            admin.setPassword(passwordEncoder.encode("admin123"));

            // IMPORTANT
            admin.setRole("ROLE_ADMIN");

            admin.setName("Administrator");

            userRepository.save(admin);

            System.out.println("✅ Admin created");
        }
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
