package com.example.stockapi;

import org.springframework.stereotype.Service;

// This class simulates the 3rd Party API. It sleeps for 2 seconds to mimic network latency.

@Service
public class StockService {

    // This method simulates fetching data from a slow external API (e.g., NYSE)
    public String getStockPrice(String symbol) {
        System.out.println("⚠️ Fetching price for " + symbol + " from External API (SLOW)...");
        simulateSlowNetwork();
        
        // Return a fake price
        return "Price of " + symbol + ": $" + (Math.random() * 100 + 100);
    }

    private void simulateSlowNetwork() {
        try {
            long start = System.currentTimeMillis();
            Thread.sleep(2000); // Wait 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}