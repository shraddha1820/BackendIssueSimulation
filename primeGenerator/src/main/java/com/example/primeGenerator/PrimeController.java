package com.example.primeGenerator;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

@RestController
public class PrimeController {

    @GetMapping("/health")
    public String healthCheck() {
        return "I am alive! " + System.currentTimeMillis();
    }

    // STEP 1: Change the return type to CompletableFuture
    // This tells Spring: "I will give you the result later, go free up the thread now."
    @GetMapping("/primes")
    @Async("taskExecutor") // STEP 2: Tell Spring to run this in a separate thread pool
    public CompletableFuture<String> getNthPrime(@RequestParam(defaultValue = "20000") int n) {
        
        long startTime = System.currentTimeMillis();
        
        // This heavy work now happens on a "task-1", "task-2" thread, NOT "http-nio-8080-exec-1"
        long prime = calculatePrime(n);
        
        long duration = System.currentTimeMillis() - startTime;
        String log = String.format("Calculated %dth prime in %dms | Thread: %s", 
                n, duration, Thread.currentThread().getName());
        
        System.out.println(log);
        
        // Return the result wrapped in a Future
        return CompletableFuture.completedFuture(log);
    }

    private long calculatePrime(int n) {
        int count = 0;
        long num = 2;
        while (count < n) {
            if (isPrime(num)) count++;
            num++;
        }
        return num - 1;
    }

    private boolean isPrime(long num) {
        if (num < 2) return false;
        for (long i = 2; i <= num / 2; i++) {
            if (num % i == 0) return false;
        }
        return true;
    }
}