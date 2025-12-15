package org.example.bookingservice.exception;

public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String msg) { super(msg); }
}
