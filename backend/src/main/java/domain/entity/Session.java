package domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.net.Inet4Address;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Table(name = "sessions")
@Entity
public class Session {

    public Session() {
    }

    public Session(UUID id, User user, String refreshTokenHash, Inet4Address ipAddress, String deviceName, String os, String platform, String location, Instant createdAt, Instant updatedAt, Instant revokedAt) {
        this.id = id;
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.ipAddress = ipAddress;
        this.deviceName = deviceName;
        this.os = os;
        this.platform = platform;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.revokedAt = revokedAt;
    }

    @Id
    @Column(name="id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false, nullable = false)
    private User user;

    @Column(name="refresh_token_hash", nullable = false, unique = true)
    private String refreshTokenHash;

    @Column(name="ip_address", nullable = false, updatable = false)
    private Inet4Address ipAddress;

    @Column(name="device_name", nullable = false)
    private String deviceName;

    @Column(name="os", nullable = false)
    private String os;

    @Column(name="platform", nullable = false)
    private String platform;

    @Column(name="location", nullable = false)
    private String location;

    @Column(name="created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name="updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name="revoked_at")
    private Instant revokedAt;

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

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public Inet4Address getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(Inet4Address ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(id, session.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", user=" + user +
                ", refreshTokenHash='" + refreshTokenHash + '\'' +
                ", ipAddress=" + ipAddress +
                ", deviceName='" + deviceName + '\'' +
                ", os='" + os + '\'' +
                ", platform='" + platform + '\'' +
                ", location='" + location + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", revokedAt=" + revokedAt +
                '}';
    }
}
