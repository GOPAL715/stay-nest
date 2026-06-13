package com.staynest.api.repository;

import com.staynest.api.entity.PropertyAvailability;
import com.staynest.api.enums.AvailabilityBlockReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyAvailabilityRepository extends JpaRepository<PropertyAvailability, UUID> {

    List<PropertyAvailability> findByPropertyIdOrderByStartDateAsc(UUID propertyId);

    @Query("""
            SELECT pa FROM PropertyAvailability pa
            WHERE pa.property.id = :propertyId
              AND pa.startDate <= :endDate
              AND pa.endDate >= :startDate
            """)
    List<PropertyAvailability> findOverlapping(
            @Param("propertyId") UUID propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<PropertyAvailability> findByPropertyIdAndReason(UUID propertyId, AvailabilityBlockReason reason);
}
