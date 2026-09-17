package dev.cdavidsv.auth.core.repository;

import dev.cdavidsv.auth.core.model.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository  extends JpaRepository<Session, UUID> {
    Optional<Session> findByCurrentRefreshTokenHash(String refreshTokenHash);
    Optional<Session> findByPreviousRefreshTokenHash(String previousRefreshTokenHash);

    @Modifying
    @Query("UPDATE Session s SET s.revokedAt = CURRENT_TIMESTAMP, s.previousRefreshTokenHash = NULL, s.currentRefreshTokenHash = NULL WHERE s.user.id = :userId AND s.revokedAt IS NULL")
    void revokeAllByUserId(UUID userId);
}
