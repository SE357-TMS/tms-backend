package com.example.tms.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tms.dto.request.invoice.CreateInvoiceRequest;
import com.example.tms.dto.request.invoice.InvoiceFilterRequest;
import com.example.tms.dto.request.invoice.UpdateInvoiceRequest;
import com.example.tms.dto.response.PaginationResponse;
import com.example.tms.dto.response.invoice.InvoiceResponse;
import com.example.tms.entity.Invoice;
import com.example.tms.entity.TourBooking;
import com.example.tms.repository.InvoiceRepository;
import com.example.tms.repository.TourBookingRepository;
import com.example.tms.service.interface_.InvoiceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TourBookingRepository tourBookingRepository;

    @Override
    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request) {
        TourBooking booking = tourBookingRepository.findById(request.getBookingId())
                .filter(b -> b.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check if invoice already exists
        if (invoiceRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new RuntimeException("Invoice already exists for this booking");
        }

        Invoice invoice = new Invoice();
        invoice.setTourBooking(booking);
        invoice.setTotalAmount(request.getTotalAmount());
        invoice.setPaymentStatus(Invoice.PaymentStatus.UNPAID);
        invoice.setPaymentMethod(request.getPaymentMethod());

        Invoice saved = invoiceRepository.save(invoice);
        return new InvoiceResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return new InvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getByBookingId(UUID bookingId) {
        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Invoice not found for this booking"));
        return new InvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<InvoiceResponse> getAll(InvoiceFilterRequest filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort);

        Specification<Invoice> spec = buildSpecification(filter);
        Page<Invoice> page = invoiceRepository.findAll(spec, pageable);

        List<InvoiceResponse> items = page.getContent().stream()
                .map(InvoiceResponse::new)
                .collect(Collectors.toList());

        return new PaginationResponse<>(page, items);
    }

    @Override
    @Transactional
    public InvoiceResponse update(UUID id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (request.getTotalAmount() != null) {
            invoice.setTotalAmount(request.getTotalAmount());
        }
        if (request.getPaymentStatus() != null) {
            invoice.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getPaymentMethod() != null) {
            invoice.setPaymentMethod(request.getPaymentMethod());
        }

        Invoice updated = invoiceRepository.save(invoice);
        return new InvoiceResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.markAsDeleted();
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse markAsPaid(UUID id, String paymentMethod) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Check if booking is canceled
        TourBooking booking = invoice.getTourBooking();
        if (booking.getStatus() == TourBooking.Status.CANCELED) {
            throw new RuntimeException("Cannot process payment for canceled booking");
        }

        if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
            throw new RuntimeException("Invoice is already paid");
        }
        if (invoice.getPaymentStatus() == Invoice.PaymentStatus.REFUNDED) {
            throw new RuntimeException("Cannot pay a refunded invoice");
        }

        invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);
        invoice.setPaymentMethod(paymentMethod);

        // Update booking status to CONFIRMED
        if (booking.getStatus() == TourBooking.Status.PENDING) {
            booking.setStatus(TourBooking.Status.CONFIRMED);
            tourBookingRepository.save(booking);
        }

        Invoice updated = invoiceRepository.save(invoice);
        return new InvoiceResponse(updated);
    }

    @Override
    @Transactional
    public InvoiceResponse markAsRefunded(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .filter(i -> i.getDeletedAt() == 0)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getPaymentStatus() != Invoice.PaymentStatus.PAID) {
            throw new RuntimeException("Can only refund paid invoices");
        }

        invoice.setPaymentStatus(Invoice.PaymentStatus.REFUNDED);

        Invoice updated = invoiceRepository.save(invoice);
        return new InvoiceResponse(updated);
    }

    private Specification<Invoice> buildSpecification(InvoiceFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("deletedAt"), 0L));

            if (filter.getBookingId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("tourBooking").get("id"), filter.getBookingId()));
            }
            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("tourBooking").get("user").get("id"), filter.getUserId()));
            }
            if (filter.getPaymentStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), filter.getPaymentStatus()));
            }
            if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
