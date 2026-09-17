package dev.cdavidsv.auth.core.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Table(name = "verification_codes")
@Entity
public class VerificationCode {
    public VerificationCode() {
    }

    public VerificationCode(UUID id, User user, VerificationChannel channel, VerificationPurpose verificationPurpose, String destination, String codeHash, Instant createdAt, Instant expiresAt, Instant consumedAt) {
        this.id = id;
        this.user = user;
        this.channel = channel;
        this.verificationPurpose = verificationPurpose;
        this.destination = destination;
        this.codeHash = codeHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
    }

    @Id
    @Column(name="id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false, nullable = false)
    private User user;

    @Column(name="verification_channel", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private VerificationChannel channel;

    @Column(name="verification_purpose", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private VerificationPurpose verificationPurpose;

    @Column(name="destination", nullable = false, updatable = false)
    private String destination;

    @Column(name="code_hash", nullable = false, updatable = false)
    private String codeHash;

    @Column(name="created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name="expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name="consumed_at")
    private Instant consumedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VerificationChannel getChannel() {
        return channel;
    }

    public void setChannel(VerificationChannel channel) {
        this.channel = channel;
    }

    public VerificationPurpose getVerificationPurpose() {
        return verificationPurpose;
    }

    public void setVerificationPurpose(VerificationPurpose verificationPurpose) {
        this.verificationPurpose = verificationPurpose;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
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

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VerificationCode that = (VerificationCode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "VerificationCode{" +
                "id=" + id +
                ", user=" + user +
                ", channel=" + channel +
                ", verificationPurpose=" + verificationPurpose +
                ", destination='" + destination + '\'' +
                ", codeHash='" + codeHash + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", consumedAt=" + consumedAt +
                '}';
    }
}
