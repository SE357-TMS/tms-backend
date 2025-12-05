package com.example.tms.dto.request.tourbooking;

import com.example.tms.enity.TourBooking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTourBookingRequest {
    
    private TourBooking.Status status;
}
