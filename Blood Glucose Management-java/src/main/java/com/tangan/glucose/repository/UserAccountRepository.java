package com.tangan.glucose.repository;

import com.tangan.glucose.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByAccountAndActiveTrue(String account);
    boolean existsByAccount(String account);
}
