package org.example.inventoryservice.util;

import org.example.inventoryservice.dto.DestinationDto;
import org.example.inventoryservice.entity.Destination;

public class DestinationMapper {
    public static DestinationDto toDto(Destination d) {
        DestinationDto dto = new DestinationDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setLocation(d.getLocation());
        dto.setDescription(d.getDescription());
        dto.setPrice(d.getPrice());
        dto.setAvailableSeats(d.getAvailableSeats());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        return dto;
    }
}
