package com.ticketing.user.service;

import com.ticketing.common.exception.ConflictException;
import com.ticketing.common.exception.NotFoundException;
import com.ticketing.common.security.JwtService;
import com.ticketing.user.api.AuthResponse;
import com.ticketing.user.api.LoginRequest;
import com.ticketing.user.api.RegisterRequest;
import com.ticketing.user.api.UserProfileResponse;
import com.ticketing.user.domain.UserAccount;
import com.ticketing.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return Mono.fromCallable(() -> {
                    userRepository.findByEmail(request.email()).ifPresent(existing -> {
                        throw new ConflictException("User already exists with email " + request.email());
                    });
                    UserAccount account = new UserAccount();
                    account.setEmail(request.email().toLowerCase());
                    account.setPasswordHash(passwordEncoder.encode(request.password()));
                    account.setFullName(request.fullName());
                    account.setRole(request.role());
                    UserAccount saved = userRepository.save(account);
                    String token = jwtService.generateToken(saved.getId().toString(), List.of(saved.getRole().name()));
                    return new AuthResponse(token, toProfile(saved));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return Mono.fromCallable(() -> userRepository.findByEmail(request.email().toLowerCase())
                        .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                        .map(user -> new AuthResponse(
                                jwtService.generateToken(user.getId().toString(), List.of(user.getRole().name())),
                                toProfile(user)))
                        .orElseThrow(() -> new NotFoundException("Invalid credentials")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UserProfileResponse> currentUser(UUID userId) {
        return Mono.fromCallable(() -> userRepository.findById(userId)
                        .map(this::toProfile)
                        .orElseThrow(() -> new NotFoundException("User not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<UserProfileResponse>> listUsers() {
        return Mono.fromCallable(() -> userRepository.findAll().stream().map(this::toProfile).toList())
                .subscribeOn(Schedulers.boundedElastic());
    }

    private UserProfileResponse toProfile(UserAccount account) {
        return new UserProfileResponse(
                account.getId(),
                account.getEmail(),
                account.getFullName(),
                account.getRole(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
