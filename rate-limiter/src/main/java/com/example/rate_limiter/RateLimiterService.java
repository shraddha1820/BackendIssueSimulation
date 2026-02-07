package com.example.rate_limiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// This class manages the "Buckets". It creates a new bucket for every new user (simulated by IP address) and defines the rules.

@Service
public class RateLimiterService {

    // Storage for buckets: Key = User ID (or IP), Value = Their Bucket
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String userId) {
        return cache.computeIfAbsent(userId, this::createNewBucket);
    }

    private Bucket createNewBucket(String userId) {
        // Define the limit: 10 tokens, refilled every 1 minute
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}