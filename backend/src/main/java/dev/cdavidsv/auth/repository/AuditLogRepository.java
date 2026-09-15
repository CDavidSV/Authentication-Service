package dev.cdavidsv.auth.repository;

import dev.cdavidsv.auth.domain.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<VerificationCode, UUID> {
}
