package dev.cdavidsv.auth.core.repository;

import dev.cdavidsv.auth.core.model.entity.QrLoginRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrLoginRequestRepository extends JpaRepository<QrLoginRequest, UUID> {
    Optional<QrLoginRequest> findByCodeHash(String codeHash);
}
