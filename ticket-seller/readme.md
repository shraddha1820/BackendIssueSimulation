# The Ticket Seller: Concurrency & Data Consistency

## Project Overview

This project demonstrates one of the most critical and difficult-to-catch bugs in backend engineering: **The Race Condition (Lost Update)**.

We simulate a high-demand "Flash Sale" scenario where multiple users try to buy limited inventory simultaneously.

* **The Asset:** 100 Concert Tickets.
* **The Demand:** 200 Concurrent Users.
* **The Goal:** Sell exactly 100 tickets. No more, no less.

---

## Phase 1: The Problem (The Race Condition)

### The Scenario

In a naive implementation, the "Buy" logic follows a simple 3-step process:

1. **Read:** Get current stock from DB.
2. **Decide:** If `stock > 0`, proceed.
3. **Write:** Save `stock - 1` to DB.

### The Bug

When two users (Thread A and Thread B) hit the server at the exact same millisecond, they trigger a **"Check-Then-Act"** race condition.

1. **Thread A** reads Stock = `100`.
2. **Thread B** reads Stock = `100` (Before A has finished).
3. **Thread A** writes Stock = `99`.
4. **Thread B** writes Stock = `99` (Overwriting A's work).
5. **Result:** Two tickets were sold, but the database only counted one sale.

### How to Reproduce

1. **Start the Server:**
```bash
mvn spring-boot:run

```


2. **Run the "Attack" Script:**
This script launches 200 concurrent threads to bombard the server.
```bash
mvn -Dtest=ConcurrencyTest test

```


3. **Check the Database:**
* URL: `http://localhost:8080/h2-console`
* Query: `SELECT * FROM TICKET_INVENTORY`
* **Observation:** Stock will be inconsistent (e.g., `-5`, `80`, etc.), proving that we oversold the inventory.



---

## Phase 2: The Solution (Pessimistic Locking)

To fix this, we must **serialize** the access to the shared resource (the Stock row). We use **Pessimistic Locking** (`SELECT ... FOR UPDATE`).

### The Fix

We modified the Repository to use JPA's `@Lock` annotation.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT t FROM TicketInventory t WHERE t.id = :id")
Optional<TicketInventory> findByIdWithLock(@Param("id") Long id);

```

### How It Works

1. **Thread A** starts a transaction and asks for the row.
2. **The Database locks the row.** No one else can read or write to it.
3. **Thread B** tries to read the row but is **blocked**. It must wait until Thread A finishes.
4. **Thread A** updates stock to `99` and commits. Lock is released.
5. **Thread B** is unblocked, reads the *new* stock (`99`), and updates to `98`.

### Verification

1. Enable the fix in `TicketController.java` (add `@Transactional` and use `findByIdWithLock`).
2. Restart the server and run the attack script again.
3. **Observation:** Stock will be exactly **0**. The system is consistent.

---

## Key Technical Concepts

### 1. Isolation Levels vs. Locking

* Standard database transactions provide "Isolation" (ACID), but usually only "Read Committed" level, which **does not** prevent Lost Updates.
* We need explicit **Locking** to handle high-concurrency writes on a single row.

### 2. Pessimistic vs. Optimistic Locking

| Strategy | Description | Best Use Case |
| --- | --- | --- |
| **Pessimistic** | "I trust no one." Lock the row immediately. | High contention (Flash sales). Guarantees order but slower. |
| **Optimistic** | "It's probably fine." Use a version number (`@Version`). Fail if version changed. | Low contention. Faster, but requires retry logic on failure. |

### 3. The Performance Trade-off

Consistency comes at a cost.

* **Without Locking:** Fast, parallel, but wrong data.
* **With Locking:** Slower, serial (one-by-one), but correct data.
* *Engineering Decision:* For money and inventory, correctness is always more important than raw speed.

---

## Project Structure

* `TicketInventory.java`: The Entity (Database Table).
* `TicketRepository.java`: The Data Access Layer (Includes the `@Lock` query).
* `TicketController.java`: The API Endpoint (Logic for buying tickets).
* `ConcurrencyTest.java`: The "Stress Test" script (Simulates 200 users).
