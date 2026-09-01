package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "medication_records", indexes = @Index(name = "idx_med_user_time", columnList = "user_id,taken_at"))
@Getter @Setter @NoArgsConstructor
public class MedicationRecord {
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserAccount user;
    @Column(name = "medication_name", nullable = false, length = 120) private String medicationName;
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal dose;
    @Column(name = "dose_unit", nullable = false, length = 20) private String doseUnit;
    @Column(name = "taken_at") private OffsetDateTime takenAt;
    @Column(name = "scheduled_at") private OffsetDateTime scheduledAt;
    @Column(nullable = false, length = 20) private String status;
    @Column(length = 500) private String note;
    @Column(nullable = false) private Boolean deleted = false;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }
}
