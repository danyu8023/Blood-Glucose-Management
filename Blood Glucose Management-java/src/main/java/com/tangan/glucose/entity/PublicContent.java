package com.tangan.glucose.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "public_contents", uniqueConstraints = @UniqueConstraint(name = "uk_public_slug", columnNames = "slug"))
@Getter @Setter @NoArgsConstructor
public class PublicContent {
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @Column(nullable = false, length = 120) private String slug;
    @Column(nullable = false, length = 20) private String contentType;
    @Column(nullable = false, length = 60) private String category;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 500) private String summary;
    @Column(name = "content_lead", length = 500) private String lead;
    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT") private String body;
    @Column(name = "cover_url", length = 500) private String coverUrl;
    @Column(name = "published_at") private OffsetDateTime publishedAt;
    @Column(nullable = false) private Boolean published = true;
}
