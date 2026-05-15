package com.freelance.freelance_platform.identity.service;

import com.freelance.freelance_platform.config.AppProperties;
import com.freelance.freelance_platform.identity.domain.User;
import com.freelance.freelance_platform.shared.exception.BusinessException;
import com.freelance.freelance_platform.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    // Sauvegarder le refresh token dans Redis
    public void save(String token, User user) {
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(
                key,
                user.getId().toString(),
                Duration.ofSeconds(appProperties.jwt().refreshTokenExpiration())
        );
    }

    // Valider et retourner l'userId associé au token
    public UUID validateAndGetUserId(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);

        if (userId == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        return UUID.fromString(userId);
    }

    // Révoquer un refresh token (logout)
    public void revoke(String token) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + token);
    }

    // Révoquer tous les tokens d'un utilisateur (logout partout)
    public void revokeAll(UUID userId) {
        var keys = redisTemplate.keys(REFRESH_TOKEN_PREFIX + "*");
        if (keys != null) {
            keys.forEach(key -> {
                String storedUserId = redisTemplate.opsForValue().get(key);
                if (userId.toString().equals(storedUserId)) {
                    redisTemplate.delete(key);
                }
            });
        }
    }
}
