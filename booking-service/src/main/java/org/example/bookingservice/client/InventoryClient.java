package org.example.bookingservice.client;

import org.example.bookingservice.dto.DestinationPriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {
    @GetMapping("/inventory/destinations/{id}")
    DestinationPriceResponse getDestination(@PathVariable("id") Long id);

    // internal reserve endpoint - booking-service must pass internal token header
    @PostMapping("/inventory/internal/destinations/{id}/reserve")
    void reserve(@PathVariable("id") Long id, @RequestParam("count") Integer count,
                 @RequestHeader(value = "X-Internal-Token") String internalToken);

    @PostMapping("/inventory/internal/destinations/{id}/release")
    void release(@PathVariable("id") Long id, @RequestParam("count") Integer count,
                 @RequestHeader(value = "X-Internal-Token") String internalToken);

}
