package org.example.paymentservice.dto;

import java.math.BigDecimal;

public class PaymentInitiateRequest {
    private Long bookingId;
    private String bookingReference;
    private BigDecimal amount;

    public PaymentInitiateRequest() {}

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
