package com.matt.iam.services;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matt.iam.entities.User;
import com.matt.iam.repositories.BlacklistedTokenRepository;
import com.matt.iam.repositories.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Async
    @Transactional
    public void cleanupUserTokensAsync(User user) {
                try {
            log.info("🧹 [ASYNC] Iniciando limpeza de tokens para usuário: {}", user.getEmail());
            
            long expiredTokens = this.refreshTokenRepository.findByUserAndExpiresAtBeforeAndIsRevokedFalse(user, LocalDateTime.now())
                    .stream()
                    .peek(token -> {
                        token.setIsRevoked(true);
                        this.refreshTokenRepository.save(token);
                    })
                    .count();

            long excessTokens = this.refreshTokenRepository.findByUserAndIsRevokedFalseOrderByCreatedAtDesc(user)
                    .stream()
                    .skip(5)
                    .peek(token -> {
                        token.setIsRevoked(true);
                        this.refreshTokenRepository.save(token);
                    })
                    .count();
            
            log.info("✅ [ASYNC] Limpeza concluída: {} tokens expirados, {} tokens em excesso removidos para {}", 
                    expiredTokens, excessTokens, user.getEmail());
        } catch (Exception e) {
            log.error("❌ [ASYNC] Erro na limpeza de tokens para usuário: {}", user.getEmail(), e);
        }
    }

    @Async
    @Transactional
    public void cleanupExpiredBlacklistedTokensAsync() {
        try {
            log.info("🗑️ [ASYNC] Iniciando limpeza de tokens blacklistados expirados");
            this.blacklistedTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("✅ [ASYNC] Limpeza de tokens blacklistados concluída");
        } catch (Exception e) {
            log.error("❌ [ASYNC] Erro na limpeza de tokens blacklistados", e);
        }
    }
} 