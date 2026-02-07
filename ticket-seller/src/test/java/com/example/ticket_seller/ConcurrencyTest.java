package com.example.ticketseller;

import org.junit.jupiter.api.Test; // Import JUnit
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrencyTest {

    @Test // <--- CHANGED: This is now a Test, not a Main method
    public void testConcurrency() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(200);
        RestTemplate restTemplate = new RestTemplate();

        System.out.println("--- STARTING TICKET SALE (100 SEATS) ---");

        for (int i = 0; i < 200; i++) {
            executor.submit(() -> {
                try {
                    restTemplate.postForObject("http://localhost:8080/buy", null, String.class);
                } catch (Exception e) {
                    // Ignore errors
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("--- SALE FINISHED. CHECK H2 CONSOLE ---");
    }
}