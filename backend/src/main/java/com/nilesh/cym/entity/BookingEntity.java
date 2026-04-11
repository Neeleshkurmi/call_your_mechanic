package com.nilesh.cym.entity;

import com.nilesh.cym.entity.enums.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_user_time", columnList = "user_id,booking_time"),
        @Index(name = "idx_bookings_mechanic_time", columnList = "mechanic_id,booking_time"),
        @Index(name = "idx_bookings_status", columnList = "status")
})
public class BookingEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mechanic_id", nullable = false)
    private MechanicEntity mechanic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.REQUESTED;

    @Column(name = "booking_time", nullable = false)
    private Instant bookingTime;

    @Column(nullable = false)
    private Double latitude; // Initial service point latitude captured at booking creation.

    @Column(nullable = false)
    private Double longitude; // Initial service point longitude captured at booking creation.

    @Column(name = "travel_distance_km")
    private Double travelDistanceKm;

    @Column(name = "travel_charge")
    private Double travelCharge;

    @Column(name = "service_charge")
    private Double serviceCharge;

    @Column(name = "total_fare")
    private Double totalFare;

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

    public VehicleEntity getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleEntity vehicle) {
        this.vehicle = vehicle;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Instant bookingTime) {
        this.bookingTime = bookingTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getTravelDistanceKm() {
        return travelDistanceKm;
    }

    public void setTravelDistanceKm(Double travelDistanceKm) {
        this.travelDistanceKm = travelDistanceKm;
    }

    public Double getTravelCharge() {
        return travelCharge;
    }

    public void setTravelCharge(Double travelCharge) {
        this.travelCharge = travelCharge;
    }

    public Double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(Double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public Double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(Double totalFare) {
        this.totalFare = totalFare;
    }
}
