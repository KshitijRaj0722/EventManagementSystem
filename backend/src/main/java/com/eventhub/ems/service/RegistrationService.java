package com.eventhub.ems.service;

import com.eventhub.ems.dto.RegistrationResponse;
import java.util.List;

public interface RegistrationService {

    RegistrationResponse registerForEvent(String userEmail, Long eventId);

    List<RegistrationResponse> getMyRegistrations(String userEmail);

    List<RegistrationResponse> getEventRegistrations(Long eventId);

    RegistrationResponse setAttendance(Long registrationId, boolean attended);

    void cancelRegistration(String userEmail, Long eventId);
}
