// package com.example.primeGenerator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadTester {
    public static void main(String[] args) throws InterruptedException {
        // We simulate 50 users trying to access the site at the exact same moment
        int concurrentUsers = 50; 
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        HttpClient client = HttpClient.newHttpClient();

        System.out.println("--- STARTING ATTACK WITH " + concurrentUsers + " USERS ---");
        long globalStart = System.currentTimeMillis();

        for (int i = 0; i < concurrentUsers; i++) {
            int userId = i;
            executor.submit(() -> {
                long reqStart = System.currentTimeMillis();
                try {
                    // Lower the 'n' value if your computer freezes completely
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/primes?n=10000")) 
                            .GET()
                            .build();

                    client.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    long reqEnd = System.currentTimeMillis();
                    long totalTime = reqEnd - reqStart;
                    
                    // If totalTime is much higher than processing time (~500ms), 
                    // it means the request was stuck in the queue!
                    System.out.println("User " + userId + " finished in: " + totalTime + "ms");
                    
                } catch (Exception e) {
                    System.err.println("User " + userId + " failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);
        
        long globalEnd = System.currentTimeMillis();
        System.out.println("--- TEST COMPLETED IN " + (globalEnd - globalStart) + "ms ---");
    }
}