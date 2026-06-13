package com.staynest.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "platform_config")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_admin_id")
    private User updatedByAdmin;

    public void updateValue(String newValue, User admin) {
        this.configValue = newValue;
        this.updatedByAdmin = admin;
    }
}
