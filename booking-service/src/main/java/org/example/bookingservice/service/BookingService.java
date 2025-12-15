package org.example.bookingservice.service;

import org.example.bookingservice.client.InventoryClient;
import org.example.bookingservice.client.PaymentClient;
import org.example.bookingservice.dto.*;
import org.example.bookingservice.entity.Booking;
import org.example.bookingservice.exception.NoSeatsAvailableException;
import org.example.bookingservice.exception.NotFoundException;
import org.example.bookingservice.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    @Value("${internal.service.token}")
    private String internalToken;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          InventoryClient inventoryClient,
                          PaymentClient paymentClient) {
        this.bookingRepository = bookingRepository;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public CreateBookingResponse createBooking(CreateBookingRequest req, String userIdHeader, String idempotencyKey) {
        if (userIdHeader == null) throw new IllegalArgumentException("Missing X-User-Id header");
        Long userId = Long.parseLong(userIdHeader);

        String bookingRef = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : "BR-" + UUID.randomUUID().toString();

        Optional<Booking> existing = bookingRepository.findByBookingReference(bookingRef);
        if (existing.isPresent()) {
            Booking b = existing.get();
            CreateBookingResponse r = new CreateBookingResponse();
            r.setBookingReference(b.getBookingReference());
            r.setBookingId(b.getId());
            r.setPaymentId(b.getPaymentId());
            r.setPaymentUrl(null);
            return r;
        }

        // 1. Get destination price
        DestinationPriceResponse dest = inventoryClient.getDestination(req.getDestinationId());
        if (dest == null) throw new NotFoundException("Destination not found");

        BigDecimal amount = dest.getPrice().multiply(new BigDecimal(req.getNumPeople()));

        // 2. Reserve seats (inventory internal)
        try {
            inventoryClient.reserve(req.getDestinationId(), req.getNumPeople(), internalToken);
        } catch (feign.FeignException e) {
            if (e.status() == 409) { // assuming inventory returns 409 on conflict
                throw new NoSeatsAvailableException("No seats available");
            }
            throw e;
        }

        // 3. Persist booking (PENDING_PAYMENT)
        Booking booking = new Booking();
        booking.setBookingReference(bookingRef);
        booking.setUserId(userId);
        booking.setDestinationId(req.getDestinationId());
        booking.setBookingDate(req.getBookingDate() != null ? req.getBookingDate() : Instant.now());
        booking.setNumPeople(req.getNumPeople());
        booking.setAmount(amount);
        booking.setStatus("PENDING_PAYMENT");
        booking = bookingRepository.save(booking);

        // 4. Initiate payment
        PaymentInitiateRequest payReq = new PaymentInitiateRequest();
        payReq.setBookingId(booking.getId());
        payReq.setAmount(amount);
        // callback to booking-service via gateway or direct
        payReq.setCallbackUrl("http://localhost:8091/internal/bookings/" + booking.getBookingReference() + "/payment-success");

        PaymentInitiateResponse payResp = null;
        try {
            payResp = paymentClient.initiatePayment(payReq);
        } catch (feign.FeignException e) {
            // compensate: release seats
            try {
                inventoryClient.release(req.getDestinationId(), req.getNumPeople(), internalToken);
            } catch (Exception ignore) {
            }
            booking.setStatus("PAYMENT_FAILED");
            bookingRepository.save(booking);
            throw new RuntimeException("Payment initiation failed");
        }

        if (payResp != null && payResp.getPaymentId() != null) {
            booking.setPaymentId(payResp.getPaymentId());
            bookingRepository.save(booking);
        }

        CreateBookingResponse resp = new CreateBookingResponse();
        resp.setBookingReference(booking.getBookingReference());
        resp.setBookingId(booking.getId());
        if (payResp != null) {
            resp.setPaymentId(payResp.getPaymentId());
            resp.setPaymentUrl(payResp.getPaymentUrl());
        }
        return resp;
    }

    public List<Booking> findByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
    }

    @Transactional
    public void cancelBooking(String bookingReference, String requesterUserIdHeader) {
        Booking b = getByReference(bookingReference);
        Long requesterId = Long.parseLong(requesterUserIdHeader);
        if (!b.getUserId().equals(requesterId)) throw new IllegalArgumentException("Not allowed");

        if ("CANCELLED".equals(b.getStatus())) return;

        // release seats
        try {
            inventoryClient.release(b.getDestinationId(), b.getNumPeople(), internalToken);
        } catch (Exception ignore) {
        }

        // mark cancelled
        b.setStatus("CANCELLED");
        bookingRepository.save(b);
    }

    @Transactional
    public void paymentSuccessCallback(String bookingReference, Long paymentId) {
        Booking b = getByReference(bookingReference);
        if ("CONFIRMED".equals(b.getStatus())) return;
        b.setPaymentId(paymentId);
        b.setStatus("CONFIRMED");
        bookingRepository.save(b);
        // TODO: notify Notification-Service
    }

    @Transactional
    public void paymentFailedCallback(String bookingReference) {
        Booking b = getByReference(bookingReference);
        if ("PAYMENT_FAILED".equals(b.getStatus())) return;
        b.setStatus("PAYMENT_FAILED");
        bookingRepository.save(b);
        // release seats
        try {
            inventoryClient.release(b.getDestinationId(), b.getNumPeople(), internalToken);
        } catch (Exception ignore) {
        }
    }
}
