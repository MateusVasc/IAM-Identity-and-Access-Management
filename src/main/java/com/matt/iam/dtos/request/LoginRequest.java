package com.matt.iam.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Email is a required field")
    @Email(message = "Invalid email")
    String email,

    @NotBlank(message = "Password is a required field")
    @Size(min = 8, message = "Password must have at least 8 chars")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
    message = "Password must include an uppercase letter, a lowercase letter, a number and a special char")
    String password
) { 
}
