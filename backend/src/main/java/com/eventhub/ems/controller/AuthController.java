package com.eventhub.ems.controller;

import com.eventhub.ems.dto.AuthResponse;
import com.eventhub.ems.dto.DtoMapper;
import com.eventhub.ems.dto.LoginRequest;
import com.eventhub.ems.dto.RegisterRequest;
import com.eventhub.ems.dto.UserResponse;
import com.eventhub.ems.model.User;
import com.eventhub.ems.security.JwtService;
import com.eventhub.ems.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(UserService userService,
                         AuthenticationManager authenticationManager,
                         UserDetailsService userDetailsService,
                         JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.email()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(userDetails);
        User user = userService.getByEmail(request.email());
        return ResponseEntity.ok(new AuthResponse(token, DtoMapper.toUser(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(DtoMapper.toUser(user));
    }
}
