package org.example.bookingservice.dto;

import java.math.BigDecimal;

public class PaymentInitiateRequest {
    private Long bookingId;
    private BigDecimal amount;
    private String callbackUrl;

    public PaymentInitiateRequest() {}

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
