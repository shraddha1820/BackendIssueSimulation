package com.example.rate_limiter;

import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader; // Import this!
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    private final RateLimiterService rateLimiterService;

    public ApiController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api/public")
    public ResponseEntity<String> publicApi(@RequestHeader(value = "X-User-ID", defaultValue = "anonymous") String userId) {
        
        // 1. Get the bucket for this specific user
        Bucket bucket = rateLimiterService.resolveBucket(userId);

        // 2. Try to take 1 token
        if (bucket.tryConsume(1)) {
            // Success: They have tokens left
            return ResponseEntity.ok("✅ Success! Request processed for user: " + userId);
        } else {
            // Fail: Bucket is empty
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("⛔ Too Many Requests! Please wait.");
        }
    }
}