package org.example.inventoryservice.exception;

public class NoSeatsAvailableException extends RuntimeException{
    public NoSeatsAvailableException(String message) { super(message); }
}
