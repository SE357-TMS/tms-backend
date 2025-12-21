package com.example.tms.dto.response.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.tms.entity.Invoice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceResponse {
    private UUID id;
    private String invoiceCode;
    private UUID bookingId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String routeName;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private BigDecimal totalAmount;
    private Invoice.PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InvoiceResponse(Invoice invoice) {
        this.id = invoice.getId();
        this.invoiceCode = "HNT" + invoice.getId().toString().substring(0, 8).toUpperCase();
        if (invoice.getTourBooking() != null) {
            this.bookingId = invoice.getTourBooking().getId();
            if (invoice.getTourBooking().getUser() != null) {
                this.userId = invoice.getTourBooking().getUser().getId();
                this.userName = invoice.getTourBooking().getUser().getFullName();
                this.userEmail = invoice.getTourBooking().getUser().getEmail();
            }
            if (invoice.getTourBooking().getTrip() != null) {
                this.departureDate = invoice.getTourBooking().getTrip().getDepartureDate();
                this.returnDate = invoice.getTourBooking().getTrip().getReturnDate();
                if (invoice.getTourBooking().getTrip().getRoute() != null) {
                    this.routeName = invoice.getTourBooking().getTrip().getRoute().getRouteName();
                }
            }
        }
        this.totalAmount = invoice.getTotalAmount();
        this.paymentStatus = invoice.getPaymentStatus();
        this.paymentMethod = invoice.getPaymentMethod();
        this.createdAt = invoice.getCreatedAt();
        this.updatedAt = invoice.getUpdatedAt();
    }
}
