package com.nilesh.cym.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_mechanic_created", columnList = "mechanic_id,created_at"),
        @Index(name = "idx_reviews_user_created", columnList = "user_id,created_at"),
        @Index(name = "idx_reviews_booking", columnList = "booking_id", unique = true)
})
public class ReviewEntity extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private BookingEntity booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mechanic_id", nullable = false)
    private MechanicEntity mechanic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String review;

    public BookingEntity getBooking() {
        return booking;
    }

    public void setBooking(BookingEntity booking) {
        this.booking = booking;
    }

    public MechanicEntity getMechanic() {
        return mechanic;
    }

    public void setMechanic(MechanicEntity mechanic) {
        this.mechanic = mechanic;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
