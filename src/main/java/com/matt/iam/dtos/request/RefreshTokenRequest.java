package com.matt.iam.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "RefreshToken is a required field")
    String refreshToken
) {
}
