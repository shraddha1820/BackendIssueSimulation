package com.example.ticket_seller;

import org.springframework.transaction.annotation.Transactional; // Import this!
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {

    private final TicketRepository repository;

    public TicketController(TicketRepository repository) {
        this.repository = repository;
        // Reset stock to 100 on restart
        repository.save(new TicketInventory(1L, 100));
    }

    @PostMapping("/buy")
    @Transactional // CRITICAL: Keeps the lock active until the method ends
    public String buyTicket() {
        // 1. READ with LOCK (Others must wait here now)
        TicketInventory inventory = repository.findByIdWithLock(1L).orElseThrow();

        // 2. CHECK
        if (inventory.getStock() > 0) {
            try { Thread.sleep(50); } catch (Exception e) {} 

            // 3. WRITE
            inventory.setStock(inventory.getStock() - 1);
            repository.save(inventory);
            
            return "Success! Remaining: " + inventory.getStock();
        } else {
            return "Sold Out!";
        }
    }
}