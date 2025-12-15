package org.example.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "BOOKING-SERVICE")
public interface BookingCallbackClient {
    @PostMapping("/internal/bookings/{bookingRef}/payment-success")
    void paymentSuccess(
            @PathVariable("bookingRef") String bookingRef,
            @RequestBody Map<String, Object> body
    );

    @PostMapping("/internal/bookings/{bookingRef}/payment-failed")
    void paymentFailed(
            @PathVariable("bookingRef") String bookingRef
    );
}
