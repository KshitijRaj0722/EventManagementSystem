package com.event.eventmanagement;

import com.event.eventmanagement.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EventmanagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventmanagementApplication.class, args);
    }

    // Create admin at startup
    @Bean
    CommandLineRunner run(UserService userService) {
        return args -> {
            userService.createAdminIfNotExists();
        };
    }
}
