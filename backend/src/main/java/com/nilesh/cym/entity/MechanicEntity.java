package com.nilesh.cym.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "mechanics", indexes = {
        @Index(name = "idx_mechanics_user", columnList = "user_id", unique = true),
        @Index(name = "idx_mechanics_available", columnList = "is_available")
})
public class MechanicEntity extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "is_available", nullable = false)
    private Boolean available = Boolean.FALSE;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears = 0;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(length = 1000)
    private String bio;

    @Column(length = 500)
    private String skills;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }
}
