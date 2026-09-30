package dev.cdavidsv.auth.core.repository;

import dev.cdavidsv.auth.core.model.entity.UserMfaMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMfaMethodRepository extends JpaRepository<UserMfaMethod, UUID> {
    List<UserMfaMethod> findAllByUserId(UUID userId);
}
