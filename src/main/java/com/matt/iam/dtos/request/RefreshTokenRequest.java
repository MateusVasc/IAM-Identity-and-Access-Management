package com.matt.iam.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    String accessToken,

    @NotBlank(message = "RefreshToken is a required field")
    String refreshToken
) {
}
