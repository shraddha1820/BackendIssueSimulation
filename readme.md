
#  Backend Engineering: 6 Real-World Simulations

##  Overview

This repository contains a collection of 6 targeted simulations demonstrating advanced backend engineering concepts. Each module isolates a specific, critical failure mode in modern systems (e.g., race conditions, cascading failures, slow queries) and implements the industry-standard solution using **Spring Boot**.

These are not just code snippets; they are **reproducible experiments**. You can break the system, watch it fail, and then apply the fix to see resilience in action.

---

##  The Big Picture: How It All Fits

In a real-world production system, these 6 concepts work together to protect the lifecycle of a single request.

1. **The Bouncer:** The **Rate Limiter** stops spam at the door.
2. **The Fast Lane:** **Async Processing** moves slow tasks (like email) out of the user's way.
3. **The Memory:** **Redis Caching** prevents unnecessary database hits.
4. **The Librarian:** **Database Indexing** makes finding data instant.
5. **The Traffic Cop:** **Pessimistic Locking** prevents users from buying the same seat.
6. **The Fuse:** The **Circuit Breaker** stops a broken 3rd-party API from crashing the whole system.

---

## The 6 Scenarios

### 1.  Responsiveness: Async Processing

**"The user shouldn't wait for the email to send."**

* **The Scenario:** A user signs up, but the server hangs for 5 seconds waiting to send a "Welcome Email".
* **The Issue:** Blocking the main thread degrades user experience and server throughput.
* **The Fix:** **`@Async` & `ThreadPoolTaskExecutor**`. We fire the event to a background thread and return a response immediately.
* **Tech Stack:** Java `CompletableFuture`, Spring Async.

### 2.  Performance: Database Indexing

**"Why does this search take 200ms?"**

* **The Scenario:** A search query for a specific user takes 200ms because the database is scanning 1 million rows one by one.
* **The Issue:** Full Table Scans (O(N)) kill database CPU and slow down the entire app.
* **The Fix:** **B-Tree Indexes**. We create an ordered map of the data, turning the search into an instant O(log N) operation.
* **Tech Stack:** SQL `CREATE INDEX`, `EXPLAIN ANALYZE`.

### 3.  Latency: Look-Aside Caching

**"The database is melting under the load."**

* **The Scenario:** A "Product Details" page is hit 1,000 times/second. The database is overwhelmed.
* **The Issue:** Fetching static data from disk (DB) is expensive and slow.
* **The Fix:** **Redis (Look-Aside Pattern)**. We check fast memory (RAM) first. If data is missing, we fetch from DB and save it to RAM for next time.
* **Tech Stack:** Redis, Spring Cache (`@Cacheable`).

### 4. Consistency: Handling Race Conditions

**"We sold 105 tickets for a 100-seat event."**

* **The Scenario:** 200 users try to buy the last concert ticket at the exact same millisecond. The system sells it to 20 people.
* **The Issue:** "Check-Then-Act" race conditions allow multiple threads to overwrite each other's data.
* **The Fix:** **Pessimistic Locking (`SELECT ... FOR UPDATE`)**. We lock the database row so only one user can process it at a time.
* **Tech Stack:** JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

### 5. Stability: Rate Limiting
**"One user is crashing the server."**

* **The Scenario:** A malicious script (or buggy client) sends 10,000 requests/second, crashing the server for everyone.
* **The Issue:** Unbounded traffic consumes all server resources (CPU/RAM).
* **The Fix:** **Token Bucket Algorithm**. We give each user a "quota" (e.g., 10 req/min). If they exceed it, we reject them instantly with HTTP 429.
* **Tech Stack:** Bucket4j.

### 6.  Resilience: Circuit Breakers

**"The Payment Gateway is down, and now *we* are down."**

* **The Scenario:** The "Payment Service" (Stripe/PayPal) goes down. Your server threads get stuck waiting for it, eventually freezing the whole app.
* **The Issue:** Cascading Failures. One broken dependency takes down the entire system.
* **The Fix:** **Circuit Breaker Pattern**. If failures exceed a threshold (e.g., 50%), we "trip" the circuit and fail fast without calling the broken service.
* **Tech Stack:** Resilience4j.



## Philosophy & Key Takeaways

* **Fail Fast:** It is better to return an error immediately (Circuit Breaker) than to make the user wait 30 seconds.
* **Consistency > Speed:** For money and inventory, we sacrifice speed (Locking) to ensure correctness.
* **Protect Your Resources:** Never trust the client. Always Rate Limit and Validate.
* **Layers Matter:** The Database is for *storage*, not for *high-frequency reads*. Use Caches.