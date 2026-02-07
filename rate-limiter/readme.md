# The Rate Limiter: API Stability & Traffic Control

## Project Overview

This project demonstrates how to protect a public API from abuse, spam, and Denial of Service (DoS) attacks using **Rate Limiting**.

We simulate a "Public API" that is open to the world. Without protection, a single malicious user (or a buggy script) can overwhelm the server with thousands of requests, consuming all CPU/RAM and crashing the system for everyone else.

* **The Goal:** Allow legitimate users to access the API.
* **The Constraint:** Block users who exceed a specific quota (e.g., 10 requests/minute).

---

## Phase 1: The Problem (Unbounded Traffic)

### The Scenario

In a naive implementation, the server accepts every request it receives.

1. **User A** sends 1 request/second (Normal).
2. **User B (Attacker)** sends 10,000 requests/second (Malicious).
3. **Result:** The server tries to process all 10,001 requests.
4. **Impact:**
* **Resource Exhaustion:** The server runs out of threads or memory.
* **Cascading Failure:** The database (if connected) also crashes under the load.
* **Denial of Service:** User A cannot access the API because the server is too busy processing User B's spam.



---

## Phase 2: The Solution (Token Bucket Algorithm)

To fix this, we implement the **Token Bucket Algorithm** using the **Bucket4j** library.

### How It Works

Imagine a physical bucket for each user.

1. **Tokens:** The bucket holds "Tokens". (e.g., Capacity = 10 tokens).
2. **Cost:** Every API request costs **1 Token**.
3. **Refill:** Tokens are added back to the bucket at a fixed rate (e.g., 10 tokens every 1 minute).
4. **The Rule:**
* **If Bucket has Tokens:** Take 1 token, allow the request (`200 OK`).
* **If Bucket is Empty:** Reject the request immediately (`429 Too Many Requests`).



### Technical Implementation

We configured a `RateLimiterService` that creates a bucket for each unique User ID (or IP address).

```java
// Define the limit: 10 tokens, refilled every 1 minute
Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));

return Bucket.builder()
        .addLimit(limit)
        .build();

```

---

## Verification

### 1. The Setup

* **Endpoint:** `GET /api/public`
* **Header:** `X-User-ID: Hacker` (Simulating a specific user)
* **Quota:** 10 Requests per minute.

### 2. The "Spam" Attack

We wrote a test script (`RateLimitTest.java`) that fires **15 requests** instantly.

### 3. The Results

The logs confirm the algorithm works perfectly:

```text
Request 1:  200 OK (Tokens left: 9)
Request 2:  200 OK (Tokens left: 8)
...
Request 10: 200 OK (Tokens left: 0)
Request 11: 429 TOO MANY REQUESTS (Bucket Empty)
Request 12: 429 TOO MANY REQUESTS
...
Request 15: 429 TOO MANY REQUESTS

```

---

## Project Structure

* `RateLimiterService.java`: Manages the creation and storage of Buckets (using a `ConcurrentHashMap`).
* `ApiController.java`: The endpoint that checks the bucket before processing the request.
* `RateLimitTest.java`: The simulation script that acts as the "Spammer".

---

## Key Takeaways

### 1. Fail Fast

Rate limiting allows the server to reject bad requests **cheaply**.

* Processing a request might take 100ms (Database, Logic, etc.).
* Rejecting a request takes < 1ms (Check bucket in memory).
* This saves the server's resources for legitimate users.

### 2. HTTP 429

The standard HTTP status code for "You are sending too many requests" is `429`. It tells the client to "Back off and try again later."

### 3. Granularity

We limited by `User ID`. In production, you often limit by:

* **IP Address:** To stop anonymous botnets.
* **API Key:** To enforce pricing tiers (e.g., Free Tier = 100 req/hour, Pro Tier = 10,000 req/hour).
