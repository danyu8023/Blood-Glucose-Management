package com.tangan.glucose.service;

import com.tangan.glucose.dto.FamilyDtos;
import java.util.*;

public interface FamilyConnectionService {
    List<FamilyDtos.Response> list(UUID userId);
    FamilyDtos.Response create(UUID userId, FamilyDtos.Request request);
    void delete(UUID userId, UUID id);
}
