package com.tangan.glucose.repository;

import com.tangan.glucose.entity.FamilyConnection;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.*;

public interface FamilyConnectionRepository extends JpaRepository<FamilyConnection, UUID> {
    List<FamilyConnection> findByUserId(UUID userId);
    Optional<FamilyConnection> findByIdAndUserId(UUID id, UUID userId);
    @Modifying(clearAutomatically = true)
    @Query("delete from FamilyConnection f where f.id=:id and f.user.id=:uid")
    int deleteOwned(@Param("id") UUID id, @Param("uid") UUID userId);
}
