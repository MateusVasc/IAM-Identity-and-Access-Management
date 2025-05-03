package com.matt.iam.exception;

public class ExceptionMessages {
    public static final String USER_NOT_FOUND = "User not found";
    public static final String ROLE_NOT_FOUND = "Role not found";
    public static final String FAILED_TO_CREATE_TOKEN = "Error while creating access token";
    public static final String USER_ALREADY_EXISTS = "Email already taken";
    public static final String INVALID_CREDENTIALS = "Invalid credentials were provided";
    public static final String INVALID_TOKEN = "An invalid token was provided";
    public static final String TOKEN_WAS_REVOKED = "The refresh token was revoked";
    public static final String FAILED_TO_GET_EXPIRATION_TIME = "Error while getting token expiration time";
    public static final String ACCOUNT_NOT_ENABLED = "Account is not enabled";
    public static final String ACCOUNT_LOCKED = "Account was temporarily blocked";
    public static final String TOO_MANY_ATTEMPTS = "Too many login attempts. Try again later";
    public static final String TOKEN_EXPIRED = "Refresh token has expired";
    public static final String TOKEN_NOT_BELONG_TO_USER = "Token does not belong to the user";

    private ExceptionMessages() {}
}
