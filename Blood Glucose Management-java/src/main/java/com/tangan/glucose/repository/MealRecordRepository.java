package com.tangan.glucose.repository;

import com.tangan.glucose.entity.MealRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.*;

public interface MealRecordRepository extends JpaRepository<MealRecord, UUID> {
    Page<MealRecord> findByUserIdAndDeletedFalseAndEatenAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    Optional<MealRecord> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
    long countByUserIdAndDeletedFalseAndEatenAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to);
    boolean existsByUserIdAndMealTypeAndDeletedFalseAndEatenAtBetween(UUID userId, String mealType, OffsetDateTime from, OffsetDateTime to);
    @Modifying(clearAutomatically = true)
    @Query("update MealRecord m set m.deleted=true, m.updatedAt=:now where m.id=:id and m.user.id=:uid")
    int softDelete(@Param("id") UUID id, @Param("uid") UUID userId, @Param("now") OffsetDateTime now);
}
