package com.example.tms.dto.request.customer;

import java.time.LocalDate;
import java.util.UUID;

import com.example.tms.entity.BookingTraveler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTravelerRequest {
    
    private UUID id; // For updating existing traveler
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;
    
    private BookingTraveler.Gender gender;
    
    private LocalDate dateOfBirth;
    
    @Size(max = 100, message = "Identity document must be at most 100 characters")
    private String identityDoc;
    
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;
    
    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phoneNumber;
    
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;
}

