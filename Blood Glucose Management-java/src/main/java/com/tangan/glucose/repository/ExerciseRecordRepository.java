package com.tangan.glucose.repository;

import com.tangan.glucose.entity.ExerciseRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.*;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, UUID> {
    Page<ExerciseRecord> findByUserIdAndDeletedFalseAndStartedAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    Optional<ExerciseRecord> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
    @Query("select coalesce(sum(e.durationMinutes),0) from ExerciseRecord e where e.user.id=:uid and e.deleted=false and e.startedAt between :from and :to")
    Integer totalMinutes(@Param("uid") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    @Modifying(clearAutomatically = true)
    @Query("update ExerciseRecord e set e.deleted=true, e.updatedAt=:now where e.id=:id and e.user.id=:uid")
    int softDelete(@Param("id") UUID id, @Param("uid") UUID userId, @Param("now") OffsetDateTime now);
}
