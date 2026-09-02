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
    @Query("select m from MedicationRecord m where m.user.id=:uid and m.deleted=false and coalesce(m.scheduledAt,m.takenAt,m.createdAt) between :from and :to order by coalesce(m.scheduledAt,m.takenAt,m.createdAt) desc")
    List<MedicationRecord> findForReport(@Param("uid") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    Optional<MedicationRecord> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
    long countByUserIdAndDeletedFalseAndTakenAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to);
    @Modifying(clearAutomatically = true)
    @Query("update MedicationRecord m set m.deleted=true, m.updatedAt=:now where m.id=:id and m.user.id=:uid")
    int softDelete(@Param("id") UUID id, @Param("uid") UUID userId, @Param("now") OffsetDateTime now);
}
