package com.example.ticket_seller;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

// A table to hold ticket count

@Entity
public class TicketInventory {

    @Id
    private Long id;
    private int stock;

    public TicketInventory() {}

    public TicketInventory(Long id, int stock) {
        this.id = id;
        this.stock = stock;
    }

    // Getters and Setters
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}