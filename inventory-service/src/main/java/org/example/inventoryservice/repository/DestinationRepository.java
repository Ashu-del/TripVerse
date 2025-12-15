package org.example.inventoryservice.repository;

import org.example.inventoryservice.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination,Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Destination d SET d.availableSeats = d.availableSeats - :count WHERE d.id = :id AND d.availableSeats >= :count")
    int tryReserve(@Param("id") Long id, @Param("count") int count);

    @Modifying
    @Transactional
    @Query("UPDATE Destination d SET d.availableSeats = d.availableSeats + :count WHERE d.id = :id")
    int release(@Param("id") Long id, @Param("count") int count);

}
