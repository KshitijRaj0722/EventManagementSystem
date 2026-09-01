package com.eventhub.ems.service;

import com.eventhub.ems.dto.RegisterRequest;
import com.eventhub.ems.dto.UserResponse;
import com.eventhub.ems.model.User;

public interface UserService {

    UserResponse register(RegisterRequest request);

    User getByEmail(String email);
}
