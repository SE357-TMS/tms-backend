package com.example.tms.dto.response.bookingtraveler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.tms.entity.BookingTraveler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingTravelerResponse {
    private UUID id;
    private UUID bookingId;
    private String fullName;
    private BookingTraveler.Gender gender;
    private LocalDate dateOfBirth;
    private String identityDoc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public BookingTravelerResponse(BookingTraveler traveler) {
        this.id = traveler.getId();
        if (traveler.getTourBooking() != null) {
            this.bookingId = traveler.getTourBooking().getId();
        }
        this.fullName = traveler.getFullName();
        this.gender = traveler.getGender();
        this.dateOfBirth = traveler.getDateOfBirth();
        this.identityDoc = traveler.getIdentityDoc();
        this.createdAt = traveler.getCreatedAt();
        this.updatedAt = traveler.getUpdatedAt();
    }
}

