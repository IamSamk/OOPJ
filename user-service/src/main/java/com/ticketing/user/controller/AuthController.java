package com.ticketing.user.controller;

import com.ticketing.common.api.ApiResponse;
import com.ticketing.user.api.AuthResponse;
import com.ticketing.user.api.LoginRequest;
import com.ticketing.user.api.RegisterRequest;
import com.ticketing.user.api.UserProfileResponse;
import com.ticketing.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("User registered", response, null)));
    }

    @PostMapping("/auth/login")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Login successful", response, null)));
    }

    @GetMapping("/users/me")
    public Mono<ResponseEntity<ApiResponse<UserProfileResponse>>> currentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return userService.currentUser(userId)
                .map(profile -> ResponseEntity.ok(ApiResponse.success("Current user", profile, null)));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<List<UserProfileResponse>>>> listUsers() {
        return userService.listUsers()
                .map(users -> ResponseEntity.ok(ApiResponse.success("Users fetched", users, null)));
    }
}
