package com.matt.iam.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.matt.iam.entities.Permission;
import com.matt.iam.entities.Role;
import com.matt.iam.entities.User;
import com.matt.iam.exception.CustomException;
import com.matt.iam.exception.ExceptionMessages;

@Service
public class JwtUtil {
    @Value("${jwt.secret.key}")
    private String secret;

    public String generateAccessToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                .withIssuer("iam-api")
                .withSubject(user.getEmail())
                .withClaim("roles", collectRoles(user))
                .withClaim("permissions", collectPermissions(user))
                .withExpiresAt(generateExpirationDateForAccessToken())
                .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new CustomException(ExceptionMessages.FAILED_TO_CREATE_TOKEN, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String generateRefreshToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                .withIssuer("iam-api")
                .withSubject(user.getEmail())
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("roles", collectRoles(user))
                .withClaim("permissions", collectPermissions(user))
                .withExpiresAt(generateExpirationDateForRefreshToken())
                .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new CustomException(ExceptionMessages.FAILED_TO_CREATE_TOKEN, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                .withIssuer("iam-api")
                .build()
                .verify(token)
                .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private Instant generateExpirationDateForAccessToken() {
        return Instant.now().plusSeconds(900); // 15 min
    }

    private Instant generateExpirationDateForRefreshToken() {
        return Instant.now().plusSeconds(604800); // 7 days
    }

    public LocalDateTime getExpirationDateFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                .withIssuer("iam-api")
                .build()
                .verify(token)
                .getExpiresAt()
                .toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
        } catch (Exception e) {
            throw new CustomException(ExceptionMessages.FAILED_TO_GET_EXPIRATION_TIME, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> collectRoles(User user) {
        return user.getRoles()
            .stream()
            .map(Role::getName)
            .collect(Collectors.toList());
    }

    private List<String> collectPermissions(User user) {
        return user.getRoles()
            .stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getName)
            .distinct()
            .collect(Collectors.toList());
    }
}
