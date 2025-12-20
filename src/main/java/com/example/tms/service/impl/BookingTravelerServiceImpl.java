package com.example.tms.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.bookingtraveler.CreateBookingTravelerRequest;
import com.example.tms.dto.request.bookingtraveler.UpdateBookingTravelerRequest;
import com.example.tms.dto.response.bookingtraveler.BookingTravelerResponse;
import com.example.tms.entity.BookingTraveler;
import com.example.tms.entity.TourBooking;
import com.example.tms.repository.BookingTravelerRepository;
import com.example.tms.repository.TourBookingRepository;
import com.example.tms.service.interface_.BookingTravelerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingTravelerServiceImpl implements BookingTravelerService {

    private final BookingTravelerRepository bookingTravelerRepository;
    private final TourBookingRepository tourBookingRepository;

    @Override
    @Transactional
    public BookingTravelerResponse create(CreateBookingTravelerRequest request) {
        TourBooking booking = tourBookingRepository.findById(request.getBookingId())
                .filter(b -> b.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        BookingTraveler traveler = new BookingTraveler();
        traveler.setTourBooking(booking);
        traveler.setFullName(request.getFullName());
        traveler.setGender(request.getGender());
        traveler.setDateOfBirth(request.getDateOfBirth());
        traveler.setIdentityDoc(request.getIdentityDoc());

        BookingTraveler saved = bookingTravelerRepository.save(traveler);
        return new BookingTravelerResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingTravelerResponse getById(UUID id) {
        BookingTraveler traveler = bookingTravelerRepository.findById(id)
                .filter(t -> t.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking traveler not found"));
        return new BookingTravelerResponse(traveler);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingTravelerResponse> getByBookingId(UUID bookingId) {
        return bookingTravelerRepository.findByBookingId(bookingId).stream()
                .map(BookingTravelerResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingTravelerResponse update(UUID id, UpdateBookingTravelerRequest request) {
        BookingTraveler traveler = bookingTravelerRepository.findById(id)
                .filter(t -> t.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking traveler not found"));

        if (request.getFullName() != null) {
            traveler.setFullName(request.getFullName());
        }
        if (request.getGender() != null) {
            traveler.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            traveler.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getIdentityDoc() != null) {
            traveler.setIdentityDoc(request.getIdentityDoc());
        }

        BookingTraveler updated = bookingTravelerRepository.save(traveler);
        return new BookingTravelerResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        BookingTraveler traveler = bookingTravelerRepository.findById(id)
                .filter(t -> t.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking traveler not found"));
        traveler.markAsDeleted();
        bookingTravelerRepository.save(traveler);
    }
}

