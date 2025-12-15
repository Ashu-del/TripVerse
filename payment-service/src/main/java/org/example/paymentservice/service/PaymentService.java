package org.example.paymentservice.service;

import org.example.paymentservice.client.BookingCallbackClient;
import org.example.paymentservice.dto.PaymentInitiateRequest;
import org.example.paymentservice.dto.PaymentInitiateResponse;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingCallbackClient bookingCallbackClient;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          BookingCallbackClient bookingCallbackClient) {
        this.paymentRepository = paymentRepository;
        this.bookingCallbackClient = bookingCallbackClient;
    }

    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest req) {

        Payment payment = new Payment();
        payment.setBookingId(req.getBookingId());
        payment.setBookingReference(req.getBookingReference());
        payment.setAmount(req.getAmount());
        payment.setStatus("CREATED");

        payment = paymentRepository.save(payment);

        // simulate async payment processing
        simulatePayment(payment);

        PaymentInitiateResponse resp = new PaymentInitiateResponse();
        resp.setPaymentId(payment.getId());
        resp.setStatus("CREATED");
        resp.setPaymentUrl("http://fake-gateway/pay/" + payment.getId());

        return resp;
    }

    private void simulatePayment(Payment payment) {
        new Thread(() -> {
            try {
                Thread.sleep(2000); // simulate gateway delay

                // ---- CHANGE THIS TO TEST FAILURE ----
                boolean success = true;

                if (success) {
                    payment.setStatus("SUCCESS");
                    paymentRepository.save(payment);

                    bookingCallbackClient.paymentSuccess(
                            payment.getBookingReference(),
                            java.util.Map.of("paymentId", payment.getId())
                    );
                } else {
                    payment.setStatus("FAILED");
                    paymentRepository.save(payment);

                    bookingCallbackClient.paymentFailed(
                            payment.getBookingReference()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
