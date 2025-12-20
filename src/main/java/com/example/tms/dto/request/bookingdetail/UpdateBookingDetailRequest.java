package com.example.tms.dto.request.bookingdetail;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookingDetailRequest {
    
    @Min(value = 1, message = "At least 1 adult is required")
    private Integer noAdults;
    
    @Min(value = 0, message = "Number of children cannot be negative")
    private Integer noChildren;
}

