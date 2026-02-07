

# Payment Service: System Resilience & Circuit Breakers

##  Project Overview

This project demonstrates how to build a **Resilient Microservice** that can survive the failure of its dependencies.

We simulate a "Payment Service" that relies on a 3rd-party API (like Stripe, PayPal, or a Bank).

* **The Dependency:** An unstable external API that fails 50% of the time.
* **The Goal:** Prevent these failures from crashing our own application.
* **The Tool:** **Resilience4j** (The standard fault tolerance library for Java).

---

##  Phase 1: The Problem (Cascading Failures)

### The Scenario

In a distributed system, your service rarely works alone. It calls Database A, Service B, and External API C.

1. **The Trigger:** The External API starts failing or becomes incredibly slow (latency spikes).
2. **The Impact:** Your application threads get stuck waiting for the API to respond.
3. **The Cascade:** As more users make requests, *all* your threads become occupied waiting.
4. **The Result:** Your entire application becomes unresponsive (Resource Exhaustion). One bad dependency has taken down your whole system.

---

##  Phase 2: The Solution (The Circuit Breaker)

To fix this, we implement the **Circuit Breaker Pattern**. Just like an electrical fuse, it cuts the connection when it detects a "power surge" (too many errors).

### How It Works (The State Machine)

The Circuit Breaker monitors the success/failure rate of requests to the external service.

1. ** CLOSED (Normal State):**
* Traffic flows freely to the external API.
* The breaker counts failures.
* *If failures exceed 50% (threshold), the circuit TRIPS to Open.*


2. ** OPEN (Failure State):**
* **The connection is cut.**
* All requests fail **instantly** (Fail Fast) with an exception or fallback message.
* The external API is not called. This gives it time to recover.
* *After a set time (e.g., 10 seconds), the circuit moves to Half-Open.*


3. ** HALF-OPEN (Testing State):**
* The system allows a limited number of "test requests" (e.g., 3) to pass through.
* **If they succeed:** The circuit resets to **CLOSED**.
* **If they fail:** The circuit returns to **OPEN**.



---

## Technical Implementation

### 1. The Annotation

We wrapped our risky method with `@CircuitBreaker`.

```java
@CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackPayment")
public String processPayment() {
    // Risky code (Simulated 50% failure)
}

```

### 2. The Configuration (`application.properties`)

We defined strict rules for when to trip the circuit:

```properties
# Failure Rate Threshold: 50%
resilience4j.circuitbreaker.instances.paymentService.failure-rate-threshold=50

# Sliding Window: Look at the last 5 requests
resilience4j.circuitbreaker.instances.paymentService.sliding-window-size=5

# Wait Duration: Stay open for 10 seconds
resilience4j.circuitbreaker.instances.paymentService.wait-duration-in-open-state=10s

```

---

##  Verification

### 1. The Setup

* **Endpoint:** `GET /pay`
* **Behavior:** Calls the `PaymentService`, which has a 50% chance of throwing a `RuntimeException`.

### 2. The Test

We manually stressed the system by refreshing the browser repeatedly.

### 3. The Evidence (Logs)

1. **Mixed Results:** Initially, logs show random successes and failures.
* ` Payment Successful`
* ` Payment Gateway Failed!`


2. **Circuit Trip:** After ~3 failures, the logs change drastically.
* **No more "Gateway Failed" logs.**
* Only: `Circuit Open! Returning fallback response.`


3. **Recovery:** After 10 seconds, the system attempts to reach the gateway again.

---

##  Key Takeaways

### 1. Fail Fast

The most important feature of a Circuit Breaker is **speed**.

* **Without Breaker:** User waits 30s for a timeout. Server thread is blocked for 30s.
* **With Breaker (Open):** User gets error in 10ms. Server thread is free immediately.

### 2. Fallbacks improve User Experience

Instead of showing a raw "500 Internal Server Error," we provided a graceful fallback:

* *"Payment Failed (Circuit Open). Please try again later."*
* In a real app, this could be *"The payment system is down, but we saved your cart."*
