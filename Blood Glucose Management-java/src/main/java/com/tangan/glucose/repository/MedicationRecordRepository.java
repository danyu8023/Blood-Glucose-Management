package com.tangan.glucose.repository;

import com.tangan.glucose.entity.MedicationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.*;

public interface MedicationRecordRepository extends JpaRepository<MedicationRecord, UUID> {
    Page<MedicationRecord> findByUserIdAndDeletedFalseAndTakenAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    Optional<MedicationRecord> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
    long countByUserIdAndDeletedFalseAndTakenAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to);
    @Modifying(clearAutomatically = true)
    @Query("update MedicationRecord m set m.deleted=true, m.updatedAt=:now where m.id=:id and m.user.id=:uid")
    int softDelete(@Param("id") UUID id, @Param("uid") UUID userId, @Param("now") OffsetDateTime now);
}
