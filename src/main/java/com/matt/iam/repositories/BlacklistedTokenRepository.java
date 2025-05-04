package com.matt.iam.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matt.iam.entities.BlacklistedToken;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
    boolean existsByToken(String token);
}
