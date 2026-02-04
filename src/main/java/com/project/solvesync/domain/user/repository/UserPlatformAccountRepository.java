package com.project.solvesync.domain.user.repository;

import com.project.solvesync.domain.common.Platform;
import com.project.solvesync.domain.user.entity.UserPlatformAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPlatformAccountRepository extends JpaRepository<UserPlatformAccount, Long> {

    boolean existsByUserIdAndPlatform(Long userId, Platform platform);

    Optional<UserPlatformAccount> findByUserIdAndPlatform(Long userId, Platform platform);

    List<UserPlatformAccount> findAllByUserIdOrderByPlatformAsc(Long userId);
}
