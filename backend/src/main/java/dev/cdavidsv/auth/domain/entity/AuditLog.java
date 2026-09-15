package dev.cdavidsv.auth.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.net.Inet4Address;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Table(name = "audit_logs")
@Entity
public class AuditLog {

    public AuditLog() {
    }

    public AuditLog(UUID id, User user, Session session, AuditEvent event, Inet4Address ipAddress, String userAgent, String metadata, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.session = session;
        this.event = event;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    @Id
    @Column(name="id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", referencedColumnName = "id")
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(name="event", nullable = false, updatable = false)
    private AuditEvent event;

    @Column(name="ip_address", nullable = true, updatable = false)
    private Inet4Address ipAddress;

    @Column(name="user_agent", nullable = true, updatable = false)
    private String userAgent;

    @Column(name="metadata", nullable = true, updatable = false)
    private String metadata;

    @Column(name="created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

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

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public AuditEvent getEvent() {
        return event;
    }

    public void setEvent(AuditEvent event) {
        this.event = event;
    }

    public Inet4Address getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(Inet4Address ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", user=" + user +
                ", session=" + session +
                ", event=" + event +
                ", ipAddress=" + ipAddress +
                ", userAgent='" + userAgent + '\'' +
                ", metadata='" + metadata + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
