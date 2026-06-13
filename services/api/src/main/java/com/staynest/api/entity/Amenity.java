package com.staynest.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "amenities")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Amenity extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "category", length = 100)
    private String category;
}
