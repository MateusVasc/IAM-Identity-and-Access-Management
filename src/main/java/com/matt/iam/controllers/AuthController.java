package com.matt.iam.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.matt.iam.dtos.request.LoginRequest;
import com.matt.iam.dtos.request.LogoutRequest;
import com.matt.iam.dtos.request.RefreshTokenRequest;
import com.matt.iam.dtos.request.RegisterRequest;
import com.matt.iam.dtos.response.LoginResponse;
import com.matt.iam.services.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        this.authService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody(required = false) LogoutRequest logoutRequest
    ) {
        if (accessToken == null || !accessToken.startsWith("Bearer ") || accessToken.length() <= 7) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        String token = accessToken.substring(7);
        String refreshToken = logoutRequest != null ? logoutRequest.refreshToken() : null;
        
        this.authService.logout(token, refreshToken);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
