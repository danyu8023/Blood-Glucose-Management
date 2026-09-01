package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "exercise_records", indexes = @Index(name = "idx_exercise_user_time", columnList = "user_id,started_at"))
@Getter @Setter @NoArgsConstructor
public class ExerciseRecord {
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserAccount user;
    @Column(name = "exercise_type", nullable = false, length = 30) private String exerciseType;
    @Column(name = "started_at", nullable = false) private OffsetDateTime startedAt;
    @Column(name = "duration_minutes", nullable = false) private Integer durationMinutes;
    @Column(nullable = false, length = 20) private String intensity;
    @Column(name = "before_glucose", precision = 5, scale = 1) private BigDecimal beforeGlucose;
    @Column(name = "after_glucose", precision = 5, scale = 1) private BigDecimal afterGlucose;
    @Column(length = 500) private String note;
    @Column(nullable = false) private Boolean deleted = false;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }
}
