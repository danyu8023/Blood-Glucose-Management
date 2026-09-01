package com.tangan.glucose.service;

import com.tangan.glucose.dto.PageResponse;
import java.util.Map;

public interface PublicContentService {
    PageResponse<Map<String, Object>> articles(String category, int page, int pageSize);
    Map<String, Object> article(String slug);
    Map<String, Object> guide(String slug);
}
