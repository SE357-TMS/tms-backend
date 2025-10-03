package com.example.tms.enity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tour_booking_detail")
public class TourBookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private TourBooking tourBooking;

    @Column(name = "no_adults", nullable = false)
    private Integer noAdults;

    @Column(name = "no_children", nullable = false)
    private Integer noChildren;
}
