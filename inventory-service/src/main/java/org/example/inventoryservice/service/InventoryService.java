package org.example.inventoryservice.service;

import org.example.inventoryservice.entity.Destination;
import org.example.inventoryservice.exception.NoSeatsAvailableException;
import org.example.inventoryservice.exception.NotFoundException;
import org.example.inventoryservice.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {
    private final DestinationRepository repo;

    @Autowired
    public InventoryService(DestinationRepository repo) {
        this.repo = repo;
    }

    public Destination create(Destination destination) {
         return repo.save(destination);
    }

    public Page<Destination> list(int page, int size) {
        return repo.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    public Destination get(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Destination not found"));
    }
    public Destination update(Long id, Destination update) {
        Destination existing = repo.findById(id).orElseThrow(() -> new NotFoundException("Destination not found"));
        existing.setName(update.getName());
        existing.setLocation(update.getLocation());
        existing.setDescription(update.getDescription());
        existing.setPrice(update.getPrice());
        existing.setAvailableSeats(update.getAvailableSeats());
        return repo.save(existing);
    }
    @Transactional
    public void reserve(Long destinationId, int count) {
        int updated = repo.tryReserve(destinationId, count);
        if (updated == 0) {
            throw new NoSeatsAvailableException("No seats available");
        }
    }

    @Transactional
    public void release(Long destinationId, int count) {
        int updated = repo.release(destinationId, count);
        if (updated == 0) {
            // Shouldn't usually happen; log or handle
        }
    }

    public void delete(Long id) {
        Optional<Destination> d = repo.findById(id);
        if (!d.isPresent()) {
            throw new NotFoundException("Destination not found");
        }
        repo.deleteById(id);
    }

}
