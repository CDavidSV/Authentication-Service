package dev.cdavidsv.auth.repository;

import dev.cdavidsv.auth.domain.entity.VerificationChannel;
import dev.cdavidsv.auth.domain.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    Optional<VerificationCode> findByCodeHashAndChannel(String codeHash, VerificationChannel channel);
}
