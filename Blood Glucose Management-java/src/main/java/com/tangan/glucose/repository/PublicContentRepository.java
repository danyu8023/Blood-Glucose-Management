package com.tangan.glucose.repository;

import com.tangan.glucose.entity.PublicContent;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PublicContentRepository extends JpaRepository<PublicContent, UUID> {
    Page<PublicContent> findByPublishedTrueAndContentTypeAndCategoryContainingIgnoreCase(String contentType, String category, Pageable pageable);
    Optional<PublicContent> findBySlugAndPublishedTrue(String slug);
}
