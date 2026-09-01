package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_account", columnNames = "account"))
@Getter @Setter @NoArgsConstructor
public class UserAccount {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;
    @Column(nullable = false, length = 60) private String name;
    @Column(nullable = false, length = 120) private String account;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(length = 30) private String phone;
    @Column(name = "diabetes_type", length = 20) private String diabetesType = "type2";
    @Column(name = "target_min", precision = 5, scale = 1, nullable = false) private BigDecimal targetMin = new BigDecimal("4.4");
    @Column(name = "target_max", precision = 5, scale = 1, nullable = false) private BigDecimal targetMax = new BigDecimal("7.8");
    @Column(name = "doctor_name", length = 60) private String doctorName = "李医生";
    @Column(name = "doctor_clinic", length = 120) private String doctorClinic = "上海市第一人民医院";
    @Column(nullable = false, length = 50) private String timezone = "Asia/Shanghai";
    @Column(nullable = false) private Boolean active = true;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }
}
