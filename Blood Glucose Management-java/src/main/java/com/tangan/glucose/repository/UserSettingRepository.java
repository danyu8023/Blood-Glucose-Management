package com.tangan.glucose.repository;

import com.tangan.glucose.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserSettingRepository extends JpaRepository<UserSetting, UUID> { }
