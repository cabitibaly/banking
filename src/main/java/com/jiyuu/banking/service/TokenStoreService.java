package com.jiyuu.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class TokenStoreService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX     = "blacklist:";


    public TokenStoreService(@Qualifier("tokenRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
