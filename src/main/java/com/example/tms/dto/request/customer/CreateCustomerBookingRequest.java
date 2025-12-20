package com.example.tms.dto.request.customer;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCustomerBookingRequest {
    
    @NotNull(message = "Trip ID is required")
    private UUID tripId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    // Optional: If coming from cart, provide cartItemId to delete it after booking
    private UUID cartItemId;
    
    // Travelers information - can be provided at creation or added later
    @Valid
    private List<TravelerInfo> travelers;
    
    @Getter
    @Setter
    public static class TravelerInfo {
        private String fullName;
        private String gender; // M, F, O
        private String dateOfBirth; // yyyy-MM-dd format
        private String identityDoc;
        private String email;
        private String phoneNumber;
        private String address;
    }
}

