package com.example.tms.dto.response.bookingdetail;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.tms.enity.TourBookingDetail;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingDetailResponse {
    private UUID id;
    private UUID bookingId;
    private Integer noAdults;
    private Integer noChildren;
    private Integer totalTravelers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public BookingDetailResponse(TourBookingDetail detail) {
        this.id = detail.getId();
        if (detail.getTourBooking() != null) {
            this.bookingId = detail.getTourBooking().getId();
        }
        this.noAdults = detail.getNoAdults();
        this.noChildren = detail.getNoChildren();
        this.totalTravelers = detail.getNoAdults() + detail.getNoChildren();
        this.createdAt = detail.getCreatedAt();
        this.updatedAt = detail.getUpdatedAt();
    }
}
