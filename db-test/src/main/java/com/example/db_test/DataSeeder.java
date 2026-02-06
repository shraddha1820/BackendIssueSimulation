package com.example.db_test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // 1. Check if data exists so we don't re-seed on restart
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM blog_posts", Integer.class);
        if (count != null && count > 100_000) {
            System.out.println("Data already loaded (" + count + " rows). Ready.");
            return;
        }

        // 2. Insert 1 Million Rows (Batch Insert for speed)
        System.out.println("--- STARTING SEED (1 MILLION ROWS) - PLEASE WAIT ~30s ---");
        long start = System.currentTimeMillis();

        String sql = "INSERT INTO blog_posts (title, author_name, content) VALUES (?, ?, ?)";
        List<Object[]> batch = new ArrayList<>();

        for (int i = 1; i <= 1_000_000; i++) {
            batch.add(new Object[]{
                "Java Tutorial #" + i,
                "Author" + (i % 5000), // We reuse authors so searches find multiple results
                "Content for post " + i
            });

            // Commit every 1000 rows
            if (i % 1000 == 0) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
                if (i % 200_000 == 0) System.out.println("Inserted " + i + " rows...");
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("--- SEEDING COMPLETE in " + (end - start) + "ms ---");
    }
}