package com.example.rate_limiter;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class RateLimitTest {

    @Test
    public void testRateLimit() throws InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/public";

        // We simulate a user named "Hacker"
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-ID", "Hacker");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        System.out.println("--- STARTING SPAM ATTACK (15 Requests) ---");

        for (int i = 1; i <= 15; i++) {
            try {
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                System.out.println("Request " + i + ": ✅ 200 OK");
            } catch (HttpClientErrorException.TooManyRequests e) {
                System.out.println("Request " + i + ": ⛔ 429 TOO MANY REQUESTS");
            } catch (Exception e) {
                System.out.println("Request " + i + ": " + e.getMessage());
            }
        }
        
        System.out.println("--- ATTACK FINISHED ---");
    }
}