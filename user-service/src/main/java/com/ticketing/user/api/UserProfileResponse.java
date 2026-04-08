package com.ticketing.user.api;

import com.ticketing.common.model.RoleType;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(UUID id,
                                  String email,
                                  String fullName,
                                  RoleType role,
                                  boolean active,
                                  Instant createdAt,
                                  Instant updatedAt) {
}
