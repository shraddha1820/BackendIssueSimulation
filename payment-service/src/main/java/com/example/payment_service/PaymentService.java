package com.example.payment_service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Random;

// Simulate a Payment Gateway that fails randomly

@Service
public class PaymentService {

    // 1. Name of the circuit breaker config
    // 2. Fallback method to call if the circuit is OPEN or the method fails
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackPayment")
    public String processPayment() {
        // Simulate random failure (50% chance)
        if (new Random().nextBoolean()) {
            System.out.println("❌ Payment Gateway Failed!");
            throw new RuntimeException("Payment Gateway Down");
        }

        System.out.println("✅ Payment Successful");
        return "Payment Processed Successfully";
    }

    // This method runs when the circuit is OPEN or exception is thrown
    public String fallbackPayment(Throwable t) {
        System.out.println("⚠️ Circuit Open! Returning fallback response.");
        return "Payment Failed (Circuit Open). Please try again later.";
    }
}