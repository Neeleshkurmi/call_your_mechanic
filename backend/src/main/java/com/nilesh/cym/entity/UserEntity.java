package com.nilesh.cym.entity;

import com.nilesh.cym.entity.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_mob", columnList = "mob", unique = true),
        @Index(name = "idx_users_role", columnList = "role")
})
public class UserEntity extends AuditableEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String mob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean profileCompleted = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMob() {
        return mob;
    }

    public void setMob(String mob) {
        this.mob = mob;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }
}
