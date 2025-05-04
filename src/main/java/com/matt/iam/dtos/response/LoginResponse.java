package com.matt.iam.dtos.response;

import java.util.UUID;

public record LoginResponse(
    UUID id,
    String token,
    String refreshToken
) {
}
