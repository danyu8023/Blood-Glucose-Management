package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "user_settings")
@Getter @Setter @NoArgsConstructor
public class UserSetting {
    @Id @Column(name = "user_id") private UUID userId;
    @OneToOne(fetch = FetchType.LAZY) @MapsId @JoinColumn(name = "user_id") private UserAccount user;
    @Column(nullable = false) private Boolean glucoseReminder = true;
    @Column(nullable = false) private Boolean medicationReminder = true;
    @Column(nullable = false) private Boolean familyAlert = true;
    @Column(nullable = false) private Boolean autoSync = true;
    @Column(nullable = false) private Boolean faceIdUnlock = false;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist @PreUpdate void touch() { updatedAt = OffsetDateTime.now(); }
}
