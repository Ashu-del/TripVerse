package org.example.bookingservice.controller;

import jakarta.validation.Valid;
import org.example.bookingservice.dto.BookingDto;
import org.example.bookingservice.dto.CreateBookingRequest;
import org.example.bookingservice.dto.CreateBookingResponse;
import org.example.bookingservice.entity.Booking;
import org.example.bookingservice.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BookingController {

    @Value("${internal.service.token}")
    private String internalToken;
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @PostMapping("/bookings")
    public ResponseEntity<CreateBookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest req,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        CreateBookingResponse resp = bookingService.createBooking(req, userId, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<List<BookingDto>> myBookings(@RequestHeader(value = "X-User-Id", required = true) String userId) {
        Long uid = Long.parseLong(userId);
        List<Booking> list = bookingService.findByUserId(uid);
        List<BookingDto> dtoList = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/bookings/{ref}")
    public ResponseEntity<BookingDto> getBooking(@PathVariable String ref, @RequestHeader(value = "X-User-Id", required = true) String userId) {
        Booking b = bookingService.getByReference(ref);
        if (!b.getUserId().toString().equals(userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(toDto(b));
    }
    @PostMapping("/bookings/{ref}/cancel")
    public ResponseEntity<String> cancel(@PathVariable String ref, @RequestHeader(value = "X-User-Id", required = true) String userId) {
        bookingService.cancelBooking(ref, userId);
        return ResponseEntity.ok("Cancelled");
    }

    @PostMapping("/internal/bookings/{ref}/payment-success")
    public ResponseEntity<String> paymentSuccess(
            @RequestHeader("X-Internal-Token") String token,
            @PathVariable String ref,
            @RequestBody Map<String, Object> body
    ) {
        // 1. Verify caller
        if (!internalToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        // 2. Extract paymentId safely
        Long paymentId = null;
        if (body != null && body.get("paymentId") != null) {
            Number n = (Number) body.get("paymentId");
            paymentId = n.longValue();
        }

        // 3. Process callback
        bookingService.paymentSuccessCallback(ref, paymentId);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/internal/bookings/{ref}/payment-failed")
    public ResponseEntity<String> paymentFailed(
            @RequestHeader("X-Internal-Token") String token,
            @PathVariable String ref
    ) {
        if (!internalToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        bookingService.paymentFailedCallback(ref);
        return ResponseEntity.ok("OK");
    }

    private BookingDto toDto(Booking b) {
        BookingDto dto = new BookingDto();
        dto.setBookingReference(b.getBookingReference());
        dto.setBookingId(b.getId());
        dto.setDestinationId(b.getDestinationId());
        dto.setBookingDate(b.getBookingDate());
        dto.setNumPeople(b.getNumPeople());
        dto.setAmount(b.getAmount());
        dto.setStatus(b.getStatus());
        dto.setPaymentId(b.getPaymentId());
        return dto;
    }

}
