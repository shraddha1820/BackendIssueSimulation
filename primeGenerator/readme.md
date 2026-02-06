# Backend Resilience Experiment: Thread Starvation vs. Async Processing

## Experiment Intent
The goal of this experiment is to demonstrate a fundamental backend vulnerability: **Thread Starvation**. 

We simulate a scenario where a few "heavy" requests (CPU-intensive tasks) consume all available server threads, causing the entire application to become unresponsive—even for lightweight, harmless requests (like a health check). We then implement an **Asynchronous** architecture to fix this, decoupling request handling from task execution.

---

## The Setup (The Constraints)
To make the failure visible on a local machine, we artificially constrained the web server (Tomcat) to a very small worker pool.

**File:** `src/main/resources/application.properties`
```properties
server.port=8080

# THE CONSTRAINT: 
# We limit the server to only 10 concurrent threads. 
# The 11th simultaneous request must wait in the queue.
server.tomcat.threads.max=10
server.tomcat.threads.min-spare=10

```

---

## Phase 1: The Failure (Synchronous Blocking)

In this scenario, the heavy calculation happens **on the same thread** that accepted the HTTP request.

### The Code (The "Bad" Controller)

```java
@GetMapping("/primes")
public String getNthPrime(@RequestParam int n) {
    // BLOCKING OPERATION
    // This thread is now stuck doing math for 2+ seconds.
    // It cannot accept new users until this finishes.
    long prime = calculatePrime(n); 
    return "Result: " + prime;
}

@GetMapping("/health")
public String health() {
    // This takes 0ms, but will TIMEOUT during the load test
    // because no threads are free to handle it.
    return "I am alive!";
}

```

### The Observation

1. **Load Test:** We spam `/primes` with 50 concurrent users.
2. **Browser Test:** We try to open `/health`.
3. **Result:** The browser spins indefinitely (or times out). The server appears "dead" because all 10 threads are busy calculating primes.

**The Crux:** Code changes cannot fix hardware limits (CPU speed), but bad architecture (Blocking I/O) destroys **Availability**.

---

## Phase 2: The Fix (Asynchronous Processing)

We move the heavy lifting to a separate "Worker Thread Pool," freeing up the main Tomcat threads immediately.

### The Configuration

We enable async processing and define a separate pool for heavy tasks.

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // The "Back Office" team
        return executor;
    }
}

```

### The Code (The "Good" Controller)

```java
@GetMapping("/primes")
@Async("taskExecutor") // Run this in the background pool
public CompletableFuture<String> getNthPrime(@RequestParam int n) {
    // The heavy math happens here, but the Tomcat thread 
    // has already returned to the pool to help other users.
    long prime = calculatePrime(n);
    return CompletableFuture.completedFuture("Result: " + prime);
}

```

### The Observation

1. **Load Test:** The `/primes` requests still take time (CPU is still busy).
2. **Browser Test:** We try to open `/health`.
3. **Result:** The `/health` endpoint loads **INSTANTLY**.

---

## Summary of Findings

| Metric | Synchronous (Blocking) | Asynchronous (Non-Blocking) |
| --- | --- | --- |
| **Request Latency** | High (Wait Time + Processing Time) | High (Processing Time) |
| **Server Availability** | **0%** (Server freezes) | **100%** (Server stays responsive) |
| **Health Check** | Timed Out | **Instant** |

### Key Takeaway

**`@Async` does not make the calculation faster.** It makes the system **resilient**. By offloading work, you ensure that one slow feature does not take down the entire application.
