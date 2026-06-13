package com.staynest.api.repository;

import com.staynest.api.entity.Property;
import com.staynest.api.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID>,
        JpaSpecificationExecutor<Property> {

    Page<Property> findByHostIdOrderByCreatedAtDesc(UUID hostId, Pageable pageable);

    Page<Property> findByHostIdAndStatusOrderByCreatedAtDesc(UUID hostId, PropertyStatus status, Pageable pageable);

    boolean existsByIdAndHostId(UUID id, UUID hostId);

    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);

    long countByStatus(PropertyStatus status);

    long countByHostIdAndStatus(UUID hostId, PropertyStatus status);
}
