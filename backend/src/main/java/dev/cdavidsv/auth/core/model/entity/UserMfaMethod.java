package dev.cdavidsv.auth.core.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Table(name="user_mfa_methods")
@Entity
public class UserMfaMethod {
    public UserMfaMethod() {
    }

    public UserMfaMethod(UUID id, User user, MfaMethod mfaMethod, Instant addedAt) {
        this.id = id;
        this.user = user;
        this.mfaMethod = mfaMethod;
        this.addedAt = addedAt;
    }

    @Id
    @Column(name="id", unique=true, nullable=false, updatable=false)
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable=false, nullable=false)
    private User user;

    @Column(name="mfa_method", nullable=false, updatable=false)
    @Enumerated(EnumType.STRING)
    private MfaMethod mfaMethod;

    @Column(name="added_at", updatable=false, nullable=false)
    @CreationTimestamp
    private Instant addedAt;

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

    public MfaMethod getMfaMethod() {
        return mfaMethod;
    }

    public void setMfaMethod(MfaMethod mfaMethod) {
        this.mfaMethod = mfaMethod;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserMfaMethod that = (UserMfaMethod) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UserMfaMethods{" +
                "id=" + id +
                ", user=" + user +
                ", mfaMethod=" + mfaMethod +
                ", addedAt=" + addedAt +
                '}';
    }
}
