package com.example.tms.dto.request.bookingtraveler;

import java.time.LocalDate;
import java.util.UUID;

import com.example.tms.enity.BookingTraveler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookingTravelerRequest {
    
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;
    
    private BookingTraveler.Gender gender;
    
    private LocalDate dateOfBirth;
    
    @Size(max = 100, message = "Identity document must be at most 100 characters")
    private String identityDoc;
}
