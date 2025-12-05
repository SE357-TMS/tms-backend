package com.example.tms.dto.request.bookingtraveler;

import java.time.LocalDate;

import com.example.tms.enity.BookingTraveler;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookingTravelerRequest {
    
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;
    
    private BookingTraveler.Gender gender;
    
    private LocalDate dateOfBirth;
    
    @Size(max = 100, message = "Identity document must be at most 100 characters")
    private String identityDoc;
}
