package org.example.inventoryservice.controller;

import jakarta.validation.Valid;
import org.example.inventoryservice.dto.CreateDestinationRequest;
import org.example.inventoryservice.dto.DestinationDto;
import org.example.inventoryservice.entity.Destination;
import org.example.inventoryservice.service.InventoryService;
import org.example.inventoryservice.util.DestinationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService service;
    @Value("${internal.service.token}")
    private String internalToken;

    @Autowired
    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping("/destinations")
    public ResponseEntity<DestinationDto> create(
            @RequestHeader("X-User-Role") String role,
            @RequestBody CreateDestinationRequest request
    ) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Destination destination = new Destination(
                request.getName(),
                request.getLocation(),
                request.getDescription(),
                request.getPrice(),
                request.getAvailableSeats()
        );
        Destination created = service.create(destination);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DestinationMapper.toDto(created));
    }

    @GetMapping("/destinations")
    public ResponseEntity<List<DestinationDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        // Basic implementation: paging, no filters (improve later)
        Page<Destination> p = service.list(page, size);
        List<DestinationDto> res = p.stream().map(DestinationMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/destinations/{id}")
    public ResponseEntity<DestinationDto> get(@PathVariable Long id) {
        Destination d = service.get(id);
        return ResponseEntity.ok(DestinationMapper.toDto(d));
    }

    @PutMapping("/destinations/{id}")
    public ResponseEntity<DestinationDto> update(@PathVariable Long id, @Valid @RequestBody CreateDestinationRequest req) {
        Destination update = new Destination(req.getName(), req.getLocation(), req.getDescription(), req.getPrice(), req.getAvailableSeats());
        Destination d = service.update(id, update);
        return ResponseEntity.ok(DestinationMapper.toDto(d));
    }

    @DeleteMapping("/destinations/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Deleted");
    }

    // Internal endpoints used by booking-service
    // Reserve seats
    @PostMapping("/internal/destinations/{id}/reserve")
    public ResponseEntity<String> reserve(
            @RequestHeader("X-Internal-Token") String token,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int count
    ) {
        if (!token.equals(internalToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        service.reserve(id, count);
        return ResponseEntity.ok("Reserved");
    }

    @PostMapping("/internal/destinations/{id}/release")
    public ResponseEntity<String> release(
            @RequestHeader("X-Internal-Token") String token,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int count
    ) {
        if (!token.equals(internalToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        service.release(id, count);
        return ResponseEntity.ok("Released");
    }



}
