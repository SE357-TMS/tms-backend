package com.example.tms.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.bookingdetail.CreateBookingDetailRequest;
import com.example.tms.dto.request.bookingdetail.UpdateBookingDetailRequest;
import com.example.tms.dto.response.bookingdetail.BookingDetailResponse;
import com.example.tms.enity.TourBooking;
import com.example.tms.enity.TourBookingDetail;
import com.example.tms.repository.TourBookingDetailRepository;
import com.example.tms.repository.TourBookingRepository;
import com.example.tms.service.interface_.BookingDetailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingDetailServiceImpl implements BookingDetailService {

    private final TourBookingDetailRepository bookingDetailRepository;
    private final TourBookingRepository tourBookingRepository;

    @Override
    @Transactional
    public BookingDetailResponse create(CreateBookingDetailRequest request) {
        TourBooking booking = tourBookingRepository.findById(request.getBookingId())
                .filter(b -> b.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check if detail already exists
        if (bookingDetailRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new RuntimeException("Booking detail already exists for this booking");
        }

        TourBookingDetail detail = new TourBookingDetail();
        detail.setTourBooking(booking);
        detail.setNoAdults(request.getNoAdults());
        detail.setNoChildren(request.getNoChildren());

        TourBookingDetail saved = bookingDetailRepository.save(detail);
        return new BookingDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getById(UUID id) {
        TourBookingDetail detail = bookingDetailRepository.findById(id)
                .filter(d -> d.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking detail not found"));
        return new BookingDetailResponse(detail);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getByBookingId(UUID bookingId) {
        TourBookingDetail detail = bookingDetailRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking detail not found for this booking"));
        return new BookingDetailResponse(detail);
    }

    @Override
    @Transactional
    public BookingDetailResponse update(UUID id, UpdateBookingDetailRequest request) {
        TourBookingDetail detail = bookingDetailRepository.findById(id)
                .filter(d -> d.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking detail not found"));

        if (request.getNoAdults() != null) {
            detail.setNoAdults(request.getNoAdults());
        }
        if (request.getNoChildren() != null) {
            detail.setNoChildren(request.getNoChildren());
        }

        TourBookingDetail updated = bookingDetailRepository.save(detail);
        return new BookingDetailResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        TourBookingDetail detail = bookingDetailRepository.findById(id)
                .filter(d -> d.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking detail not found"));
        detail.markAsDeleted();
        bookingDetailRepository.save(detail);
    }
}
