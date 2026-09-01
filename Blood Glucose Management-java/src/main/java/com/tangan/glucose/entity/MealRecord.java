package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "meal_records", indexes = @Index(name = "idx_meal_user_time", columnList = "user_id,eaten_at"))
@Getter @Setter @NoArgsConstructor
public class MealRecord {
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserAccount user;
    @Column(name = "meal_type", nullable = false, length = 20) private String mealType;
    @Column(name = "eaten_at", nullable = false) private OffsetDateTime eatenAt;
    @Lob @Column(name = "foods_json", nullable = false) private String foodsJson;
    @Column(name = "carbohydrate_grams", precision = 6, scale = 1) private BigDecimal carbohydrateGrams;
    @Column(length = 500) private String note;
    @Column(nullable = false) private Boolean deleted = false;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }
}
