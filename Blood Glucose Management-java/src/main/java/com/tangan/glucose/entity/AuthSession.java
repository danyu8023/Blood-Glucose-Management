package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions")
@Getter @Setter @NoArgsConstructor
public class AuthSession {
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserAccount user;
    @Column(name = "refresh_token_hash", nullable = false, length = 255) private String refreshTokenHash;
    @Column(name = "device_name", length = 120) private String deviceName;
    @Column(name = "expires_at", nullable = false) private OffsetDateTime expiresAt;
    @Column(nullable = false) private Boolean revoked = false;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); }
}
