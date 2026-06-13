package com.staynest.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expiry", nullable = false)
    private Instant expiry;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return expiry.isBefore(Instant.now());
    }
}
