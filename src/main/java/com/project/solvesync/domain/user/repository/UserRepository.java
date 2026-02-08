package com.project.solvesync.domain.user.repository;

import com.project.solvesync.domain.user.entity.AuthProvider;
import com.project.solvesync.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
