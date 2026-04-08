package com.ticketing.user.api;

public record AuthResponse(String token, UserProfileResponse user) {
}
