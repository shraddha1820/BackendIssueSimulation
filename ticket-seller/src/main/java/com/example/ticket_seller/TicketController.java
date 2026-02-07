package com.example.ticket_seller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {

    private final TicketRepository repository;

    public TicketController(TicketRepository repository) {
        this.repository = repository;
        // Initialize inventory with 100 tickets on startup (ID: 1)
        repository.save(new TicketInventory(1L, 100));
    }

    @PostMapping("/buy")
    public String buyTicket() {
        // 1. READ the current stock
        TicketInventory inventory = repository.findById(1L).orElseThrow();

        // 2. CHECK if we have enough
        if (inventory.getStock() > 0) {
            
            // ⚠️ THE DANGER ZONE ⚠️
            // A race condition happens here.
            // If User A and User B are both here at the same time,
            // they BOTH see stock > 0.
            
            try { Thread.sleep(50); } catch (Exception e) {} // Simulate processing time

            // 3. WRITE the new stock (Stock - 1)
            inventory.setStock(inventory.getStock() - 1);
            repository.save(inventory);
            
            return "Success! Remaining: " + inventory.getStock();
        } else {
            return "Sold Out!";
        }
    }
}