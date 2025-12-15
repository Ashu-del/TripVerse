package org.example.paymentservice.controller;

import org.example.paymentservice.dto.PaymentInitiateRequest;
import org.example.paymentservice.dto.PaymentInitiateResponse;
import org.example.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService service;

    @Autowired
    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiate(
            @RequestBody PaymentInitiateRequest req) {

        PaymentInitiateResponse resp = service.initiatePayment(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
