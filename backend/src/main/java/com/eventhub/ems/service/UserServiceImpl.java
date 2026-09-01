package com.eventhub.ems.service;

import com.eventhub.ems.dto.DtoMapper;
import com.eventhub.ems.dto.RegisterRequest;
import com.eventhub.ems.dto.UserResponse;
import com.eventhub.ems.exception.ConflictException;
import com.eventhub.ems.exception.ResourceNotFoundException;
import com.eventhub.ems.model.Role;
import com.eventhub.ems.model.User;
import com.eventhub.ems.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER);
        return DtoMapper.toUser(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
