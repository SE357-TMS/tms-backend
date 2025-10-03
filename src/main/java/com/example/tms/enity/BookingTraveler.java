package com.example.tms.enity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking_traveler")
public class BookingTraveler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "traveler_id")
    private Integer travelerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private TourBooking tourBooking;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    public enum Gender { M, F, O }
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "ENUM('M','F','O')")
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "identity_doc", length = 100)
    private String identityDoc;
}
