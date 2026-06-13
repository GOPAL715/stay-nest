package com.staynest.api.repository;

import com.staynest.api.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, UUID> {

    boolean existsByName(String name);

    Set<Amenity> findByIdIn(Set<UUID> ids);
}
