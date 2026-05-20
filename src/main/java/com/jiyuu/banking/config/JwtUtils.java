package com.jiyuu.banking.config;

import com.jiyuu.banking.entity.RefreshToken;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.exception.ExpiredRefreshTokenException;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.repository.RefreshTokenRepository;
import com.jiyuu.banking.service.TokenStoreService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtils {
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenStoreService tokenStoreService;

    @Value("${secret-key}")
    private String secretKey;

    @Value("${expiration-time}")
    private long expirationTime;

    public JwtUtils(RefreshTokenRepository refreshTokenRepository, TokenStoreService tokenStoreService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenStoreService = tokenStoreService;
    }

    public Map<String, String> generateToken(UserDetails userDetails) {
        final String jit = UUID.randomUUID().toString();
        User user = (User) userDetails;

        Map<String, String> claims = Map.of(
                "username", user.getEmail(),
                "role", user.getRole().getLabel().name(),
                "jti", jit
        );

        String accessToken = createToken(claims, userDetails.getUsername());

        RefreshToken refreshToken = RefreshToken.builder()
                .value(UUID.randomUUID().toString())
                .expired(false)
                .expirationTime(LocalDateTime.now().plusDays(7))
                .user(user)
                .build();

        this.refreshTokenRepository.save(refreshToken);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getValue()
        );
    }

    private String createToken(Map<String, String> claims, String username) {
        final long currentTime = System.currentTimeMillis();
        final long expirationTimeMillis = currentTime + expirationTime * 60 * 1000;

        return Jwts.builder()
                .setId(claims.get("jti"))
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expirationTimeMillis))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = secretKey.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationTime(token).before(new Date());
    }

    private Date extractExpirationTime(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String getJit(String token) {
        return extractClaim(token, Claims::getId);
    }

    public long getRemainingTime(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        return Math.max(remainingTime, 0);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void logout(String accessToken, String refreshT) {
        RefreshToken refreshToken = this.refreshTokenRepository.findByValue(refreshT)
                .orElseThrow(() -> new ResourceNotFoundException("Le refresh token n'existe pas"));

        if(refreshToken.isExpired() || refreshToken.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException("Impossible de valider votre requête car le refresh token a expiré");
        }

        refreshToken.setExpired(true);
        this.refreshTokenRepository.save(refreshToken);

        this.tokenStoreService.blacklistToken(accessToken, this.getRemainingTime(accessToken));
    }
}
