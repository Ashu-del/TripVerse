package org.example.bookingservice.client;

import org.example.bookingservice.dto.PaymentInitiateRequest;
import org.example.bookingservice.dto.PaymentInitiateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    @PostMapping("/payments/initiate")
    PaymentInitiateResponse initiatePayment(@RequestBody PaymentInitiateRequest request);

}
