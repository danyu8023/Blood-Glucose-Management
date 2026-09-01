package com.tangan.glucose.dto;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int pageSize, long total, boolean hasNext) { }
