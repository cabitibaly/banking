package com.jiyuu.banking.service;

import com.jiyuu.banking.config.JwtUtils;
import com.jiyuu.banking.entity.RefreshToken;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.exception.ExpiredRefreshTokenException;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;

    public Map<String, String> newTokenPair(String refreshTokenRequest) {
        RefreshToken refreshToken = this.refreshTokenRepository.findByValue(refreshTokenRequest)
                .orElseThrow(() -> new ResourceNotFoundException("Veuillez vous reconnecter"));

        if (refreshToken.isExpired() || refreshToken.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException("Veuillez vous reconnecter");
        }

        refreshToken.setExpired(true);
        this.refreshTokenRepository.save(refreshToken);

        return jwtUtils.generateToken(refreshToken.getUser());
    }

    public Map<String, String> createTokenPair(User user) {
        return jwtUtils.generateToken(user);
    }

    @Transactional
    public void expireAllUserRefreshToken(Long userId) {
        this.refreshTokenRepository.expiredAllByUserId(userId);
    }
}
