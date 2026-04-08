package com.ticketing.user.api;

import com.ticketing.common.model.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(@Email @NotBlank String email,
                             @NotBlank String password,
                             @NotBlank String fullName,
                             @NotNull RoleType role) {
}
