package com.jiyuu.banking.service;

import com.jiyuu.banking.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class TokenStoreService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String PASSWORD_RESET_PREFIX = "passwordReset:";


    public TokenStoreService(@Qualifier("tokenRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generatePasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                PASSWORD_RESET_PREFIX + token,
                email,
                Duration.ofMinutes(5)
        );
        return token;
    }

    public String validatePasswordResetToken(String token) {
        String email = redisTemplate.opsForValue().get(PASSWORD_RESET_PREFIX + token);
        if (email == null) {
            throw new ValidationException("Token invalide ou expiré");
        }

        return email;
    }

    public void deletePasswordResetToken(String token) {
        redisTemplate.delete(PASSWORD_RESET_PREFIX + token);
    }

    public void blacklistToken(String token, long remainingSeconds) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "revoked",
                Duration.ofSeconds(remainingSeconds)
        );
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
