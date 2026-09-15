package domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Table(name = "qr_login_requests")
@Entity
public class QrLoginRequest {

    public QrLoginRequest() {
    }

    public QrLoginRequest(UUID id, String code_hash, QrLoginStatus status, Instant createdAt, Instant expiresAt, Instant approvedAt) {
        this.id = id;
        this.code_hash = code_hash;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.approvedAt = approvedAt;
    }

    @Id
    @Column(name="id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="code_hash", nullable = false, unique = true)
    private String code_hash;

    @Column(name="status", nullable = false)
    private QrLoginStatus status;

    @Column(name="created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name="expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name="approved_at")
    private Instant approvedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode_hash() {
        return code_hash;
    }

    public void setCode_hash(String code_hash) {
        this.code_hash = code_hash;
    }

    public QrLoginStatus getStatus() {
        return status;
    }

    public void setStatus(QrLoginStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        QrLoginRequest that = (QrLoginRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "QrLoginRequest{" +
                "id=" + id +
                ", code_hash='" + code_hash + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", approvedAt=" + approvedAt +
                '}';
    }
}
