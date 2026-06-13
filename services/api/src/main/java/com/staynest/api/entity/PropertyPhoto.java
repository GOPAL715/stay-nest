package com.staynest.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "property_photos")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyPhoto extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "caption", length = 255)
    private String caption;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_cover", nullable = false)
    private boolean isCover;
}
