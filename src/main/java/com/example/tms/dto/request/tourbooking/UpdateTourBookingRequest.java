package com.example.tms.dto.request.tourbooking;

import com.example.tms.entity.TourBooking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTourBookingRequest {
    
    private TourBooking.Status status;
}

