
# 3rd Party API Simulation: Latency & Caching

##  Experiment Intent
The goal of this experiment is to demonstrate how **Caching** protects your backend from slow, unreliable external services.

We simulate a common real-world scenario: fetching data from a "3rd Party API" (like a Stock Exchange or Weather Service). External APIs are often the bottleneck in modern microservices. We will simulate a **2-second latency** and then neutralize it using Spring Boot's Caching abstraction.

---

## The Setup (The "Slow" Service)
We created a service that intentionally sleeps for 2 seconds to mimic a network call across the internet.

**The Constraints:**
* **Latency:** Fixed 2000ms (2 seconds) per request.
* **Data Nature:** "Read-Heavy" (Many users request the same stock symbol, e.g., 'AAPL').

---

##  Phase 1: The Failure (No Caching)
Every request travels all the way to the "Slow Service," forcing the user to wait every single time.

### The Code
```java
public String getStockPrice(String symbol) {
    // SLOW: Simulating a network call
    Thread.sleep(2000); 
    return "Price: $100";
}

```

### The Observation

1. **User A requests 'AAPL':** Waits **2.0s**.
2. **User A refreshes:** Waits **2.0s** again.
3. **User B requests 'AAPL':** Waits **2.0s**.
4. **Impact:** The application feels broken. If the external API goes down, your app goes down.

---

## Phase 2: The Fix (In-Memory Caching)

We instruct Spring to store the result of the function call in memory.

### The Code

**1. Enable Caching (`Application.java`):**

```java
@EnableCaching // Turns on the caching engine
public class StockApiApplication { ... }

```

**2. Annotate the Method (`StockService.java`):**

```java
@Cacheable("prices") // Intercepts the call
public String getStockPrice(String symbol) {
    Thread.sleep(2000);
    return "Price: $100";
}

```

### The Observation

1. **Request 1 (Cache Miss):**
* Spring checks the "prices" map ➔ Empty.
* Executes the method ➔ **Waits 2.0s**.
* Stores result: `{"AAPL": "Price: $100"}` inside the map.


2. **Request 2 (Cache Hit):**
* Spring checks the "prices" map ➔ Found "AAPL"!
* **Skips the method entirely.** Returns data instantly.
* **Time:** **~0ms** 



---

## Key Learnings

### 1. The "Cache Hit" vs. "Cache Miss"

* **Miss (Costly):** The data isn't in memory. You pay the penalty (latency) to fetch it.
* **Hit (Free):** The data is in memory. You get it instantly.

### 2. The Trade-off: Stale Data

Speed comes at a cost: **Accuracy**.

* If the *real* stock price changes to $105, your cache still says $100.
* **Fix:** You must set an **Eviction Policy** (TTL - Time To Live), e.g., "Delete this cache entry after 60 seconds" so we force a fresh fetch.

### 3. "Shared" Data

This fix works perfectly because **everyone sees the same price for AAPL**.

* If this were *user-specific* data (e.g., "My Portfolio Balance"), you would need to cache based on `userId`, not just `symbol`.

## Summary of Metrics

| Scenario | Strategy | Response Time | User Experience |
| --- | --- | --- | --- |
| **No Cache** | Always call External API | **2000ms** (Consistent) | Frustrating / Slow |
| **With Cache** | Store & Reuse Result | **~1ms** (After 1st load) | Instant / Snappy |
