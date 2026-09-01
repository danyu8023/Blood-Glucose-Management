package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "family_connections")
@Getter @Setter @NoArgsConstructor
public class FamilyConnection {
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserAccount user;
    @Column(nullable = false, length = 120) private String contact;
    @Column(nullable = false, length = 30) private String relationship;
    @Column(name = "permissions_json", nullable = false, length = 500) private String permissionsJson;
    @Column(nullable = false, length = 20) private String status = "pending";
    @Column(name = "expires_at", nullable = false) private OffsetDateTime expiresAt;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); }
}
