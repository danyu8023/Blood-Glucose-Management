package com.tangan.glucose.repository;

import com.tangan.glucose.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByRefreshTokenHashAndRevokedFalse(String hash);
    @Modifying(clearAutomatically = true)
    @Query("update AuthSession s set s.revoked = true where s.user.id = :userId and s.revoked = false")
    int revokeAllByUserId(@Param("userId") UUID userId);
    void deleteByUserId(UUID userId);
}
