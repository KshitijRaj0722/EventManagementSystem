package com.eventhub.ems.config;

import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.Role;
import com.eventhub.ems.model.Speaker;
import com.eventhub.ems.model.User;
import com.eventhub.ems.repository.EventRepository;
import com.eventhub.ems.repository.SpeakerRepository;
import com.eventhub.ems.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seed(UserRepository userRepository,
                           SpeakerRepository speakerRepository,
                           EventRepository eventRepository,
                           PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("Admin", "admin@eventhub.local",
                        passwordEncoder.encode("admin123"), Role.ADMIN));
                userRepository.save(new User("Demo User", "user@eventhub.local",
                        passwordEncoder.encode("user123"), Role.USER));
                log.info("Seeded default users: admin@eventhub.local/admin123, user@eventhub.local/user123");
            }

            if (speakerRepository.count() == 0 && eventRepository.count() == 0) {
                Speaker grace = speakerRepository.save(new Speaker(
                        "Dr. Grace Park", "Cloud architect and keynote speaker.", "Cloud & DevOps"));
                Speaker leo = speakerRepository.save(new Speaker(
                        "Leo Martins", "Full-stack engineer and educator.", "Web Development"));
                Speaker mia = speakerRepository.save(new Speaker(
                        "Mia Chen", "AI researcher focused on applied ML.", "Artificial Intelligence"));

                LocalDateTime base = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0)
                        .withSecond(0).withNano(0);

                Event e1 = new Event();
                e1.setTitle("Spring Boot Masterclass");
                e1.setDescription("A hands-on deep dive into building production-ready REST APIs with Spring Boot.");
                e1.setDateTime(base);
                e1.setVenue("Hall A, Tech Convention Center");
                e1.setLocation("Bengaluru");
                e1.setCategory("Technology");
                e1.setCapacity(100);
                e1.setSpeakers(Set.of(grace, leo));
                eventRepository.save(e1);

                Event e2 = new Event();
                e2.setTitle("AI & Machine Learning Summit");
                e2.setDescription("Explore the latest trends in applied AI with industry leaders.");
                e2.setDateTime(base.plusDays(3).with(LocalTime.of(14, 0)));
                e2.setVenue("Auditorium 2, Innovation Park");
                e2.setLocation("Hyderabad");
                e2.setCategory("Artificial Intelligence");
                e2.setCapacity(200);
                e2.setSpeakers(Set.of(mia));
                eventRepository.save(e2);

                Event e3 = new Event();
                e3.setTitle("Startup Networking Night");
                e3.setDescription("Meet founders, investors and builders. Casual networking and lightning talks.");
                e3.setDateTime(base.plusDays(10).with(LocalTime.of(18, 30)));
                e3.setVenue("Rooftop Lounge, WeWork");
                e3.setLocation("Bengaluru");
                e3.setCategory("Networking");
                e3.setCapacity(50);
                e3.setSpeakers(Set.of(leo));
                eventRepository.save(e3);

                log.info("Seeded {} speakers and {} sample events",
                        speakerRepository.count(), eventRepository.count());
            }
        };
    }
}
