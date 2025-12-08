package com.example.tms.dto.request.tourbooking;

import java.util.List;

import com.example.tms.dto.request.booking.TravelerRequest;
import com.example.tms.enity.TourBooking;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTourBookingRequest {

    private TourBooking.Status status;

    // For editing travelers (only allowed before departure)
    @Valid
    private List<TravelerRequest> travelers;

    // Number of adults and children (recalculated from travelers if provided)
    private Integer noAdults;
    private Integer noChildren;

    // Special requests or notes
    private String notes;
}
