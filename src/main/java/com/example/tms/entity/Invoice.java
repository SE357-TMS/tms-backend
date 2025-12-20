package com.example.tms.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoice")
public class Invoice extends AbstractBaseEntity {
    // Inherit UUID id, createdAt, updatedAt, deleted from AbstractBaseEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private TourBooking tourBooking;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    public enum PaymentStatus { UNPAID, PAID, REFUNDED }
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, columnDefinition = "ENUM('UNPAID','PAID','REFUNDED')")
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
}

