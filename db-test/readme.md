# Database Performance Experiment: Full Table Scan vs. Indexing

## Experiment Intent
The goal of this experiment is to demonstrate the impact of **Database Indexing** on query performance.

We simulate a "silent killer" scenario: a database that performs well with small data but crawls when the dataset grows. We populate a table with **1 Million rows** and compare the search performance of a "Full Table Scan" versus an "Indexed Lookup."

---

## The Setup (The Data)
We use an embedded **H2 Database** (File Mode) to persist 1 million records.

**The Volume:**
- **Rows:** 1,000,000 `BlogPost` entries.
- **Data Distribution:** 5,000 unique authors (repeated ~200 times each).
- **The Seeder:** A batch script (`DataSeeder.java`) inserts these rows on startup.

---

## Phase 1: The Failure (Full Table Scan)
In the initial state, the database has **NO** index on the `author_name` column.

### The Code (Un-optimized Entity)
```java
@Entity
@Table(name = "blog_posts") // No Index defined
public class BlogPost {
    @Id
    @GeneratedValue
    private Long id;
    
    private String authorName; // Searching this requires checking every row
    // ...
}

```

### The Observation

1. **Query:** `GET /search?author=Author4000`
2. **Log Output:** `Found 200 posts in 888ms`
3. **Why:** The database engine performed a **Full Table Scan**. It read all 1,000,000 rows from the disk to find the matching 200. This burns CPU and I/O.

---

## Phase 2: The Fix (Indexing)

We add a B-Tree Index to the `author_name` column, creating a sorted lookup table (like the index at the back of a book).

### The Code (Optimized Entity)

```java
@Entity
@Table(name = "blog_posts", 
       indexes = @Index(name = "idx_author", columnList = "authorName")) //  Index Added
public class BlogPost {
    // ...
}

```

### The Observation

1. **Query:** `GET /search?author=Author4000`
2. **First Run (Cold Start):** `~880ms` (Overhead of loading index pages from disk to RAM).
3. **Second Run (Warm Cache):** `~12ms` 
4. **Why:** The database jumped directly to the relevant rows using the index tree, skipping 999,800 irrelevant rows.

---

##  Key Learnings

### 1. The "Cold Start" Phenomenon

You noticed the first request was still slow even with the index.

* **Reason:** The index lives on the disk (SSD). The first time you use it, the database must read the index structure into RAM (Buffer Pool).
* **Lesson:** Benchmarks should always ignore the first "warm-up" request.

### 2. The Trade-off

Indexing is not free magic.

* **Pro:** Read speed increased by **~70x** (888ms → 12ms).
* **Con:** Write speed decreases slightly (every INSERT now has to update the Table AND the Index).

## Summary of Metrics

| Scenario | Search Strategy | Time (Approx) | CPU Impact |
| --- | --- | --- | --- |
| **No Index** | Full Table Scan (Read 1M rows) | ~800ms | High (100% Core) |
| **With Index** | B-Tree Lookup (Read ~200 rows) | **~12ms** | Negligible |
