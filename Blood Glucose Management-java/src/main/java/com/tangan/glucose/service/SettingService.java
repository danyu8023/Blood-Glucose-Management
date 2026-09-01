package com.tangan.glucose.service;

import com.tangan.glucose.dto.SettingsDtos;
import java.util.UUID;

public interface SettingService {
    SettingsDtos.Response get(UUID userId);
    SettingsDtos.Response update(UUID userId, SettingsDtos.Request request);
}
