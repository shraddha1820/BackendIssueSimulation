package com.example.primeGenerator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrimeController {

    @GetMapping("/primes")
    public String getNthPrime(@RequestParam(defaultValue = "5000") int n) {
        long startTime = System.currentTimeMillis();
        
        // This is the "Blocking" operation
        long prime = calculatePrime(n);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // We print the thread name so you can see only 10 threads (http-nio-8080-exec-1 to 10) working.
        String log = String.format("Calculated %dth prime in %dms | Thread: %s", 
                n, duration, Thread.currentThread().getName());
        
        System.out.println(log);
        return log;
    }

    // Inefficient algorithm to deliberately burn CPU cycles
    private long calculatePrime(int n) {
        int count = 0;
        long num = 2;
        while (count < n) {
            if (isPrime(num)) {
                count++;
            }
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