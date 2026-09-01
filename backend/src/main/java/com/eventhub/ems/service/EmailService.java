package com.eventhub.ems.service;

import com.eventhub.ems.model.Event;
import com.eventhub.ems.model.User;

public interface EmailService {

    void sendRegistrationConfirmation(User user, Event event);

    void sendEventReminder(User user, Event event);
}
