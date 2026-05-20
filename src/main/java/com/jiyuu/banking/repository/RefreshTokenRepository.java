package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByValue(String value);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.expired = true WHERE r.user.idUser = :userId")
    void expiredAllByUserId(Long userId);
}
