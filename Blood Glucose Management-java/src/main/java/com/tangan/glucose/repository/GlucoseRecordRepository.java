package com.tangan.glucose.repository;

import com.tangan.glucose.entity.GlucoseRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface GlucoseRecordRepository extends JpaRepository<GlucoseRecord, UUID> {
    Page<GlucoseRecord> findByUserIdAndDeletedFalseAndMeasuredAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    Page<GlucoseRecord> findByUserIdAndDeletedFalseAndMeasuredAtBetweenAndPeriod(UUID userId, OffsetDateTime from, OffsetDateTime to, String period, Pageable pageable);
    Optional<GlucoseRecord> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
    long countByUserIdAndDeletedFalseAndMeasuredAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to);
    @Query("select avg(g.value) from GlucoseRecord g where g.user.id=:uid and g.deleted=false and g.measuredAt between :from and :to")
    Double average(@Param("uid") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    @Query("select min(g.value) from GlucoseRecord g where g.user.id=:uid and g.deleted=false and g.measuredAt between :from and :to")
    Double minimum(@Param("uid") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    @Query("select max(g.value) from GlucoseRecord g where g.user.id=:uid and g.deleted=false and g.measuredAt between :from and :to")
    Double maximum(@Param("uid") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
    @Modifying(clearAutomatically = true)
    @Query("update GlucoseRecord g set g.deleted=true, g.updatedAt=:now where g.id=:id and g.user.id=:uid")
    int softDelete(@Param("id") UUID id, @Param("uid") UUID userId, @Param("now") OffsetDateTime now);
}
