package dev.cdavidsv.auth.core.repository;

import dev.cdavidsv.auth.core.model.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<VerificationCode, UUID> {
}
