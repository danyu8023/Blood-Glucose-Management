package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "glucose_records", indexes = {
        @Index(name = "idx_glucose_user_time", columnList = "user_id,measured_at"),
        @Index(name = "idx_glucose_user_period", columnList = "user_id,period")
})
@Getter @Setter @NoArgsConstructor
public class GlucoseRecord {
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserAccount user;
    @Column(nullable = false, precision = 5, scale = 1) private BigDecimal value;
    @Column(nullable = false, length = 10) private String unit = "mmol/L";
    @Column(nullable = false, length = 20) private String period;
    @Column(name = "measured_at", nullable = false) private OffsetDateTime measuredAt;
    @Column(nullable = false, length = 20) private String status;
    @Column(length = 500) private String note;
    @Column(nullable = false) private Boolean deleted = false;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }
}
