package com.tangan.glucose.service.impl;

import com.tangan.glucose.common.ApiException;
import com.tangan.glucose.dto.UpdateUserRequest;
import com.tangan.glucose.dto.UserResponse;
import com.tangan.glucose.entity.UserAccount;
import com.tangan.glucose.repository.UserAccountRepository;
import com.tangan.glucose.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserAccountRepository repository;

    @Override
    @Transactional(readOnly = true)
    public UserAccount require(UUID userId) {
        return repository.findById(userId).filter(UserAccount::getActive)
                .orElseThrow(() -> ApiException.unauthorized("用户不存在或已停用"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getName(), user.getAccount(), user.getDiabetesType(), user.getPhone(),
                new UserResponse.TargetRange(user.getTargetMin(), user.getTargetMax(), "mmol/L"),
                new UserResponse.Doctor(user.getDoctorName(), user.getDoctorClinic()), user.getTimezone());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse update(UUID userId, UpdateUserRequest request) {
        UserAccount user = require(userId);
        if (request.name() != null) user.setName(request.name());
        if (request.diabetesType() != null) user.setDiabetesType(request.diabetesType());
        if (request.timezone() != null) user.setTimezone(request.timezone());
        if (request.targetRange() != null) {
            if (request.targetRange().min().compareTo(request.targetRange().max()) >= 0) {
                throw ApiException.badRequest("目标血糖下限必须小于上限");
            }
            user.setTargetMin(request.targetRange().min());
            user.setTargetMax(request.targetRange().max());
        }
        if (request.doctor() != null) {
            if (request.doctor().name() != null) user.setDoctorName(request.doctor().name());
            if (request.doctor().clinic() != null) user.setDoctorClinic(request.doctor().clinic());
        }
        log.info("Updated profile for user {}", userId);
        return toResponse(repository.save(user));
    }
}
