package org.example.bookingservice.dto;

import java.math.BigDecimal;

public class DestinationPriceResponse {
    private Long id;
    private BigDecimal price;
    public DestinationPriceResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
