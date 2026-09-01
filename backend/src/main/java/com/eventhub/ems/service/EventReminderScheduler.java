package com.eventhub.ems.service;

import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.Registration;
import com.eventhub.ems.repository.EventRepository;
import com.eventhub.ems.repository.RegistrationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends reminder emails to attendees of events happening within the next 24 hours.
 * Runs hourly. Email sending itself is a no-op log unless app.mail.enabled=true.
 */
@Component
public class EventReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventReminderScheduler.class);

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EmailService emailService;

    public EventReminderScheduler(EventRepository eventRepository,
                                  RegistrationRepository registrationRepository,
                                  EmailService emailService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "${app.reminder.cron:0 0 * * * *}")
    @Transactional(readOnly = true)
    public void sendUpcomingEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);
        List<Event> upcoming = eventRepository.findByDateTimeBetween(now, in24h);
        if (upcoming.isEmpty()) {
            return;
        }
        log.info("Sending reminders for {} upcoming event(s)", upcoming.size());
        for (Event event : upcoming) {
            for (Registration reg : registrationRepository.findByEvent(event)) {
                emailService.sendEventReminder(reg.getUser(), event);
            }
        }
    }
}
