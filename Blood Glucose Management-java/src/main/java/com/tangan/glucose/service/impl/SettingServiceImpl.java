package com.tangan.glucose.service.impl;

import com.tangan.glucose.dto.SettingsDtos;
import com.tangan.glucose.entity.*;
import com.tangan.glucose.repository.*;
import com.tangan.glucose.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {
    private final UserSettingRepository repository;
    private final UserAccountRepository userRepository;

    @Override @Transactional(rollbackFor = Exception.class)
    public SettingsDtos.Response get(UUID userId) {
        return toResponse(getOrCreate(userId));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public SettingsDtos.Response update(UUID userId, SettingsDtos.Request request) {
        UserSetting setting = getOrCreate(userId);
        if (request.glucoseReminder() != null) setting.setGlucoseReminder(request.glucoseReminder());
        if (request.medicationReminder() != null) setting.setMedicationReminder(request.medicationReminder());
        if (request.familyAlert() != null) setting.setFamilyAlert(request.familyAlert());
        if (request.autoSync() != null) setting.setAutoSync(request.autoSync());
        if (request.faceIdUnlock() != null) setting.setFaceIdUnlock(request.faceIdUnlock());
        return toResponse(repository.save(setting));
    }

    private UserSetting getOrCreate(UUID userId) {
        return repository.findById(userId).orElseGet(() -> {
            UserSetting setting = new UserSetting();
            setting.setUser(userRepository.findById(userId).orElseThrow());
            return repository.save(setting);
        });
    }
    private SettingsDtos.Response toResponse(UserSetting s) { return new SettingsDtos.Response(s.getGlucoseReminder(), s.getMedicationReminder(), s.getFamilyAlert(), s.getAutoSync(), s.getFaceIdUnlock()); }
}
