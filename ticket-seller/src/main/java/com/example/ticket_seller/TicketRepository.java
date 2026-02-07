package com.example.ticket_seller;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketInventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 🔒 This is the magic key
    @Query("SELECT t FROM TicketInventory t WHERE t.id = :id")
    Optional<TicketInventory> findByIdWithLock(@Param("id") Long id);
}