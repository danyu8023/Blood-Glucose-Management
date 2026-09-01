package com.tangan.glucose.service;

import com.tangan.glucose.dto.UpdateUserRequest;
import com.tangan.glucose.dto.UserResponse;
import com.tangan.glucose.entity.UserAccount;
import java.util.UUID;

public interface UserService {
    UserAccount require(UUID userId);
    UserResponse toResponse(UserAccount user);
    UserResponse update(UUID userId, UpdateUserRequest request);
}
