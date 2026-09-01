package com.eventhub.ems.service;

import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.User;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy 'at' HH:mm");

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String from;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${app.mail.from:no-reply@eventhub.local}") String from) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.from = from;
    }

    @Override
    public void sendRegistrationConfirmation(User user, Event event) {
        String subject = "Registration confirmed: " + event.getTitle();
        String body = """
                Hi %s,

                Your registration for "%s" is confirmed.

                When:  %s
                Venue: %s, %s

                We look forward to seeing you there!

                — EventHub
                """.formatted(
                user.getName(),
                event.getTitle(),
                event.getDateTime().format(FMT),
                event.getVenue(),
                event.getLocation());
        send(user.getEmail(), subject, body);
    }

    @Override
    public void sendEventReminder(User user, Event event) {
        String subject = "Reminder: " + event.getTitle() + " is coming up";
        String body = """
                Hi %s,

                This is a reminder that "%s" is happening soon.

                When:  %s
                Venue: %s, %s

                See you there!

                — EventHub
                """.formatted(
                user.getName(),
                event.getTitle(),
                event.getDateTime().format(FMT),
                event.getVenue(),
                event.getLocation());
        send(user.getEmail(), subject, body);
    }

    private void send(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[email disabled] To: {} | Subject: {}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception ex) {
            // Never fail the request because of email problems.
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
