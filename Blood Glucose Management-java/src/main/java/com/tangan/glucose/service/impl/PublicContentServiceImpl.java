package com.tangan.glucose.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangan.glucose.common.ApiException;
import com.tangan.glucose.dto.PageResponse;
import com.tangan.glucose.entity.PublicContent;
import com.tangan.glucose.repository.PublicContentRepository;
import com.tangan.glucose.service.PublicContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class PublicContentServiceImpl implements PublicContentService {
    private final PublicContentRepository repository;
    private final ObjectMapper objectMapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> articles(String category, int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(pageSize, 1), 50), Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<PublicContent> result = repository.findByPublishedTrueAndContentTypeAndCategoryContainingIgnoreCase("article", category == null ? "" : category, pageable);
        return new PageResponse<>(result.map(this::card).getContent(), result.getNumber() + 1, result.getSize(), result.getTotalElements(), result.hasNext());
    }

    @Override @Transactional(readOnly = true)
    public Map<String, Object> article(String slug) { return detail(repository.findBySlugAndPublishedTrue(slug).filter(c -> "article".equals(c.getContentType())).orElseThrow(() -> ApiException.notFound("资讯不存在"))); }

    @Override @Transactional(readOnly = true)
    public Map<String, Object> guide(String slug) { return detail(repository.findBySlugAndPublishedTrue(slug).filter(c -> "guide".equals(c.getContentType())).orElseThrow(() -> ApiException.notFound("指南不存在"))); }

    private Map<String, Object> card(PublicContent c) {
        Map<String, Object> map = new LinkedHashMap<>(); map.put("slug", c.getSlug()); map.put("title", c.getTitle()); map.put("summary", c.getSummary()); map.put("coverUrl", c.getCoverUrl()); map.put("publishedAt", c.getPublishedAt()); return map;
    }
    private Map<String, Object> detail(PublicContent c) {
        Map<String, Object> map = card(c); map.put("eyebrow", c.getCategory()); map.put("lead", c.getLead());
        try { map.put("sections", objectMapper.readValue(c.getBody(), new TypeReference<>() { })); }
        catch (Exception ex) { map.put("body", c.getBody()); }
        map.put("disclaimer", "本文为公共健康科普，不替代医生针对个人情况的建议。"); return map;
    }
}
