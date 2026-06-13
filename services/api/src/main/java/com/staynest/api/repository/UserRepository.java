package com.staynest.api.repository;

import com.staynest.api.entity.User;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByEmail(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByStatusAndRole(UserStatus status, UserRole role, Pageable pageable);
}
