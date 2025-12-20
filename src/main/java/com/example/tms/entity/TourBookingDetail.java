package com.example.tms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tour_booking_detail")
public class TourBookingDetail extends AbstractBaseEntity {
    // Inherit UUID id, createdAt, updatedAt, deleted from AbstractBaseEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private TourBooking tourBooking;

    @Column(name = "no_adults", nullable = false)
    private Integer noAdults;

    @Column(name = "no_children", nullable = false)
    private Integer noChildren;
}

