package com.staynest.api.repository;

import com.staynest.api.entity.HostApplication;
import com.staynest.api.enums.HostApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostApplicationRepository extends JpaRepository<HostApplication, UUID> {

    Optional<HostApplication> findByApplicantId(UUID applicantId);

    boolean existsByApplicantId(UUID applicantId);

    Page<HostApplication> findByStatus(HostApplicationStatus status, Pageable pageable);
}
