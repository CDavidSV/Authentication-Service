package dev.cdavidsv.auth.core.repository;

import dev.cdavidsv.auth.core.model.entity.VerificationChannel;
import dev.cdavidsv.auth.core.model.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    Optional<VerificationCode> findByCodeHashAndChannel(String codeHash, VerificationChannel channel);
}
